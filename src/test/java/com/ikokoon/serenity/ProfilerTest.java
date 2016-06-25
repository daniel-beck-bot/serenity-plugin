package com.ikokoon.serenity;

import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.serenity.process.Calculator;
import com.ikokoon.toolkit.Executer;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static com.ikokoon.serenity.persistence.IDataBase.DataBaseManager.getDataBase;

/**
 * This test needs to have assertions. TODO implement the real tests.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 19.06.10
 */
public class ProfilerTest extends ATest implements IConstants {

    private Logger logger = Logger.getLogger(this.getClass());
    private static IDataBase dataBase;
    private static Calculator calculator;

    @BeforeClass
    public static void beforeClass() {
        ATest.beforeClass();
        String dataBaseFile = "./src/test/resources/isearch/serenity.odb";
        dataBase = getDataBase(DataBaseOdb.class, dataBaseFile, Boolean.FALSE, mockInternalDataBase);
        calculator = new Calculator();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void averageMethodNetTime() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                double averageMethodNetTime = calculator.averageMethodNetTime(method);
                logger.debug("Average net method time : method : " + method.getName() + " - " + averageMethodNetTime);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void averageMethodTime() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                double averageMethodTime = calculator.averageMethodTime(method);
                logger.debug("Average method : method : " + method.getName() + " - " + averageMethodTime);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void methodChange() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                double methodChange = calculator.methodChange(method);
                logger.debug("Method change : method : " + method.getName() + " - " + methodChange);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void methodChangeSeries() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                List<Double> series = calculator.methodChangeSeries(method);
                logger.debug("Method change series : " + method.getName() + " - " + series);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void methodNetChange() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                double methodNetChange = calculator.methodNetChange(method);
                logger.debug("Method net change : method : " + method.getName() + " - " + methodNetChange);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void methodNetChangeSeries() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                List<Double> methodNetChangeSeries = calculator.methodNetChangeSeries(method);
                logger.debug("Method net change series : method : " + method.getName() + " - " + methodNetChangeSeries);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void methodNetSeries() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                List<Double> methodNetSeries = calculator.methodNetSeries(method);
                logger.debug("Method net series : method : " + method.getName() + " - " + methodNetSeries);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void methodSeries() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                List<Double> series = calculator.methodSeries(method);
                logger.debug("Method series : " + method.getName() + " - " + series);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void totalMethodTime() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                double totalMethodTime = calculator.totalMethodTime(method);
                logger.debug("Total method time : method : " + method.getName() + " - " + totalMethodTime);
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void totalNetMethodTime() {
        List<Class> classes = dataBase.find(Class.class);
        for (Class klass : classes) {
            List<Method<?, ?>> methods = klass.getChildren();
            for (Method<?, ?> method : methods) {
                double totalNetMethodTime = calculator.totalNetMethodTime(method);
                logger.debug("Total net method time : method : " + method.getName() + " - " + totalNetMethodTime);
            }
        }
    }

    @Test
    public void collectStart() {
        Executer.execute(new Executer.IPerform() {
            @Override
            public void execute() {
                Profiler.collectStart(className, methodName, methodDescription);
            }
        }, "Profiler collect start : ", 10000);
    }

}