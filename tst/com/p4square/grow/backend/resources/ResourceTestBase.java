/*
 * Copyright 2015 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.io.IOException;

import org.restlet.Application;

import com.p4square.grow.model.Playlist;
import com.p4square.grow.model.Question;
import com.p4square.grow.model.TrainingRecord;
import com.p4square.grow.model.UserRecord;

import com.p4square.grow.backend.feed.FeedDataProvider;
import com.p4square.grow.provider.CollectionProvider;
import com.p4square.grow.provider.MapCollectionProvider;
import com.p4square.grow.provider.MapProvider;
import com.p4square.grow.provider.Provider;
import com.p4square.grow.provider.ProvidesAssessments;
import com.p4square.grow.provider.ProvidesQuestions;
import com.p4square.grow.provider.ProvidesStrings;
import com.p4square.grow.provider.ProvidesTrainingRecords;
import com.p4square.grow.provider.ProvidesUserRecords;
import com.p4square.grow.provider.ProvidesVideos;

import org.junit.Before;

/**
 * 
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class ResourceTestBase {

    protected TestApplication mApplication;

    @Before
    public void setup() throws Exception {
        mApplication = new TestApplication();
        Application.setCurrent(mApplication);
    }

    public static class TestApplication extends Application implements
        ProvidesQuestions, ProvidesTrainingRecords, ProvidesUserRecords,
        ProvidesStrings, ProvidesAssessments
    {

        private final Provider<String, UserRecord> mUserRecordProvider;
        private final Provider<String, Question> mQuestionProvider;
        private final Provider<String, TrainingRecord> mTrainingRecordProvider;
        private final Provider<String, String> mStringProvider;
        private final CollectionProvider<String, String, String> mAnswerProvider;

        private Playlist mDefaultPlaylist;

        public TestApplication() {
            mStringProvider = new MapProvider<String, String>();
            mUserRecordProvider = new MapProvider<String, UserRecord>();
            mQuestionProvider = new MapProvider<String, Question>();
            mTrainingRecordProvider = new MapProvider<String, TrainingRecord>();
            mAnswerProvider = new MapCollectionProvider<String, String, String>();

            mDefaultPlaylist = new Playlist();
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

        public void setDefaultPlaylist(Playlist playlist) {
            mDefaultPlaylist = playlist;
        }

        @Override
        public Playlist getDefaultPlaylist() throws IOException {
            return mDefaultPlaylist;
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
}
