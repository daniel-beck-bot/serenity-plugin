package com.ikokoon.serenity.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 09-12-2009
 */
public abstract class Composite<E, F> {

    public static final String NAME = "name";
    public static final String CLASS_NAME = "className";
    public static final String METHOD_NAME = "methodName";
    public static final String NUMBER = "number";
    public static final String DESCRIPTION = "description";

    private Long id;
    private Composite<E, F> parent;
    private List<F> children = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Composite<E, F> getParent() {
        return parent;
    }

    public void setParent(final Composite<E, F> parent) {
        this.parent = parent;
    }

    public List<F> getChildren() {
        return children;
    }

    public void setChildren(final List<F> children) {
        this.children = children;
    }

    public double getCoverage() {
        return 0;
    }

    public double getComplexity() {
        return 0;
    }

    public double getAbstractness() {
        return 0;
    }

    public double getStability() {
        return 0;
    }

    public double getDistance() {
        return 0;
    }

}
