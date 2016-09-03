package com.ikokoon.serenity.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 12.08.09
 */
@SuppressWarnings("unused")
@Unique(fields = {Composite.NAME})
public class Class<E, F> extends Stability<Package, Method> implements Comparable<Class<?, ?>>, Serializable {

    private int access;
    private String name;
    private Class<Package, Method> outerClass;
    private Method<Class, Line> outerMethod;
    private List<Class<Package, Method>> innerClasses;

    private double coverage;
    private double complexity;

    private boolean interfaze;

    private double allocations;

    private List<Snapshot<?, ?>> snapshots;

    private String source;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAccess() {
        return access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

    public Class<?, ?> getOuterClass() {
        return outerClass;
    }

    public void setOuterClass(Class<Package, Method> outerClass) {
        this.outerClass = outerClass;
    }

    public Method<?, ?> getOuterMethod() {
        return outerMethod;
    }

    public void setOuterMethod(Method<Class, Line> outerMethod) {
        this.outerMethod = outerMethod;
    }

    public List<Class<Package, Method>> getInnerClasses() {
        if (innerClasses == null) {
            innerClasses = new ArrayList<>();
        }
        return innerClasses;
    }

    public void setInnerClasses(List<Class<Package, Method>> innerClasses) {
        this.innerClasses = innerClasses;
    }

    public double getComplexity() {
        return complexity;
    }

    public void setComplexity(double complexity) {
        this.complexity = complexity;
    }

    public double getCoverage() {
        return coverage;
    }

    public void setCoverage(double coverage) {
        this.coverage = coverage;
    }

    public boolean getInterfaze() {
        return interfaze;
    }

    public void setInterfaze(boolean interfaze) {
        this.interfaze = interfaze;
    }

    public double getAllocations() {
        return allocations;
    }

    public void setAllocations(double allocations) {
        this.allocations = allocations;
    }

    public List<Snapshot<?, ?>> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(List<Snapshot<?, ?>> snapshots) {
        this.snapshots = snapshots;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String toString() {
        return getId() + ":" + name;
    }

    @SuppressWarnings("NullableProblems")
    public int compareTo(final Class<?, ?> o) {
        int comparison = 0;
        if (this.getId() != null && o.getId() != null) {
            comparison = this.getId().compareTo(o.getId());
        } else {
            if (this.getName() != null && o.getName() != null) {
                comparison = this.getName().compareTo(o.getName());
            }
        }
        return comparison;
    }

}