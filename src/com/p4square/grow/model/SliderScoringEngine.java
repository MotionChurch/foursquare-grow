/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

/**
 * SliderScoringEngine expects the user's answer to be a decimal value in the
 * range [0, 1]. The value is scaled to the range [1, 4] and added to the
 * score.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SliderScoringEngine extends ScoringEngine {

    @Override
    public boolean scoreAnswer(Score score, Question question, RecordedAnswer userAnswer) {
        float delta = Float.valueOf(userAnswer.getAnswerId()) * 3 + 1;

        if (delta < 0 || delta > 4) {
            throw new IllegalArgumentException("Answer out of bounds.");
        }

        score.sum += delta;
        score.count++;

        return true;
    }
}
