/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.provider;

import com.p4square.grow.model.Question;

/**
 * Indicates the ability to provide a Question Provider.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public interface ProvidesQuestions {
    /**
     * @return A Provider of Questions keyed by question id.
     */
    Provider<String, Question> getQuestionProvider();
}
