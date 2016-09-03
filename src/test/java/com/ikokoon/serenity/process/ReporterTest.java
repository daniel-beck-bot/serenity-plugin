package com.ikokoon.serenity.process;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.Profiler;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.ikokoon.serenity.persistence.IDataBase.DataBaseManager.getDataBase;

/**
 * This test needs to have assertions. TODO implement the real tests.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 19.06.10
 */
@Ignore
public class ReporterTest extends ATest {

    private static IDataBase dataBase;

    @Test
    public void methodSeries() throws Exception {
        System.setProperty(IConstants.TIME_UNIT, "1000");
        String dataBaseFile = "./src/test/resources/profiler/serenity.odb";
        dataBase = getDataBase(DataBaseOdb.class, dataBaseFile, Boolean.FALSE, mockInternalDataBase);

        Profiler.initialize(dataBase);
        String html = new Reporter(null, dataBase).methodSeries(dataBase);
        File file = new File(IConstants.METHOD_SERIES_FILE);
        Toolkit.setContents(file, html.getBytes());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void buildGraph() {
        String dataBaseFile = "./src/test/resources/isearch/serenity.odb";
        dataBase = getDataBase(DataBaseOdb.class, dataBaseFile, Boolean.FALSE, mockInternalDataBase);

        File chartDirectory = new File(IConstants.SERENITY_DIRECTORY + File.separatorChar + IConstants.CHARTS);
        Toolkit.deleteFile(chartDirectory, 3);
        // DataBaseToolkit.dump(dataBase, null, "ReporterTest");
        String className = "com.ikokoon.search.listener.EventPersistenceListener";
        long id = Toolkit.hash(className);
        Class klass = dataBase.find(Class.class, id);
        List<Method> methods = klass.getChildren();
        for (Method method : methods) {
            List<Double> methodSeries = new Calculator().methodSeries(method);
            logger.warn("Method series : " + methodSeries);
            String graph = new Reporter(null, dataBase).buildGraph(IConstants.METHOD_SERIES, method, methodSeries);
            logger.info("Built graph : " + graph);
        }
    }

    @Test
    public void report() {
        String dataBaseFile = "./src/test/resources/isearch/serenity.odb";
        dataBase = getDataBase(DataBaseOdb.class, dataBaseFile, Boolean.FALSE, mockInternalDataBase);
        new Reporter(null, dataBase).execute();
    }

}