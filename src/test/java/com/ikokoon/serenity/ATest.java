package com.ikokoon.serenity;

import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.serenity.model.*;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.target.Target;
import com.ikokoon.toolkit.LoggingConfigurator;
import com.ikokoon.toolkit.Toolkit;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static com.ikokoon.serenity.persistence.IDataBase.DataBaseManager.getDataBase;
import static org.mockito.Mockito.mock;

/**
 * Base class for the tests.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 30.07.09
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public abstract class ATest implements IConstants {

    protected static Logger LOGGER;

    protected static IDataBase mockInternalDataBase = mock(IDataBase.class);
    protected static IDataBase dataBase;

    protected Type stringType = Type.getType(String.class);
    protected Type integerType = Type.getType(Integer.class);
    protected Type[] types = new Type[]{stringType, stringType, stringType, integerType, integerType};

    protected String packageName = Target.class.getPackage().getName();
    protected String className = Target.class.getName();
    protected String methodName = "complexMethod";
    protected String methodDescription = Type.getMethodDescriptor(Type.VOID_TYPE, types);
    protected int lineNumber = 70;
    protected double complexity = 10d;
    protected int access = 1537;

    protected String efferentName = "efferentName";
    protected String afferentName = "afferentName";

    @BeforeClass
    public static void beforeClass() {
        LoggingConfigurator.configure();
        LOGGER = Logger.getLogger(ATest.class);
        System.setProperty(IConstants.INCLUDED_ADAPTERS_PROPERTY, "profiling;coverage;complexity;dependency");
        Configuration configuration = Configuration.getConfiguration();
        configuration.getIncludedPackages().add(IConstants.class.getPackage().getName());
        configuration.getIncludedPackages().add(Target.class.getPackage().getName());
        configuration.getIncludedPackages().add(Configuration.class.getPackage().getName());
        configuration.getIncludedPackages().add("com.ikokoon");
        configuration.getExcludedPackages().add(Object.class.getPackage().getName());

        dataBase = getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, Boolean.FALSE, mockInternalDataBase);
        Collector.initialize(dataBase);
    }

    @AfterClass
    public static void afterClass() {
        dataBase.close();
    }

    protected void visitClass(final java.lang.Class<?> visitorClass, final String className) {
        byte[] classBytes = getClassBytes(className);
        byte[] sourceBytes = getSourceBytes(className);
        visitClass(visitorClass, className, classBytes, sourceBytes);
    }

    @SuppressWarnings("unchecked")
    protected ClassWriter visitClass(final java.lang.Class<?> visitorClass, final String className, final byte[] classBytes,
                                     final byte[] sourceBytes) {
        ByteArrayOutputStream source = new ByteArrayOutputStream();
        try {
            source.write(sourceBytes);
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        return (ClassWriter) VisitorFactory.getClassVisitor(new java.lang.Class[]{visitorClass}, className, classBytes, source);
    }

    protected byte[] getClassBytes(final String className) {
        String classPath = Toolkit.dotToSlash(className) + ".class";
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(classPath);
        return Toolkit.getContents(inputStream).toByteArray();
    }

    protected byte[] getSourceBytes(final String className) {
        String classPath = Toolkit.dotToSlash(className) + ".java";
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(classPath);
        return Toolkit.getContents(inputStream).toByteArray();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Package<?, ?> getPackage() {
        Package pakkage = new Package();
        pakkage.setAbstractness(1d);
        pakkage.setAfference(1d);
        pakkage.setChildren(new ArrayList<Class>());
        pakkage.setComplexity(1d);
        pakkage.setCoverage(1d);
        pakkage.setDistance(1d);
        pakkage.setEfference(1d);
        pakkage.setImplementations(1d);
        pakkage.setInterfaces(1d);
        pakkage.setName(packageName);
        pakkage.setStability(1d);
        getClass(pakkage);
        return pakkage;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Class<?, ?> getClass(final Package<?, ?> pakkage) {
        Class klass = new Class();
        klass.setParent(pakkage);
        pakkage.getChildren().add(klass);
        klass.setAfference(1d);

        klass.setComplexity(1d);
        klass.setCoverage(1d);
        klass.setEfference(1d);

        Efferent efferent = new Efferent();
        efferent.setName(efferentName);
        klass.getEfferent().add(efferent);

        Afferent afferent = new Afferent();
        afferent.setName(afferentName);
        klass.getAfferent().add(afferent);

        klass.setInterfaze(false);
        klass.setName(className);
        klass.setStability(1d);
        getMethod(klass);
        return klass;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Method<?, ?> getMethod(final Class<?, ?> klass) {
        Method method = new Method();
        method.setParent(klass);
        method.setClassName(klass.getName());
        klass.getChildren().add(method);
        method.setComplexity(1d);
        method.setCoverage(1d);
        method.setDescription(methodDescription);
        method.setName(methodName);
        getLine(method);
        return method;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Line<?, ?> getLine(final Method method) {
        Line line = new Line();
        line.setCounter(1);
        line.setNumber(lineNumber);
        line.setParent(method);
        line.setMethodName(method.getName());
        line.setClassName(method.getClassName());
        method.getChildren().add(line);
        return line;
    }

}
