/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

/**
 * Simple structure containing a score's sum and count.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
class Score {
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

    @Override
    public String toString() {
        final double score = sum / count;

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
