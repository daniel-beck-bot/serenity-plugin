package com.ikokoon.serenity;

import com.ikokoon.serenity.instrumentation.profiling.ProfilingClassAdviceAdapter;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.process.Calculator;
import com.ikokoon.toolkit.Executer;
import com.ikokoon.toolkit.Toolkit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.util.List;
import java.util.Map;

/**
 * This test needs to have assertions. TODO implement the real tests.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 19-06-2010
 */
public class ProfilerTest extends ATest implements IConstants {

    private Calculator calculator;
    private ClassLoader classLoader;

    @Before
    public void before() {
        System.out.println("before child");
        String dataBaseFile = "./src/test/resources/isearch/serenity.odb";
        // dataBase = getDataBase(DataBaseOdb.class, dataBaseFile, Boolean.FALSE, mockInternalDataBase);
        calculator = new Calculator();
        classLoader = Thread.currentThread().getContextClassLoader();
    }

    @After
    public void after() {
        Thread.currentThread().setContextClassLoader(classLoader);
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
    public void profile() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        // TODO: Implementation details for test
        // 1) Create a class loader that loads the instrumented classes
        ClassLoader classLoader = new ClassLoader() {
            public java.lang.Class<?> loadClass(final String className) throws ClassNotFoundException {
                if (className.equals(ProfilerTest.this.className)) {
                    byte[] classBytes = getClassBytes(ProfilerTest.this.className);
                    ClassReader reader = new ClassReader(classBytes);
                    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
                    ProfilingClassAdviceAdapter profilingClassAdapter = new ProfilingClassAdviceAdapter(writer, className);
                    reader.accept(profilingClassAdapter, ClassReader.EXPAND_FRAMES);
                    final byte[] finalClassBytes = writer.toByteArray();
                    return this.defineClass(className, finalClassBytes, 0, finalClassBytes.length);
                }
                return super.loadClass(className);
            }
        };
        Thread.currentThread().setContextClassLoader(classLoader);

        // 2) Execute the code that contains the instrumented classes a few times
        final Object target = classLoader.loadClass(this.className).newInstance();
        Executer.execute(new Executer.IPerform() {
            @Override
            public void execute() {
                try {
                    Toolkit.executeMethod(target, "complexMethod", new Object[]{"Michael", "Couck", "Programmer", 1000, 1000});
                } catch (Exception e) {
                    logger.error(null, e);
                }
            }
        }, "Profiler test : ", 1000);

        // 3) Verify that the collector collected the class and method times
        for (final Map.Entry<Long, Method> mapEntry : Collector.THREAD_CALL_STACKS.entrySet()) {
            logger.info(mapEntry.getValue());
        }

        // 4) Take a copy of the profiler data
        // 5) Reset the profiler
        // 6) Repeat steps 1-4
        // 7) Compare the first profiler output with the second output
        // 8) Repeat steps 1-7 100 times
        // 9) Verify that there were no exceptions
    }

}