/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

/**
 * Simple structure containing a score's sum and count.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class Score {
    /**
     * Return the integer value for the given Score String.
     */
    public static int numericScore(String score) {
        if ("teacher".equals(score)) {
            return 4;
        } else if ("disciple".equals(score)) {
            return 3;
        } else if ("believer".equals(score)) {
            return 2;
        } else {
            return 1;
        }
    }

    double sum;
    int count;

    public Score() {
        sum = 0;
        count = 0;
    }

    /**
     * Copy Constructor.
     */
    public Score(Score other) {
        sum = other.sum;
        count = other.count;
    }

    /**
     * @return The sum of all the points.
     */
    public double getSum() {
        return sum;
    }

    /**
     * @return The number of questions included in the score.
     */
    public int getCount() {
        return count;
    }

    /**
     * @return The final score.
     */
    public double getScore() {
        if (count == 0) {
            return 0;
        }

        return sum / count;
    }

    @Override
    public String toString() {
        final double score = getScore();

        if (score >= 4) {
            return "teacher";

        } else if (score >= 3) {
            return "disciple";

        } else if (score >= 2) {
            return "believer";

        } else {
            return "seeker";
        }
    }

}
