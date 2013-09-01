/*
 * Copyright 2012 Jesse Morgan
 */

package com.p4square.grow.backend;

import org.apache.log4j.Logger;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.p4square.grow.config.Config;

import com.p4square.grow.backend.db.CassandraDatabase;
import com.p4square.grow.backend.resources.SurveyResource;
import com.p4square.grow.backend.resources.SurveyResultsResource;
import com.p4square.grow.backend.resources.TrainingResource;
import com.p4square.grow.backend.resources.TrainingRecordResource;

/**
 * Main class for the backend application.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class GrowBackend extends Application {
    private final static Logger LOG = Logger.getLogger(GrowBackend.class);

    private final Config mConfig;
    private final CassandraDatabase mDatabase;

    public GrowBackend() {
        mConfig = new Config();
        mDatabase = new CassandraDatabase();
    }

    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());

        // Survey API
        router.attach("/assessment/question/{questionId}", SurveyResource.class);

        router.attach("/accounts/{userId}/assessment", SurveyResultsResource.class);
        router.attach("/accounts/{userId}/assessment/answers/{questionId}",
                SurveyResultsResource.class);

        // Training API
        router.attach("/training/{level}", TrainingResource.class);
        router.attach("/training/{level}/videos/{videoId}", TrainingResource.class);

        router.attach("/accounts/{userId}/training", TrainingRecordResource.class);
        router.attach("/accounts/{userId}/training/videos/{videoId}",
                TrainingRecordResource.class);


        return router;
    }

    /**
     * Open the database.
     */
    @Override
    public void start() throws Exception {
        super.start();

        // Load config
        final String configDomain =
            getContext().getParameters().getFirstValue("configDomain");
        if (configDomain != null) {
            mConfig.setDomain(configDomain);
        }

        mConfig.updateConfig(this.getClass().getResourceAsStream("/grow.properties"));

        final String configFilename =
            getContext().getParameters().getFirstValue("configFile");

        if (configFilename != null) {
            LOG.info("Loading configuration from " + configFilename);
            mConfig.updateConfig(configFilename);
        }

        // Setup database
        mDatabase.setClusterName(mConfig.getString("clusterName", "Dev Cluster"));
        mDatabase.setKeyspaceName(mConfig.getString("keyspace", "GROW"));
        mDatabase.init();
    }

    /**
     * Close the database.
     */
    @Override
    public void stop() throws Exception {
        LOG.info("Shutting down...");
        mDatabase.close();

        super.stop();
    }

    /**
     * @return the current database.
     */
    public CassandraDatabase getDatabase() {
        return mDatabase;
    }

    /**
     * Stand-alone main for testing.
     */
    public static void main(String[] args) throws Exception {
        // Start the HTTP Server
        final Component component = new Component();
        component.getServers().add(Protocol.HTTP, 9095);
        component.getClients().add(Protocol.HTTP);
        component.getDefaultHost().attach(new GrowBackend());

        // Setup shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    component.stop();
                } catch (Exception e) {
                    LOG.error("Exception during cleanup", e);
                }
            }
        });

        LOG.info("Starting server...");

        try {
            component.start();
        } catch (Exception e) {
            LOG.fatal("Could not start: " + e.getMessage(), e);
        }
    }
}
