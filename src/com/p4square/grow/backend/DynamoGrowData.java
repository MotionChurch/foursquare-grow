/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.backend;

import java.io.IOException;

import com.amazonaws.auth.AWSCredentials;

import com.p4square.grow.backend.dynamo.DynamoDatabase;
import com.p4square.grow.backend.dynamo.DynamoKey;
import com.p4square.grow.backend.dynamo.DynamoProviderImpl;
import com.p4square.grow.backend.dynamo.DynamoCollectionProviderImpl;

import com.p4square.grow.config.Config;

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
import com.p4square.grow.provider.JsonEncodedProvider;

/**
 * 
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
class DynamoGrowData implements GrowData {
    private static final String DEFAULT_COLUMN = "value";
    private static final String DEFAULT_PLAYLIST_KEY = "/training/defaultplaylist";

    private final Config mConfig;
    private final DynamoDatabase mDatabase;

    private final Provider<String, UserRecord> mUserRecordProvider;

    private final Provider<String, Question> mQuestionProvider;
    private final Provider<String, TrainingRecord> mTrainingRecordProvider;
    private final CollectionProvider<String, String, String> mVideoProvider;

    private final CollectionProvider<String, String, MessageThread> mFeedThreadProvider;
    private final CollectionProvider<String, String, Message> mFeedMessageProvider;

    private final Provider<String, String> mStringProvider;

    private final CollectionProvider<String, String, String> mAnswerProvider;

    public DynamoGrowData(final Config config) {
        mConfig = config;

        mDatabase = new DynamoDatabase(config);

        mUserRecordProvider = new DelegateProvider<String, DynamoKey, UserRecord>(
                new DynamoProviderImpl<UserRecord>(mDatabase, UserRecord.class)) {
            @Override
            public DynamoKey makeKey(String userid) {
                return DynamoKey.newAttributeKey("accounts", userid, DEFAULT_COLUMN);
            }
        };

        mQuestionProvider = new DelegateProvider<String, DynamoKey, Question>(
                new DynamoProviderImpl<Question>(mDatabase, Question.class)) {
            @Override
            public DynamoKey makeKey(String questionId) {
                return DynamoKey.newAttributeKey("strings",
                                                 "/questions/" + questionId,
                                                 DEFAULT_COLUMN);
            }
        };

        mFeedThreadProvider = new DynamoCollectionProviderImpl<MessageThread>(
                mDatabase, "feedthreads", MessageThread.class);
        mFeedMessageProvider = new DynamoCollectionProviderImpl<Message>(
                mDatabase, "feedmessages", Message.class);

        mTrainingRecordProvider = new DelegateProvider<String, DynamoKey, TrainingRecord>(
                new DynamoProviderImpl<TrainingRecord>(mDatabase, TrainingRecord.class)) {
            @Override
            public DynamoKey makeKey(String userId) {
                return DynamoKey.newAttributeKey("training",
                                                 userId,
                                                 DEFAULT_COLUMN);
            }
        };

        mVideoProvider = new DelegateCollectionProvider<String, String, String, String, String>(
                new DynamoCollectionProviderImpl<String>(mDatabase, "strings", String.class)) {
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

        mStringProvider = new DelegateProvider<String, DynamoKey, String>(
                new DynamoProviderImpl<String>(mDatabase, String.class)) {
            @Override
            public DynamoKey makeKey(String id) {
                return DynamoKey.newAttributeKey("strings", id, DEFAULT_COLUMN);
            }
        };

        mAnswerProvider = new DynamoCollectionProviderImpl<String>(
                mDatabase, "assessments", String.class);
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
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
        String blob = mStringProvider.get(DEFAULT_PLAYLIST_KEY);
        if (blob == null) {
            return null;
        }

        return JsonEncodedProvider.MAPPER.readValue(blob, Playlist.class);
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
