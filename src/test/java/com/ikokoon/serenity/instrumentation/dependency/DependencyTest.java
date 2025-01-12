package com.ikokoon.serenity.instrumentation.dependency;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.model.*;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.target.TargetAccess;
import com.ikokoon.target.consumer.Annotation;
import com.ikokoon.target.consumer.TargetConsumer;
import com.ikokoon.toolkit.Toolkit;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DependencyTest extends ATest {

    // private IDataBase dataBase;

    @Before
    public void clear() {
        // dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, mockInternalDataBase);
        // DataBaseToolkit.clear(dataBase);
        // Collector.initialize(dataBase);
    }

    @Test
    public void visitInner() throws Exception {
        // logger.info("***************************************************");
        // visitClass(DependencyClassAdapter.class, Discovery.class.getName());
        // logger.info("***************************************************");
        // visitClass(DependencyClassAdapter.class, Discovery.InnerClass.class.getName());
        // logger.info("***************************************************");
        // visitClass(DependencyClassAdapter.class, Discovery.InnerClass.InnerInnerClass.class.getName());
    }

    @Test
    public void visit() throws Exception {
        // DataBaseToolkit.clear(dataBase);
        Configuration.getConfiguration().getIncludedPackages().add(className);
        Configuration.getConfiguration().getIncludedPackages().add(packageName);
        Configuration.getConfiguration().getIncludedPackages().add(Logger.class.getPackage().getName());

        visitClass(DependencyClassAdapter.class, className);

        Package<?, ?> pakkage = (Package<?, ?>) dataBase.find(Package.class, Toolkit.hash(packageName));
        assertNotNull(pakkage);
        Class<?, ?> klass = (Class<?, ?>) dataBase.find(Class.class, Toolkit.hash(className));
        assertNotNull(klass);

        Set<Afferent> afferent = klass.getAfferent();
        Set<Efferent> efferent = klass.getEfferent();
        assertTrue(containsAfferentPackage(afferent, Logger.class.getPackage().getName()));
        // TODO: Check why this is failing...
        // assertTrue(containsAfferentPackage(afferent, Serializable.class.getPackage().getName()));
        assertTrue(containsAfferentPackage(afferent, Annotation.class.getPackage().getName()));

        visitClass(DependencyClassAdapter.class, TargetConsumer.class.getName());
        assertTrue(containsEfferentPackage(efferent, TargetConsumer.class.getPackage().getName()));

        dataBase.remove(Package.class, Toolkit.hash(packageName));

        // Test that Annotation has a reference to AnnotationAnnotation and visa versa
        // Test for field annotations and method annotations
        // Test for cyclic references?
    }

    @Test
    public void access() {
        String className = TargetAccess.class.getName();
        visitClass(DependencyClassAdapter.class, className);
        Class<Package, Method<Class, Line>> klass = dataBase.find(Class.class, Toolkit.hash(className));
        assertNotNull(klass);

        List<Class<Package, Method>> innerClasses = klass.getInnerClasses();
        for (Class<?, ?> innerClass : innerClasses) {
            logger.warn("Inner class : " + innerClass.getName() + ", " + innerClass.getAccess());
        }

        List<Method> methods = klass.getChildren();
        for (Method<?, ?> method : methods) {
            logger.warn("Method : " + method.getName() + ", " + method.getAccess());
        }

        // printOpCodes();
    }

    protected void printOpCodes() {
        // <j:set var="publicAccess" value="1" />
        Field[] fields = Opcodes.class.getDeclaredFields();
        Opcodes opcodes = new Opcodes() {
        };
        for (Field field : fields) {
            StringBuilder builder = new StringBuilder("<j:set var=\"");
            builder.append(field.getName());
            builder.append("\" value=\"");
            try {
                field.setAccessible(true);
                builder.append(field.get(opcodes));
            } catch (Exception e) {
                logger.error("", e);
            }
            builder.append("\" />");

            System.out.println(builder);
        }
    }

    private boolean containsAfferentPackage(Set<Afferent> afferent, String name) {
        for (Afferent aff : afferent) {
            logger.debug("Afferent : " + aff);
            if (aff.getName().indexOf(name) > -1) {
                return true;
            }
        }
        return false;
    }

    private boolean containsEfferentPackage(Set<Efferent> efferent, String name) {
        for (Efferent eff : efferent) {
            if (eff.getName().indexOf(name) > -1) {
                return true;
            }
        }
        return false;
    }
}
