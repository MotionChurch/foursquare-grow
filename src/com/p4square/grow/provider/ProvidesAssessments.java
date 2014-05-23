/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.provider;

import com.p4square.grow.model.RecordedAnswer;

/**
 * 
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public interface ProvidesAssessments {
    /**
     * Provides a collection of user assessments.
     * The collection key is the user id.
     * The key is the question id.
     */
    CollectionProvider<String, String, String> getAnswerProvider();
}
