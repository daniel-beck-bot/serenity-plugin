package com.ikokoon.serenity;

import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.serenity.instrumentation.profiling.ProfilingClassAdviceAdapter;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.serenity.process.*;
import com.ikokoon.toolkit.LoggingConfigurator;
import com.ikokoon.toolkit.Toolkit;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Date;
import java.util.List;

import static com.ikokoon.serenity.persistence.IDataBase.DataBaseManager.*;

/**
 * This class is the entry point for the Serenity code coverage/complexity/dependency/profiling functionality. This
 * class is called by the JVM on startup. The agent then has first access to the byte code for all classes that are loaded.
 * During this loading the byte code can be enhanced.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12-07-2009
 */
public class Transformer implements ClassFileTransformer, IConstants {

    /**
     * The LOGGER.
     */
    private static Logger LOGGER;
    /**
     * During tests there can be more than one shutdown hook added.
     */
    private static boolean INITIALISED = false;
    /**
     * The chain of adapters for analysing the classes.
     */
    private static Class<ClassVisitor>[] CLASS_ADAPTER_CLASSES;
    /**
     * The shutdown hook will clean, accumulate and aggregate the data.
     */
    private static Thread SHUTDOWN_HOOK;

    /**
     * This method is called by the JVM at startup. This method will only be called if the command line for starting the
     * JVM has the following on it: -javaagent:serenity/serenity.jar. This instruction tells the JVM that there is an agent
     * that must be used. In the META-INF directory of the jar specified there must be a MANIFEST.MF file. In this file the
     * instructions must be something like the following:
     * <p>
     * Manifest-Version: 1.0 <br>
     * Boot-Class-Path: asm-3.1.jar and so on..., in the case that the required libraries are not on the classpath, which
     * they should be<br> Premain-Class: com.ikokoon.serenity.Transformer
     * <p>
     * Another line in the manifest can start an agent after the JVM has been started, but not for all JVMs. So not very useful.
     * <p>
     * These instructions tell the JVM to call this method when loading class files.
     *
     * @param args            a set of arguments that the JVM will call the method with
     * @param instrumentation the instrumentation implementation of the JVM
     */
    @SuppressWarnings("unchecked")
    public static void premain(final String args, final Instrumentation instrumentation) {
        if (!INITIALISED) {
            INITIALISED = true;
            LoggingConfigurator.configure();
            LOGGER = LoggerFactory.getLogger(Transformer.class);

            Configuration configuration = Configuration.getConfiguration();

            List<Class<ClassVisitor>> classAdapters = configuration.getClassAdapters();
            CLASS_ADAPTER_CLASSES = classAdapters.toArray(new Class[classAdapters.size()]);
            LOGGER.info("Starting Serenity : ");
            if (instrumentation != null) {
                instrumentation.addTransformer(new Transformer());
            }
            String cleanClasses = configuration.getProperty(IConstants.CLEAN_CLASSES);
            if (cleanClasses != null && cleanClasses.equals(Boolean.TRUE.toString())) {
                File serenityDirectory = new File(IConstants.SERENITY_DIRECTORY);
                Toolkit.deleteFiles(serenityDirectory, ".class");
                if (!serenityDirectory.exists()) {
                    if (!serenityDirectory.mkdirs()) {
                        LOGGER.warn("Didn't re-create Serenity directory : " + serenityDirectory.getAbsolutePath());
                    }
                }
            }
            String deleteDatabaseFile = configuration.getProperty(IConstants.DELETE);
            File file = new File(IConstants.DATABASE_FILE_ODB);
            if (deleteDatabaseFile == null || "true".equals(deleteDatabaseFile)) {
                LOGGER.info("Deleting database file : " + file.getAbsolutePath());
                Toolkit.deleteFile(file, 3);
            } else {
                LOGGER.info("Not deleting database file : " + file.getAbsolutePath());
            }

            // This is the underlying database that will persist the data to the file system
            IDataBase odbDataBase = getDataBase(DataBaseOdb.class, IConstants.DATABASE_FILE_ODB, Boolean.TRUE, null);
            // This is the ram database that will hold all the data in memory for better performance
            IDataBase ramDataBase = getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, Boolean.FALSE, odbDataBase);
            Collector.initialize(ramDataBase);
            Profiler.initialize(ramDataBase);
            new Listener(null, ramDataBase).execute();
            addShutdownHook(ramDataBase);
            LOGGER.info("Finished initializing Serenity : ");
        }
    }

    /**
     * This method adds the shutdown hook that will clean and accumulate the data when the Jvm shuts down.
     *
     * @param dataBase the database to get the data from
     */
    private static void addShutdownHook(final IDataBase dataBase) {
        SHUTDOWN_HOOK = new Thread() {
            public void run() {
                Date start = new Date();
                LOGGER.info("Starting accumulation : " + start);

                long processStart = System.currentTimeMillis();
                new Accumulator(null).execute();
                LOGGER.info("Accumlulator : " + (System.currentTimeMillis() - processStart));

                processStart = System.currentTimeMillis();
                new Cleaner(null, dataBase).execute();
                LOGGER.info("Cleaner : " + (System.currentTimeMillis() - processStart));

                processStart = System.currentTimeMillis();
                new Aggregator(null, dataBase).execute();
                LOGGER.info("Aggregator : " + (System.currentTimeMillis() - processStart));

                processStart = System.currentTimeMillis();
                new Reporter(null, dataBase).execute();
                LOGGER.info("Reporter : " + (System.currentTimeMillis() - processStart));

                String dumpData = Configuration.getConfiguration().getProperty(IConstants.DUMP);
                LOGGER.info("Dump data : " + dumpData + ", " + System.getProperties());
                if (dumpData != null && "true".equals(dumpData.trim())) {
                    DataBaseToolkit.dump(dataBase, null, null);
                }

                processStart = System.currentTimeMillis();
                dataBase.close();
                LOGGER.info("Close database : " + (System.currentTimeMillis() - processStart));

                Date end = new Date();
                long million = 1000 * 1000;
                long duration = end.getTime() - start.getTime();
                LOGGER.info("Finished accumulation : " + end + ", duration : " + duration + " millis");
                LOGGER.info("Total memory : " + (Runtime.getRuntime().totalMemory() / million) + ", max memory : "
                        + (Runtime.getRuntime().maxMemory() / million) + ", free memory : " + (Runtime.getRuntime().freeMemory() / million));
            }
        };
        Runtime.getRuntime().addShutdownHook(SHUTDOWN_HOOK);
    }

    protected static void removeShutdownHook() {
        Runtime.getRuntime().removeShutdownHook(SHUTDOWN_HOOK);
    }

    /**
     * This method transforms the classes that are specified.
     */
    public byte[] transform(
            final ClassLoader loader,
            final String className,
            final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain,
            final byte[] classBytes)
            throws IllegalClassFormatException {
        Configuration configuration = Configuration.getConfiguration();
        List<Class<ClassVisitor>> classAdapters = configuration.getClassAdapters();
        boolean profiling = Boolean.FALSE;
        try {
            @SuppressWarnings("unchecked")
            Class<ClassVisitor> classVisitorClass = (Class<ClassVisitor>) Class.forName(ProfilingClassAdviceAdapter.class.getName());
            profiling = classAdapters.contains(classVisitorClass);
        } catch (final ClassNotFoundException e) {
            LOGGER.error("Error getting profiling class : " + className);
        }
        // If we are profiling then we enhance every class
        if ((profiling || configuration.included(className)) && !configuration.excluded(className)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Enhancing class : " + className);
            }
            ByteArrayOutputStream source = new ByteArrayOutputStream(0);
            ClassWriter writer = (ClassWriter) VisitorFactory.getClassVisitor(CLASS_ADAPTER_CLASSES, className, classBytes, source);
            byte[] enhancedClassBytes = writer.toByteArray();
            String writeClasses = configuration.getProperty(IConstants.WRITE_CLASSES);
            if (writeClasses != null && writeClasses.equals(Boolean.TRUE.toString())) {
                writeClass(className, enhancedClassBytes);
            }
            // Return the injected bytes for the class, i.e. with the coverage instructions
            return enhancedClassBytes;
        } else {
            LOGGER.debug("Class not included : " + className);
        }
        // Return the original bytes for the class
        return classBytes;
    }

    /**
     * This method writes the transformed classes to the file system so they can be viewed later.
     *
     * @param className  the name of the class file
     * @param classBytes the bytes of byte code to write
     */
    private void writeClass(final String className, final byte[] classBytes) {
        // Write the class so we can check it with JD decompiler visually
        String directoryPath = Toolkit.dotToSlash(Toolkit.classNameToPackageName(className));
        String fileName = className.replaceFirst(Toolkit.classNameToPackageName(className), "") + ".class";
        File directory = new File(IConstants.SERENITY_DIRECTORY + File.separator + directoryPath);
        if (!directory.exists()) {
            boolean mkDirs = directory.mkdirs();
            if (!mkDirs) {
                LOGGER.warn("Couldn't make directory to write out injected classes : " + directory.getAbsolutePath());
            }
        }
        File file = new File(directory, fileName);
        Toolkit.setContents(file, classBytes);
    }

}