package com.ikokoon.serenity.model;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 12.08.16
 */
public abstract class Stability<P, C> extends Composite<P, C> implements Serializable {

    private double stability;

    private double efference;
    private double afference;

    private Set<Efferent> efferent;
    private Set<Afferent> afferent;

    public double getStability() {
        return stability;
    }

    public void setStability(double stability) {
        this.stability = stability;
    }

    public double getEfference() {
        return efference;
    }

    public void setEfference(double efferent) {
        this.efference = efferent;
    }

    public double getAfference() {
        return afference;
    }

    public void setAfference(double afferent) {
        this.afference = afferent;
    }

    public Set<Efferent> getEfferent() {
        if (efferent == null) {
            efferent = new TreeSet<>();
        }
        return efferent;
    }

    public void setEfferent(Set<Efferent> efference) {
        this.efferent = efference;
    }

    public Set<Afferent> getAfferent() {
        if (afferent == null) {
            afferent = new TreeSet<>();
        }
        return afferent;
    }

    public void setAfferent(Set<Afferent> afference) {
        this.afferent = afference;
    }

}
