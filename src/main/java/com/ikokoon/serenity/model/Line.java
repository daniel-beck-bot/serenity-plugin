package com.ikokoon.serenity.model;

import java.io.Serializable;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 12.08.09
 */
@SuppressWarnings("unused")
@Unique(fields = {Composite.CLASS_NAME, Composite.METHOD_NAME, Composite.NUMBER})
public class Line<E, F> extends Composite<Class<?, ?>, Object> implements Comparable<Line<?, ?>>, Serializable {

    private String className;
    private String methodName;
    private int number;
    private int counter;

    public Line() {
    }

    public Line(final String className, final String methodName, final int number) {
        this.className = className;
        this.methodName = methodName;
        this.number = number;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(final String methodName) {
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(final String className) {
        this.className = className;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(final int counter) {
        this.counter = counter;
    }

    public void increment() {
        this.counter++;
    }

    public String toString() {
        return "Id : " + getId() + ", class name : " + className + ", method name : " + methodName + ", number : " + number + ", counter : "
                + counter;
    }

    @Override
    public int compareTo(final Line<?, ?> o) {
        int comparison = 0;
        if (this.getId() != null && o.getId() != null) {
            comparison = this.getId().compareTo(o.getId());
        }
        return comparison;
    }

}
