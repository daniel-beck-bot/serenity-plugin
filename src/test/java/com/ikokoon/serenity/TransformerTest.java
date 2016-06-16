package com.ikokoon.serenity;

import com.ikokoon.serenity.instrumentation.coverage.CoverageClassAdapterChecker;
import com.ikokoon.serenity.instrumentation.coverage.CoverageMethodAdapterChecker;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.target.Target;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import static org.junit.Assert.*;

/**
 * This class tests that the transformer adds the Collector instructions to the class byte code.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12.07.09
 */
public class TransformerTest extends ATest {

    private Configuration configuration;
    private Instrumentation instrumentation = null;
    private ProtectionDomain protectionDomain = null;

    @Before
    public void before() {
        // Call the pre-main to load stuff we need
        Transformer.premain(null, instrumentation);
        Transformer.removeShutdownHook();
        configuration = Configuration.getConfiguration();
    }

    @After
    public void after() {
        DataBaseToolkit.clear(dataBase);
    }

    @Test
    public void transform() throws Exception {
        byte[] classBytes = getClassBytes(className);
        byte[] sourceBytes = getSourceBytes(className);
        Exception exception = null;
        try {
            visitClass(CoverageMethodAdapterChecker.class, className, classBytes, sourceBytes);
        } catch (Exception e) {
            exception = e;
        }
        assertNotNull(exception);

        Transformer transformer = new Transformer();
        Class<?> classBeingRedefined = Class.forName(className);
        ClassLoader classLoader = TransformerTest.class.getClassLoader();
        classBytes = transformer.transform(classLoader, className, classBeingRedefined, protectionDomain, classBytes);

        // We need to verify that the collector instructions have been added
        visitClass(CoverageClassAdapterChecker.class, className, classBytes, sourceBytes);
    }

    @Test
    public void excluded() {
        assertTrue(configuration.excluded(Object.class.getName()));
        assertFalse(configuration.excluded(Target.class.getName()));
    }

    @Test
    public void included() {
        assertTrue(configuration.included(Target.class.getName()));
        assertFalse(configuration.included(Object.class.getName()));
    }

}