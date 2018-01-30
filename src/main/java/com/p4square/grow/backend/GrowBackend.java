/*
 * Copyright 2012 Jesse Morgan
 */

package com.p4square.grow.backend;

import java.io.IOException;

import com.codahale.metrics.MetricRegistry;

import com.p4square.grow.provider.*;
import org.apache.log4j.Logger;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

import com.p4square.grow.config.Config;

import com.p4square.grow.model.Message;
import com.p4square.grow.model.MessageThread;
import com.p4square.grow.model.Playlist;
import com.p4square.grow.model.Question;
import com.p4square.grow.model.TrainingRecord;
import com.p4square.grow.model.UserRecord;

import com.p4square.grow.backend.resources.AccountResource;
import com.p4square.grow.backend.resources.BannerResource;
import com.p4square.grow.backend.resources.HealthCheckResource;
import com.p4square.grow.backend.resources.SurveyResource;
import com.p4square.grow.backend.resources.SurveyResultsResource;
import com.p4square.grow.backend.resources.TrainingRecordResource;
import com.p4square.grow.backend.resources.TrainingResource;

import com.p4square.grow.backend.feed.ThreadResource;
import com.p4square.grow.backend.feed.TopicResource;

import com.p4square.restlet.metrics.MetricRouter;

/**
 * Main class for the backend application.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class GrowBackend extends Application implements GrowData, ProvidesNotificationService {

    private final static Logger LOG = Logger.getLogger(GrowBackend.class);

    private final MetricRegistry mMetricRegistry;

    private final Config mConfig;
    private final GrowData mGrowData;
    private final NotificationService mNotificationService;

    public GrowBackend() {
        this(new Config(), new MetricRegistry());
    }

    public GrowBackend(Config config, MetricRegistry metricRegistry) {
        mConfig = config;

        mMetricRegistry = metricRegistry;

        mGrowData = new DynamoGrowData(config);

        mNotificationService = new SESNotificationService(config);
    }

    public MetricRegistry getMetrics() {
        return mMetricRegistry;
    }

    @Override
    public Restlet createInboundRoot() {
        Router router = new MetricRouter(getContext(), mMetricRegistry);

        // Account API
        router.attach("/accounts/{userId}", AccountResource.class);

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

        // Misc.
        router.attach("/banner", BannerResource.class);
        router.attach("/ping", HealthCheckResource.class);

        // Feed
        router.attach("/feed/{topic}", TopicResource.class);
        router.attach("/feed/{topic}/{thread}", ThreadResource.class);
        //router.attach("/feed/{topic/{thread}/{message}", MessageResource.class);

        router.attachDefault(new Directory(getContext(), new Reference(getClass().getResource("apiinfo.html"))));

        return router;
    }

    /**
     * Open the database.
     */
    @Override
    public void start() throws Exception {
        super.start();

        mGrowData.start();
    }

    /**
     * Close the database.
     */
    @Override
    public void stop() throws Exception {
        LOG.info("Shutting down...");
        mGrowData.stop();

        super.stop();
    }

    @Override
    public Provider<String, UserRecord> getUserRecordProvider() {
        return mGrowData.getUserRecordProvider();
    }

    @Override
    public Provider<String, Question> getQuestionProvider() {
        return mGrowData.getQuestionProvider();
    }

    @Override
    public CollectionProvider<String, String, String> getVideoProvider() {
        return mGrowData.getVideoProvider();
    }

    @Override
    public Provider<String, TrainingRecord> getTrainingRecordProvider() {
        return mGrowData.getTrainingRecordProvider();
    }

    /**
     * @return the Default Playlist.
     */
    public Playlist getDefaultPlaylist() throws IOException {
        return mGrowData.getDefaultPlaylist();
    }

    @Override
    public CollectionProvider<String, String, MessageThread> getThreadProvider() {
        return mGrowData.getThreadProvider();
    }

    @Override
    public CollectionProvider<String, String, Message> getMessageProvider() {
        return mGrowData.getMessageProvider();
    }

    @Override
    public Provider<String, String> getStringProvider() {
        return mGrowData.getStringProvider();
    }

    @Override
    public CollectionProvider<String, String, String> getAnswerProvider() {
        return mGrowData.getAnswerProvider();
    }

    @Override
    public NotificationService getNotificationService() { return mNotificationService; }

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
