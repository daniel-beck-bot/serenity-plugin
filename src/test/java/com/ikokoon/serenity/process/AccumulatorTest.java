package com.ikokoon.serenity.process;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.target.Target;
import com.ikokoon.toolkit.Toolkit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import static org.junit.Assert.*;

/**
 * This is the test for the accumulator the looks through all the classes on the classpath that were not loaded at runtime and does the dependency, coverage and
 * so on for them.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 24.07.09
 */
public class AccumulatorTest extends ATest implements IConstants {

    private Accumulator accumulator;

    @Before
    public void before() {
        String classPath = System.getProperty("java.class.path");
        classPath += ";" + new File(".", "/target/serenity.jar").getAbsolutePath() + ";";

        classPath = Toolkit.replaceAll(classPath, "\\.\\", "\\");
        classPath = Toolkit.replaceAll(classPath, "/./", "/");
        System.setProperty("java.class.path", classPath);

        accumulator = new Accumulator(null);
    }

    @After
    public void after() {
        // Toolkit.deleteFile(new File("./serenity"), 3);
    }

    @Test
    public void accumulate() {
        accumulator.execute();
        Class<?, ?> targetClass = (Class<?, ?>) dataBase.find(Class.class, Toolkit.hash(Target.class.getName()));
        assertNotNull(targetClass);
        Class<?, ?> targetConsumerClass = (Class<?, ?>) dataBase.find(Class.class, Toolkit.hash(Class.class.getName()));
        assertNull(targetConsumerClass);

        // Verify that the Java.java has been accumulated
        Class<?, ?> javaClass = dataBase.find(Class.class, Toolkit.hash("com.ikokoon.Java"));
        assertNotNull(javaClass);
        List<File> files = new ArrayList<>();
        Toolkit.findFiles(new File("."), new Toolkit.IFileFilter() {
            public boolean matches(final File file) {
                return file.getName().endsWith("Java.html");
            }
        }, files);
        assertEquals("The source for the Java.java class should have been persisted : ", 1, files.size());
    }

    @Test
    public void getSource() throws Exception {
        JarFile jarFile = new JarFile(new File("src/test/resources/serenity.jar"));
        String source = accumulator.getSource(jarFile, Toolkit.dotToSlash(Accumulator.class.getName()) + ".java").toString();
        assertNotNull(source);
    }

    @Test
    public void excluded() {
        Configuration.getConfiguration().getExcludedPackages().clear();
        Configuration.getConfiguration().getIncludedPackages().clear();

        Configuration.getConfiguration().getIncludedPackages().add("ikube");

        Configuration.getConfiguration().getExcludedPackages().add("model");
        Configuration.getConfiguration().getExcludedPackages().add("Mock");
        Configuration.getConfiguration().getExcludedPackages().add("Test");
        Configuration.getConfiguration().getExcludedPackages().add("Integration");


        boolean excluded = accumulator.isExcluded(".root..jenkins.jobs.ikube.workspace.code.com.src.main.java.ikube.toolkit.ObjectToolkit.java");
        assertFalse(excluded);
        excluded = accumulator
                .isExcluded(".usr.share.eclipse.workspace.serenity.work.workspace.ikube.code.core.serenity.ikube.action.index.handler.IIndexableHandler.class");
        assertFalse(excluded);
    }

}