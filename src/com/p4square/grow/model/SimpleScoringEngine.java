/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

/**
 * SimpleScoringEngine expects the user's answer to a valid answer id and
 * scores accordingly.
 *
 * If the answer id is not valid an Exception is thrown.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SimpleScoringEngine extends ScoringEngine {

    @Override
    public boolean scoreAnswer(Score score, Question question, RecordedAnswer userAnswer) {
        final Answer answer = question.getAnswers().get(userAnswer.getAnswerId());
        if (answer == null) {
            throw new IllegalArgumentException("Not a valid answer.");
        }

        return answer.score(score);
    }
}
