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
     * Return the decimal value for the given Score String.
     *
     * This method satisfies the invariant for Score x:
     *  numericScore(x.toString()) <= x.getScore()
     *
     *  @throws IllegalArgumentException if the string is not a score name.
     */
    public static double numericScore(String score) {
        score = score.toLowerCase();

        if ("teacher".equals(score)) {
            return 3.5;
        } else if ("disciple".equals(score)) {
            return 2.5;
        } else if ("believer".equals(score)) {
            return 1.5;
        } else if ("seeker".equals(score)) {
            return 0;
        } else {
            throw new IllegalArgumentException("Invalid score " + score);
        }
    }

    double sum;
    int count;

    public Score() {
        sum = 0;
        count = 0;
    }

    public Score(double sum, int count) {
        this.sum = sum;
        this.count = count;
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

    /**
     * @return the lowest score in the same category as this score.
     */
    public double floor() {
        final double score = getScore();

        if (score >= 3.5) {
            return 3.5; // teacher

        } else if (score >= 2.5) {
            return 2.5; // disciple

        } else if (score >= 1.5) {
            return 1.5; // believer

        } else {
            return 0; // seeker
        }
    }

    @Override
    public String toString() {
        final double score = getScore();

        if (score >= 3.5) {
            return "teacher";

        } else if (score >= 2.5) {
            return "disciple";

        } else if (score >= 1.5) {
            return "believer";

        } else {
            return "seeker";
        }
    }

}
