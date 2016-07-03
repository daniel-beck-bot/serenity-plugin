package com.ikokoon.serenity.process;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Executer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.util.List;

import static com.ikokoon.serenity.persistence.IDataBase.DataBaseManager.getDataBase;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-01-2010
 */
public class CleanerTest extends ATest {

    private IDataBase dataBaseOne;
    private IDataBase dataBaseTwo;

    @Before
    public void before() throws IOException, IllegalAccessException, IllegalClassFormatException, NoSuchFieldException {
        String databaseFileNameOne = "./serenity/serenity-clean-one.odb";
        String databaseFileNameTwo = "./serenity/serenity-clean-two.odb";

        loadDatabase(databaseFileNameOne, databaseFileNameTwo);

        dataBaseOne = getDataBase(DataBaseRam.class, databaseFileNameOne, Boolean.FALSE, null);
        dataBaseTwo = getDataBase(DataBaseRam.class, databaseFileNameTwo, Boolean.FALSE, null);

        DataBaseToolkit.copyDataBase(dataBaseOne, dataBaseTwo);
    }

    @After
    public void after() {
        DataBaseToolkit.clear(dataBaseOne);
        DataBaseToolkit.clear(dataBaseTwo);
    }

    @Test
    public void execute() {
        assertTrue(containsPattern(RunWith.class.getSimpleName()));
        assertTrue(containsPattern(RunWith.class.getPackage().getName()));

        Executer.execute(new Executer.IPerform() {
            public void execute() {
                new Cleaner(null, dataBaseTwo).execute();
            }
        }, "CleanerTest : ", 1);

        assertFalse(containsPattern(RunWith.class.getSimpleName()));
        assertFalse(containsPattern(RunWith.class.getPackage().getName()));
    }

    @SuppressWarnings("rawtypes")
    private boolean containsPattern(final String pattern) {
        // Assert that there are not spelling packages in the database
        List<Package> packages = dataBaseTwo.find(Package.class);
        for (final Package<?, ?> pakkage : packages) {
            if (pakkage.getName().contains(pattern)) {
                return Boolean.TRUE;
            }
        }
        List<Class> classes = dataBaseTwo.find(Class.class);
        for (final Class clazz : classes) {
            if (clazz.getName().contains(pattern)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

}