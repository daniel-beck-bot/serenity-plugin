package com.ikokoon.serenity.process.aggregator;

import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.*;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 07.03.10
 */
public abstract class AAggregator implements IAggregator {

    private static final int PRECISION = 2;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected IDataBase dataBase;

    public AAggregator(IDataBase dataBase) {
        this.dataBase = dataBase;
    }

    /**
     * Returns a list of lines in the packages, i.e. all the lines in the packages.
     *
     * @param pakkages bla...
     * @return bla...
     */
    @SuppressWarnings("unchecked")
    protected List<Line<?, ?>> getLines(final List<Package> pakkages) {
        List<Line<?, ?>> projectLines = new ArrayList<>();
        for (final Package<?, ?> pakkage : pakkages) {
            projectLines.addAll(getLines(pakkage));
        }
        return projectLines;
    }

    /**
     * Returns a list of lines in the package.
     *
     * @param pakkage bla...
     * @return bla...
     */
    @SuppressWarnings("unchecked")
    protected List<Line<?, ?>> getLines(final Package<?, ?> pakkage) {
        List<Line<?, ?>> packageLines = new ArrayList<>();
        for (final Class<?, ?> klass : pakkage.getChildren()) {
            for (final Method<Class, Line> method : klass.getChildren()) {
                for (final Line line : method.getChildren()) {
                    packageLines.add(line);
                }
            }
        }
        return packageLines;
    }

    /**
     * Returns a list of lines in the class.
     *
     * @param klass bla...
     * @param lines bla...
     * @return bla...
     */
    @SuppressWarnings("unchecked")
    protected List<Line<?, ?>> getLines(final Class<Package, Method> klass, List<Line<?, ?>> lines) {
        for (final Class<Package, Method> innerKlass : klass.getInnerClasses()) {
            getLines(innerKlass, lines);
        }
        for (final Method<Class, Line> method : klass.getChildren()) {
            for (final Line<?, ?> line : method.getChildren()) {
                if (!containsLine(lines, line)) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    /**
     * Returns true if the specified set contains the line.
     *
     * @param lines bla...
     * @param line  bla...
     * @return bla...
     */
    private boolean containsLine(List<Line<?, ?>> lines, Line<?, ?> line) {
        for (Line<?, ?> setLine : lines) {
            if (setLine.getNumber() == line.getNumber()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of methods that are in the packages, i.e. all the methods in the package.
     *
     * @param pakkages bla...
     * @return bla...
     */
    @SuppressWarnings("unchecked")
    List<Method<?, ?>> getMethods(final Collection<Package> pakkages) {
        List<Method<?, ?>> projectMethods = new ArrayList<>();
        for (final Package<?, ?> pakkage : pakkages) {
            projectMethods.addAll(getMethods(pakkage));
        }
        return projectMethods;
    }

    @SuppressWarnings("unchecked")
    List<Method<?, ?>> getMethods(final Package<?, ?> pakkage) {
        List<Method<?, ?>> packageMethods = new ArrayList();
        for (final Class<?, ?> klass : pakkage.getChildren()) {
            List<Method<?, ?>> methods = new ArrayList();
            getMethods(klass, methods);
            packageMethods.addAll(methods);
        }
        return packageMethods;
    }

    List<Method<?, ?>> getMethods(final Class<?, ?> klass, final List<Method<?, ?>> methods) {
        for (final Class<?, ?> innerKlass : klass.getInnerClasses()) {
            getMethods(innerKlass, methods);
        }
        for (final Method<?, ?> method : klass.getChildren()) {
            methods.add(method);
        }
        return methods;
    }

    void setPrecision(final Composite<?, ?> composite) {
        Field[] fields = composite.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (double.class.isAssignableFrom(field.getType()) || Double.class.isAssignableFrom(field.getDeclaringClass())) {
                try {
                    field.setAccessible(true);
                    double value = field.getDouble(composite);
                    value = Toolkit.format(value, PRECISION);
                    field.setDouble(composite, value);
                } catch (Exception e) {
                    logger.error("Exception accessing the field : " + field, e);
                }
            }
        }
    }

    /**
     * Distance from the Main Sequence (D): The perpendicular distance of a package from the idealised line A + I = 1. This
     * metric is an indicator of the package's balance between abstractness and stability. A package squarely on the main sequence
     * is optimally balanced with respect to its abstractness and stability. Ideal packages are either completely abstract and stable
     * (x=0, y=1) or completely concrete and unstable (x=1, y=0). The range for this metric is 0 to 1, with D=0 indicating a package
     * that is coincident with the main sequence and D=1 indicating a package that is as far from the main sequence as possible.
     * <p>
     * 1) u = (x3 - x1)(x2 - x1) + (y3 - y1)(y2 - y1) / ||p2 - p1||² <br>
     * 2) y = mx + c, 0 = ax + by + c, d = |am + bn + c| / sqrt(a² + b²) : d= |-stability + -abstractness + 1| / sqrt(-1² + -1²)
     *
     * @param stability    bla...
     * @param abstractness bla...
     * @return bla...
     */
    @SuppressWarnings("WeakerAccess")
    public static double getDistance(double stability, double abstractness) {
        double a = -1, b = -1;
        return Math.abs(-stability + -abstractness + 1) / Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    /**
     * Abstractness (A): The ratio of the number of abstract classes (and interfaces) in the analyzed package to the total
     * number of classes in the analyzed package. The range for this metric is 0 to 1, with A=0 indicating a completely concrete
     * package and A=1 indicating a completely abstract package.
     *
     * @param interfaces      bla...
     * @param implementations bla...
     * @return bla...
     */
    public static double getAbstractness(double interfaces, double implementations) {
        return (interfaces + implementations) > 0 ? interfaces / (interfaces + implementations) : 0d;
    }

    /**
     * Instability (I): The ratio of efferent coupling (Ce) to total coupling (Ce + Ca) such that I = Ce / (Ce + Ca). This
     * metric is an indicator of the package's resilience to change. The range for this metric is 0 to 1, with I=0 indicating a
     * completely stable package and I=1 indicating a completely
     * unstable package.
     *
     * @param efferent bla...
     * @param afferent bla...
     * @return bla...
     */
    public static double getStability(double efferent, double afferent) {
        double denominator = efferent + afferent;
        return denominator > 0 ? efferent / denominator : 0d;
    }

    /**
     * Calculates the complexity for a class.
     *
     * @param methods         bla...
     * @param totalComplexity bla...
     * @return bla...
     */
    public static double getComplexity(double methods, double totalComplexity) {
        double complexity = methods > 0 ? totalComplexity / methods : 1;
        return Math.max(1, complexity);
    }

    /**
     * Calculates the coverage for a method, class or package.
     *
     * @param lines    bla...
     * @param executed bla...
     * @return bla...
     */
    public static double getCoverage(double lines, double executed) {
        return lines > 0 ? (executed / lines) * 100d : 0;
    }
}
