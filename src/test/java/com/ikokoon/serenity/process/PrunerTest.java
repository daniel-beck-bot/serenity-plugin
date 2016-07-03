package com.ikokoon.serenity.process;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Executer;
import com.ikokoon.toolkit.Toolkit;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;

import static com.ikokoon.serenity.persistence.IDataBase.DataBaseManager.getDataBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-01-2010
 */
public class PrunerTest extends ATest {

    @Test
    @SuppressWarnings("rawtypes")
    public void execute() throws IOException, IllegalAccessException, IllegalClassFormatException, NoSuchFieldException {
        final String sourceDataBaseFile = "./serenity/serenity-one.odb";
        final String targetDataBaseFile = "./serenity/serenity-two.odb";

        loadDatabase(sourceDataBaseFile, targetDataBaseFile);

        IDataBase targetDataBase = getDataBase(DataBaseOdb.class, targetDataBaseFile, Boolean.FALSE, null);
        targetDataBase.persist(new Line<>(className, methodName, 1));
        targetDataBase.persist(new Afferent("og.afferent"));
        targetDataBase.persist(new Efferent("org.efferent"));

        assertTrue(targetDataBase.find(Line.class).size() > 0);
        assertTrue(targetDataBase.find(Afferent.class).size() > 0);
        assertTrue(targetDataBase.find(Efferent.class).size() > 0);

        double pruneDuration = Executer.execute(new Executer.IPerform() {
            public void execute() {
                IDataBase targetDataBase = getDataBase(DataBaseOdb.class, targetDataBaseFile, Boolean.FALSE, null);
                new Pruner(null, targetDataBase).execute();
            }
        }, "PrunerTest : ", 1);
        LOGGER.warn("Prune duration : " + pruneDuration);

        assertEquals(0, targetDataBase.find(Line.class).size());
        assertEquals(0, targetDataBase.find(Afferent.class).size());
        assertEquals(0, targetDataBase.find(Efferent.class).size());

        targetDataBase.close();

        Toolkit.deleteFile(new File(targetDataBaseFile), 3);
    }

}