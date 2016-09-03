package com.ikokoon.serenity.model;

import java.io.Serializable;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 12-08-2009
 */
@SuppressWarnings("unused")
@Unique(fields = {Composite.NAME})
public class Package<E, F> extends Stability<Project, Class> implements Comparable<Package<?, ?>>, Serializable {

    private String name;

    private double coverage;
    private double complexity;
    private double abstractness;

    private double distance;

    private double lines;
    private double interfaces;
    private double implementations;
    private double executed;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLines() {
        return lines;
    }

    public void setLines(double lines) {
        this.lines = lines;
    }

    public double getExecuted() {
        return executed;
    }

    public void setExecuted(double totalLinesExecuted) {
        this.executed = totalLinesExecuted;
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

    public double getAbstractness() {
        return abstractness;
    }

    public void setAbstractness(double abstractness) {
        this.abstractness = abstractness;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(double interfaces) {
        this.interfaces = interfaces;
    }

    public double getImplementations() {
        return implementations;
    }

    public void setImplementations(double implementations) {
        this.implementations = implementations;
    }

    public String toString() {
        return getId() + ":" + name;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public int compareTo(final Package<?, ?> o) {
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