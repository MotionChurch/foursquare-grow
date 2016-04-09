/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

/**
 * Slider Question.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SliderQuestion extends Question {
    private static final ScoringEngine ENGINE = new SliderScoringEngine();

    @Override
    public boolean scoreAnswer(Score score, RecordedAnswer answer) {
        return ENGINE.scoreAnswer(score, this, answer);
    }

    @Override
    public QuestionType getType() {
        return QuestionType.SLIDER;
    }
}
