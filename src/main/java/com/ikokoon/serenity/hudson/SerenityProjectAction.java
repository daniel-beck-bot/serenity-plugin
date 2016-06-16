package com.ikokoon.serenity.hudson;

import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.ProminentProjectAction;
import hudson.model.Run;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.neodatis.odb.core.layers.layer3.IOSocketParameter;
import org.neodatis.odb.impl.core.server.layers.layer3.engine.ClientStorageEngine;
import org.neodatis.odb.impl.main.RemoteODBClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * An object in the chain of proxy objects that serve the front end in Hudson.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-12-2009
 */
@SuppressWarnings("rawtypes")
public class SerenityProjectAction extends Actionable implements ProminentProjectAction {

    private Logger logger = LoggerFactory.getLogger(SerenityProjectAction.class);
    /**
     * The real abstractProject that generated the build.
     */
    private AbstractProject abstractProject;

    /**
     * Constructor takes the real build from Hudson.
     *
     * @param abstractProject the build that generated the actual build
     */
    public SerenityProjectAction(final AbstractProject abstractProject) {
        logger.debug("SerenityProjectAction:");
        this.abstractProject = abstractProject;
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        logger.debug("getDisplayName");
        return "Serenity report";
    }

    /**
     * {@inheritDoc}
     */
    public String getIconFileName() {
        logger.debug("getIconFileName");
        return "graph.gif";
    }

    /**
     * {@inheritDoc}
     */
    public String getUrlName() {
        logger.debug("getUrlName");
        return "serenity";
    }

    /**
     * {@inheritDoc}
     */
    public String getSearchUrl() {
        logger.debug("getSearchUrl");
        return getUrlName();
    }

    @JavaScriptMethod
    public ISerenityResult getLastResult() {
        logger.debug("getLastResult");
        Run build = abstractProject.getLastStableBuild();
        if (build == null) {
            build = abstractProject.getLastBuild();
        }
        if (build != null) {
            SerenityBuildAction action = build.getAction(SerenityBuildAction.class);
            if (action != null) {
                return action.getResult();
            }
        }
        return null;
    }

    @JavaScriptMethod
    @SuppressWarnings("unused")
    public String getLastBuildProjectId() {
        return getLastResult().getLastBuildProjectId();
    }

    @JavaScriptMethod
    @SuppressWarnings("unused")
    public String getProjectName() {
        return getLastResult().getName();
    }

    @JavaScriptMethod
    @SuppressWarnings("unused")
    public String getProfilingData() {
        try {
            RemoteODBClient odbClient;
            IOSocketParameter ioSocketParameter;
            ClientStorageEngine clientStorageEngine;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return getLastResult().getName();
    }

    @SuppressWarnings("unused")
    public void doIndex(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
        logger.debug("doIndex");
        if (hasResult()) {
            rsp.sendRedirect2("../lastBuild/serenity");
        } else {
            rsp.sendRedirect2("nocoverage");
        }
    }

    public boolean hasResult() {
        logger.debug("hasResult");
        return getLastResult() != null;
    }

}