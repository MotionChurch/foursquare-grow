/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.backend;

import java.io.IOException;

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
import com.p4square.grow.provider.DelegateCollectionProvider;
import com.p4square.grow.provider.DelegateProvider;
import com.p4square.grow.provider.Provider;

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
class CassandraGrowData implements GrowData {
    private static final String DEFAULT_COLUMN = "value";

    private final Config mConfig;
    private final CassandraDatabase mDatabase;

    private final Provider<String, UserRecord> mUserRecordProvider;

    private final Provider<String, Question> mQuestionProvider;
    private final CassandraTrainingRecordProvider mTrainingRecordProvider;
    private final CollectionProvider<String, String, String> mVideoProvider;

    private final CollectionProvider<String, String, MessageThread> mFeedThreadProvider;
    private final CollectionProvider<String, String, Message> mFeedMessageProvider;

    private final Provider<String, String> mStringProvider;

    private final CollectionProvider<String, String, String> mAnswerProvider;

    public CassandraGrowData(final Config config) {
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

        mVideoProvider = new DelegateCollectionProvider<String, String, String, String, String>(
                new CassandraCollectionProvider<String>(mDatabase, "strings", String.class)) {
            @Override
            public String makeCollectionKey(String key) {
                return "/training/" + key;
            }

            @Override
            public String makeKey(String key) {
                return key;
            }

            @Override
            public String unmakeKey(String key) {
                return key;
            }
        };

        mStringProvider = new DelegateProvider<String, CassandraKey, String>(
                new CassandraProviderImpl<String>(mDatabase, String.class)) {
            @Override
            public CassandraKey makeKey(String id) {
                return new CassandraKey("strings", id, DEFAULT_COLUMN);
            }
        };

        mAnswerProvider = new CassandraCollectionProvider<String>(
                mDatabase, "assessments", String.class);
    }

    @Override
    public void start() throws Exception {
        mDatabase.setClusterName(mConfig.getString("clusterName", "Dev Cluster"));
        mDatabase.setKeyspaceName(mConfig.getString("keyspace", "GROW"));
        mDatabase.init();
    }

    @Override
    public void stop() throws Exception {
        mDatabase.close();
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

    @Override
    public CollectionProvider<String, String, String> getVideoProvider() {
        return mVideoProvider;
    }

    @Override
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

    @Override
    public Provider<String, String> getStringProvider() {
        return mStringProvider;
    }

    @Override
    public CollectionProvider<String, String, String> getAnswerProvider() {
        return mAnswerProvider;
    }
}
