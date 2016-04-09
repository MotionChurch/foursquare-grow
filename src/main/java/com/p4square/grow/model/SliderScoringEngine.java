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
        int numberOfAnswers = question.getAnswers().size();
        if (numberOfAnswers == 0) {
            throw new IllegalArgumentException("Question has no answers.");
        }

        double answer = Double.valueOf(userAnswer.getAnswerId());
        if (answer < 0 || answer > 1) {
            throw new IllegalArgumentException("Answer out of bounds.");
        }

        double delta = Math.max(1, Math.ceil(answer * numberOfAnswers) / numberOfAnswers * 4);

        score.sum += delta;
        score.count++;

        return true;
    }
}
