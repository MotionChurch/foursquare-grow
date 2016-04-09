/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import org.apache.log4j.Logger;

/**
 * ScoringEngine computes the score for a question and a given answer.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public abstract class ScoringEngine {
    protected static final Logger LOG = Logger.getLogger(ScoringEngine.class);

    /**
     * Update the score based on the given question and answer.
     *
     * @param score The running score to update.
     * @param question The question to compute the score for.
     * @param answer The answer give to this question.
     * @return true if scoring should continue, false if this answer trumps everything else.
     */
    public abstract boolean scoreAnswer(Score score, Question question, RecordedAnswer answer);
}
