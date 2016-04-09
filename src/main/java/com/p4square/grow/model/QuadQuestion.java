/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

/**
 * Two-dimensional Question.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class QuadQuestion extends Question {
    private static final ScoringEngine ENGINE = new QuadScoringEngine();

    private String mTop;
    private String mRight;
    private String mBottom;
    private String mLeft;

    /**
     * @return the top label.
     */
    public String getTop() {
        return mTop;
    }

    /**
     * Set the top label.
     * @param s The new top label.
     */
    public void setTop(String s) {
        mTop = s;
    }

    /**
     * @return the right label.
     */
    public String getRight() {
        return mRight;
    }

    /**
     * Set the right label.
     * @param s The new right label.
     */
    public void setRight(String s) {
        mRight = s;
    }

    /**
     * @return the bottom label.
     */
    public String getBottom() {
        return mBottom;
    }

    /**
     * Set the bottom label.
     * @param s The new bottom label.
     */
    public void setBottom(String s) {
        mBottom = s;
    }

    /**
     * @return the left label.
     */
    public String getLeft() {
        return mLeft;
    }

    /**
     * Set the left label.
     * @param s The new left label.
     */
    public void setLeft(String s) {
        mLeft = s;
    }

    @Override
    public boolean scoreAnswer(Score score, RecordedAnswer answer) {
        return ENGINE.scoreAnswer(score, this, answer);
    }

    @Override
    public QuestionType getType() {
        return QuestionType.QUAD;
    }
}
