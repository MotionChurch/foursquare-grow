/*
 * Copyright 2012 Jesse Morgan
 */

package com.p4square.grow.backend;

import java.io.IOException;

import org.apache.log4j.Logger;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import com.p4square.grow.config.Config;

import com.p4square.grow.backend.db.CassandraDatabase;
import com.p4square.grow.backend.db.CassandraKey;
import com.p4square.grow.backend.db.CassandraProviderImpl;
import com.p4square.grow.backend.db.CassandraCollectionProvider;
import com.p4square.grow.backend.db.CassandraTrainingRecordProvider;

import com.p4square.grow.model.Message;
import com.p4square.grow.model.MessageThread;
import com.p4square.grow.model.Playlist;
import com.p4square.grow.model.Question;
import com.p4square.grow.model.TrainingRecord;
import com.p4square.grow.model.UserRecord;

import com.p4square.grow.provider.CollectionProvider;
import com.p4square.grow.provider.DelegateProvider;
import com.p4square.grow.provider.Provider;
import com.p4square.grow.provider.ProvidesQuestions;
import com.p4square.grow.provider.ProvidesTrainingRecords;
import com.p4square.grow.provider.ProvidesUserRecords;

import com.p4square.grow.backend.resources.AccountResource;
import com.p4square.grow.backend.resources.BannerResource;
import com.p4square.grow.backend.resources.SurveyResource;
import com.p4square.grow.backend.resources.SurveyResultsResource;
import com.p4square.grow.backend.resources.TrainingRecordResource;
import com.p4square.grow.backend.resources.TrainingResource;

import com.p4square.grow.backend.feed.FeedDataProvider;
import com.p4square.grow.backend.feed.ThreadResource;
import com.p4square.grow.backend.feed.TopicResource;

/**
 * Main class for the backend application.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class GrowBackend extends Application
        implements ProvidesQuestions, ProvidesTrainingRecords, FeedDataProvider,
          ProvidesUserRecords {
    private static final String DEFAULT_COLUMN = "value";

    private final static Logger LOG = Logger.getLogger(GrowBackend.class);

    private final Config mConfig;
    private final CassandraDatabase mDatabase;

    private final Provider<String, UserRecord> mUserRecordProvider;

    private final Provider<String, Question> mQuestionProvider;
    private final CassandraTrainingRecordProvider mTrainingRecordProvider;

    private final CollectionProvider<String, String, MessageThread> mFeedThreadProvider;
    private final CollectionProvider<String, String, Message> mFeedMessageProvider;

    public GrowBackend() {
        this(new Config());
    }

    public GrowBackend(Config config) {
        mConfig = config;
        mDatabase = new CassandraDatabase();

        mUserRecordProvider = new DelegateProvider<String, CassandraKey, UserRecord>(
                new CassandraProviderImpl<UserRecord>(mDatabase, UserRecord.class)) {
            @Override
            public CassandraKey makeKey(String userid) {
                return new CassandraKey("accounts", userid, DEFAULT_COLUMN);
            }
        };

        mQuestionProvider = new DelegateProvider<String, CassandraKey, Question>(
                new CassandraProviderImpl<Question>(mDatabase, Question.class)) {
            @Override
            public CassandraKey makeKey(String questionId) {
                return new CassandraKey("strings", "/questions/" + questionId, DEFAULT_COLUMN);
            }
        };

        mFeedThreadProvider = new CassandraCollectionProvider<MessageThread>(mDatabase,
                "feedthreads", MessageThread.class);
        mFeedMessageProvider = new CassandraCollectionProvider<Message>(mDatabase,
                "feedmessages", Message.class);

        mTrainingRecordProvider = new CassandraTrainingRecordProvider(mDatabase);
    }

    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());

        // Account API
        router.attach("/accounts", AccountResource.class);
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

        // Feed
        router.attach("/feed/{topic}", TopicResource.class);
        router.attach("/feed/{topic}/{thread}", ThreadResource.class);
        //router.attach("/feed/{topic/{thread}/{message}", MessageResource.class);

        return router;
    }

    /**
     * Open the database.
     */
    @Override
    public void start() throws Exception {
        super.start();

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

    @Override
    public Provider<String, UserRecord> getUserRecordProvider() {
        return mUserRecordProvider;
    }

    @Override
    public Provider<String, Question> getQuestionProvider() {
        return mQuestionProvider;
    }

    @Override
    public Provider<String, TrainingRecord> getTrainingRecordProvider() {
        return mTrainingRecordProvider;
    }

    /**
     * @return the Default Playlist.
     */
    public Playlist getDefaultPlaylist() throws IOException {
        return mTrainingRecordProvider.getDefaultPlaylist();
    }

    @Override
    public CollectionProvider<String, String, MessageThread> getThreadProvider() {
        return mFeedThreadProvider;
    }

    @Override
    public CollectionProvider<String, String, Message> getMessageProvider() {
        return mFeedMessageProvider;
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
