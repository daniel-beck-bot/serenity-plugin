package com.ikokoon.serenity.process;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.instrumentation.dependency.DependencyClassAdapter;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.*;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.serenity.process.aggregator.MethodAggregator;
import com.ikokoon.serenity.process.aggregator.PackageAggregator;
import com.ikokoon.serenity.process.aggregator.ProjectAggregator;
import com.ikokoon.target.discovery.Discovery;
import com.ikokoon.target.discovery.IDiscovery;
import com.ikokoon.toolkit.Executer;
import com.ikokoon.toolkit.Toolkit;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.ikokoon.serenity.persistence.IDataBase.DataBaseManager.getDataBase;
import static org.junit.Assert.*;

/**
 * This is the test for the aggregator. The aggregator takes the collected data on the methods, classes and packages and calculates the metrics like the
 * abstractness the stability and so on.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 02.08.09
 */
public class AggregatorTest extends ATest implements IConstants {

    @Before
    public void before() {
        dataBase = getDataBase(DataBaseOdb.class,
                "./src/test/resources/deploy/serenity.odb",
                Boolean.FALSE,
                null);
    }

    @Test
    public void aggregateClass() throws Exception {
        Class<?, ?> klass = dataBase.find(Class.class, 1953713242666473466L);
        assertEquals(getComplexity(klass), klass.getComplexity(), 0);
        assertEquals(getCoverage(klass), klass.getCoverage(), 5);
        assertEquals(getStability(klass), klass.getStability(), 0.1);
    }

