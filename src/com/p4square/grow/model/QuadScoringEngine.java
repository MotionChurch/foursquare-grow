/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import com.p4square.grow.model.Point;

/**
 * QuadScoringEngine expects the user's answer to be a Point string. We find
 * the closest answer Point to the user's answer and treat that as the answer.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class QuadScoringEngine extends ScoringEngine {

    @Override
    public boolean scoreAnswer(Score score, Question question, RecordedAnswer userAnswer) {
        // Find all of the answer points.
        Point[] answers = new Point[question.getAnswers().size()];
        {
            int i = 0;
            for (String answerStr : question.getAnswers().keySet()) {
               answers[i++] = Point.valueOf(answerStr);
            }
        }

        // Parse the user's answer.
        Point userPoint = Point.valueOf(userAnswer.getAnswerId());

        // Find the closest answer point to the user's answer.
        double minDistance = Double.MAX_VALUE;
        int answerIndex = 0;
        for (int i = 0; i < answers.length; i++) {
            final double distance = userPoint.distance(answers[i]);
            if (distance < minDistance) {
                minDistance = distance;
                answerIndex = i;
            }
        }

        LOG.debug("Quad " + question.getId() + ": Got answer "
                + answers[answerIndex].toString() + " for user point " + userAnswer);

        // Get the answer and update the score.
        final Answer answer = question.getAnswers().get(answers[answerIndex].toString());
        return answer.score(score);
    }
}
