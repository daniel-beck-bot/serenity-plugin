package com.ikokoon.serenity.hudson;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import org.kohsuke.stapler.StaplerProxy;

import java.lang.ref.WeakReference;

/**
 * This is the Stapler 'proxy'. It serves the chain of results objects to the front end.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12-08-2009
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SerenityBuildAction implements StaplerProxy, Action {

    /**
     * The Hudson build abstractBuild, i.e. the action that really did the build.
     */
    private final AbstractBuild abstractBuild;
    /**
     * The result from the build for Serenity.
     */
    private transient WeakReference<ISerenityResult> result;

    /**
     * Constructor takes the Hudson build abstractBuild and the result that will be presented to
     * the front end for displaying the data from teh build and metrics.
     *
     * @param abstractBuild the build abstractBuild that generated the build
     * @param result        the result from Serenity that will be presented to the front end
     */
    public SerenityBuildAction(final AbstractBuild abstractBuild, final ISerenityResult result) {
        if (abstractBuild == null) {
            throw new RuntimeException("Owner cannot be null");
        }
        this.abstractBuild = abstractBuild;
        setResult(result);
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return "Serenity Report";
    }

    /**
     * {@inheritDoc}
     */
    public String getIconFileName() {
        return "graph.gif";
    }

    /**
     * {@inheritDoc}
     */
    public String getUrlName() {
        return "serenity";
    }

    /**
     * {@inheritDoc}
     */
    public Object getTarget() {
        return getResult();
    }

    public ISerenityResult getResult() {
        if (!hasResult()) {
            reloadReport();
        }
        if (!hasResult()) {
            return new SerenityResult(abstractBuild);
        }
        return result.get();
    }

    private void setResult(final ISerenityResult result) {
        this.result = new WeakReference(result);
    }

    private boolean hasResult() {
        return result != null && result.get() != null;
    }

    private void reloadReport() {
        ISerenityResult result = new SerenityResult(abstractBuild);
        setResult(result);
    }

}