    @Test
    public void aggregate() {
        double aggregationDuration = Executer.execute(new Executer.IPerform() {
            public void execute() {
                File odbDataBaseFile = new File("./src/test/resources/findbugs/serenity.odb");

                IDataBase odbDataBase = getDataBase(DataBaseOdb.class, odbDataBaseFile.getAbsolutePath(), Boolean.FALSE, null);
                IDataBase ramDataBase = getDataBase(DataBaseRam.class, "ramDatabase.ram", Boolean.FALSE, odbDataBase);

                new Aggregator(null, ramDataBase).execute();

                Project<?, ?> project = (Project<?, ?>) ramDataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
                if (project != null) {
                    logger.warn(ToStringBuilder.reflectionToString(project));
                } else {
                    logger.warn("Project null : ");
                }

                ramDataBase.close();

                odbDataBase = getDataBase(DataBaseOdb.class, odbDataBaseFile.getAbsolutePath(), Boolean.FALSE, mockInternalDataBase);
                project = (Project<?, ?>) odbDataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
                logger.warn(ToStringBuilder.reflectionToString(project));

                odbDataBase.close();
            }
        }, "AggregatorTest : ", 1);
        logger.warn("Aggregation took : " + aggregationDuration);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void innerClasses() {
        dataBase = getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, Boolean.FALSE, mockInternalDataBase);

        visitClass(DependencyClassAdapter.class, IDiscovery.class.getName());
        visitClass(DependencyClassAdapter.class, Discovery.class.getName());

        Project project = new Project();
        project.setTimestamp(new Date());
        dataBase.persist(project);

        Package<?, ?> pakkage = dataBase.find(Package.class, Toolkit.hash(Discovery.class.getPackage().getName()));
        assertNotNull(pakkage);
        Class<?, ?> klass = dataBase.find(Class.class, Toolkit.hash(Discovery.class.getName()));
        assertNotNull(klass);
        Method<?, ?> method = dataBase.find(Method.class, Toolkit.hash(klass.getName(), "getAnonymousInnerClass", "()V"));
        assertNotNull(method);

        Aggregator aggregator = new Aggregator(null, dataBase);
        aggregator.execute();

        assertEquals(0d, method.getCoverage(), 0);
        assertEquals(3, method.getChildren().size());

        // Check the class
        assertEquals(4, klass.getChildren().size()); // 10 + 15
        // Sigma n=1, n, (method lines / class lines) * method complexity
        // ((10 / 15) * 10) + ((5 / 15) * 20) = 6.666r + 6.6666r = 13.3333r
        assertEquals(1d, klass.getComplexity(), 0);
        // ((10 / 15) * 20) + ((5 / 15) * 40) = 13.33r + 13.333r =
        assertEquals(0d, klass.getCoverage(), 0);
        // e / e + a = 2 / 2 + 1 = 0.666r
        assertEquals(0d, klass.getStability(), 0);
        assertEquals(0d, klass.getEfference(), 0);
        assertEquals(0d, klass.getAfference(), 0);
        assertEquals(false, klass.getInterfaze());

        // assertEquals(22d, pakkage.getLines());
        // Sigma : (class lines / package lines) * class complexity
        // ((15 / 65) * 13.333333333333332) + ((50 / 65) * 25) = 3.07692 + 19.2307 = 22.30692307692308
        assertEquals(1d, pakkage.getComplexity(), 0);
        // ((15 / 65) * 26.666666666666664) + ((50 / 65) * 7.996) = 6.1538 + 6.5107 = 12.298461538461538
        assertEquals(0d, pakkage.getCoverage(), 0);
        // i / (i + im) = 1 / 2 = 0.5
        assertEquals(0.16d, pakkage.getAbstractness(), 0);
        // e / (e + a) = 3 / 5 = 0.6666666666666666
        assertEquals(0d, pakkage.getStability(), 0);
        // d=|-stability + -abstractness + 1|/sqrt(-1²+-1²) = |-0.6666666666666666 + -0.5 + 1|sqrt(-1sq + -1sq) =
        assertEquals(0.58d, pakkage.getDistance(), 0);
        assertEquals(1d, pakkage.getInterfaces(), 0);
        assertEquals(5d, pakkage.getImplementations(), 0);
        assertEquals(0d, pakkage.getEfference(), 0);
        assertEquals(0d, pakkage.getAfference(), 0);
        assertEquals(6, pakkage.getChildren().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void aggregateMethods() throws Exception {
        dataBase = getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, Boolean.FALSE, mockInternalDataBase);

        visitClass(DependencyClassAdapter.class, IDiscovery.class.getName());
        visitClass(DependencyClassAdapter.class, Discovery.class.getName());

        Package<?, ?> pakkage = dataBase.find(Package.class, Toolkit.hash(Discovery.class.getPackage().getName()));

        Set<Class<?, ?>> classes = new TreeSet<>();
        Set<Method<?, ?>> methods = new TreeSet<>();
        Set<Line<?, ?>> lines = new TreeSet<>();
        getClassesMethodsAndLines(pakkage, classes, methods, lines);

        for (Method method : methods) {
            new MethodAggregator(dataBase, method).aggregate();
            double executed = getExecuted(method);
            double coverage = (executed / (double) method.getChildren().size()) * 100d;
            assertEquals(Math.round(coverage), Math.round(method.getCoverage()));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void aggregatePackage() throws Exception {
        Package pakkage = dataBase.find(Package.class, 31165058218L);

        new PackageAggregator(dataBase, pakkage).aggregate();

        assertEquals(getAbstractness(pakkage), pakkage.getAbstractness(), 0.01);
        assertEquals(getComplexity(pakkage), pakkage.getComplexity(), 0.5);
        assertEquals(getCoverage(pakkage), pakkage.getCoverage(), 0.1);
        assertEquals(getDistance(pakkage), pakkage.getDistance(), 0.1);
        assertEquals(getStability(pakkage), pakkage.getStability(), 0.5);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void aggregateProject() throws Exception {
        // DataBaseToolkit.dump(dataBase, null, "Database dump : ");

        Project project = dataBase.find(Project.class, 8720669836632739014L);

        new ProjectAggregator(dataBase).aggregate();

        assertTrue(project.getAbstractness() != 0);
        assertTrue(project.getClasses() != 0);
        assertTrue(project.getComplexity() != 0);
        // assertTrue(project.getCoverage() != 0);
        assertTrue(project.getDistance() != 0);
        assertTrue(project.getLines() != 0);
        assertTrue(project.getMethods() != 0);
        assertTrue(project.getPackages() != 0);
        assertEquals(0.14, project.getStability(), 0.1);
    }

    private double getStability(Package<?, ?> pakkage) {
        double efferent = 0d;
        double afferent = 0d;
        for (Class<?, ?> klass : pakkage.getChildren()) {
            efferent += klass.getEfferent().size();
            afferent = klass.getAfferent().size();
        }
        double denominator = efferent + afferent;
        return denominator > 0 ? efferent / denominator : 1d;
    }

    private double getDistance(Package<?, ?> pakkage) {
        // TODO - validate this test - A + I = 1
        double a = -1, b = -1;
        return Math.abs(-pakkage.getStability() + -pakkage.getAbstractness() + 1) / Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    @SuppressWarnings("unchecked")
    private double getCoverage(Package<?, ?> pakkage) {
        double totalLines = 0;
        double linesExecuted = 0;
        for (Class<?, ?> klass : pakkage.getChildren()) {
            for (final Method<Class, Line> method : klass.getChildren()) {
                for (final Line line : method.getChildren()) {
                    totalLines++;
                    if (line.getCounter() > 0) {
                        linesExecuted++;
                    }
                }
            }
        }
        if (totalLines == 0) {
            return 0;
        }
        return (linesExecuted / totalLines) * 100D;
    }

    private double getComplexity(Package<?, ?> pakkage) {
        double complexity = 0d;
        for (Class<?, ?> klass : pakkage.getChildren()) {
            complexity += klass.getComplexity();
        }
        complexity = complexity / pakkage.getChildren().size();
        return complexity;
    }

    private double getAbstractness(Package<?, ?> pakkage) {
        return pakkage.getInterfaces() / (pakkage.getImplementations() + pakkage.getInterfaces());
    }

    private double getStability(final Class<?, ?> klass) {
        double efferent = klass.getEfferent().size();
        double afferent = klass.getEfferent().size() + klass.getAfferent().size();
        return (efferent + afferent) > 0 ? efferent / (efferent + afferent) : 0d;
    }

    private double getCoverage(final Class<?, ?> klass) {
        double coverage = 0d;
        for (Method<?, ?> method : klass.getChildren()) {
            coverage += method.getCoverage();
        }
        return coverage / (double) klass.getChildren().size();
    }

    private double getComplexity(final Class<?, ?> klass) {
        double complexity = 0d;
        for (Method<?, ?> method : klass.getChildren()) {
            complexity += method.getComplexity();
        }
        complexity = complexity / klass.getChildren().size();
        return Math.max(1, complexity);
    }

    @SuppressWarnings("unchecked")
    private void getClassesMethodsAndLines(final Package<?, ?> pakkage, final Set<Class<?, ?>> classes,
                                           final Set<Method<?, ?>> methods, final Set<Line<?, ?>> lines) {
        for (Class<Package, Method> klass : pakkage.getChildren()) {
            classes.add(klass);
            for (Method<Class, Line> method : klass.getChildren()) {
                methods.add(method);
                for (Line line : method.getChildren()) {
                    lines.add(line);
                }
            }
        }
    }

    private double getExecuted(final Method<Class, Line> method) {
        double executed = 0d;
        for (Line<?, ?> line : method.getChildren()) {
            if (line.getCounter() > 0d) {
                executed++;
            }
        }
        return executed;
    }

}