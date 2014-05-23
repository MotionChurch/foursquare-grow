/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.backend;

import com.p4square.grow.backend.feed.FeedDataProvider;
import com.p4square.grow.model.Playlist;
import com.p4square.grow.provider.ProvidesAssessments;
import com.p4square.grow.provider.ProvidesQuestions;
import com.p4square.grow.provider.ProvidesStrings;
import com.p4square.grow.provider.ProvidesTrainingRecords;
import com.p4square.grow.provider.ProvidesUserRecords;
import com.p4square.grow.provider.ProvidesVideos;

/**
 * Aggregate of the data provider interfaces.
 *
 * Used by GrowBackend to swap out implementations of the providers.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
interface GrowData extends ProvidesQuestions, ProvidesTrainingRecords, ProvidesVideos,
                                   FeedDataProvider, ProvidesUserRecords, ProvidesStrings,
                                   ProvidesAssessments {

    /**
     * Start the data provider.
     */
    void start() throws Exception;

    /**
     * Stop the data provider.
     */
    void stop() throws Exception;
}
