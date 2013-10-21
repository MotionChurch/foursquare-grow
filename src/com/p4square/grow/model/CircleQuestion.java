/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

/**
 * Circle Question.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class CircleQuestion extends Question {
    private static final ScoringEngine ENGINE = new QuadScoringEngine();

    private String mTopLeft;
    private String mTopRight;
    private String mBottomLeft;
    private String mBottomRight;

    /**
     * @return the Top Left label.
     */
    public String getTopLeft() {
        return mTopLeft;
    }

    /**
     * Set the Top Left label.
     * @param s The new top left label.
     */
    public void setTopLeft(String s) {
        mTopLeft = s;
    }

    /**
     * @return the Top Right label.
     */
    public String getTopRight() {
        return mTopRight;
    }

    /**
     * Set the Top Right label.
     * @param s The new top left label.
     */
    public void setTopRight(String s) {
        mTopRight = s;
    }

    /**
     * @return the Bottom Left label.
     */
    public String getBottomLeft() {
        return mBottomLeft;
    }

    /**
     * Set the Bottom Left label.
     * @param s The new top left label.
     */
    public void setBottomLeft(String s) {
        mBottomLeft = s;
    }

    /**
     * @return the Bottom Right label.
     */
    public String getBottomRight() {
        return mBottomRight;
    }

    /**
     * Set the Bottom Right label.
     * @param s The new top left label.
     */
    public void setBottomRight(String s) {
        mBottomRight = s;
    }

    @Override
    public boolean scoreAnswer(Score score, RecordedAnswer answer) {
        return ENGINE.scoreAnswer(score, this, answer);
    }

    @Override
    public QuestionType getType() {
        return QuestionType.CIRCLE;
    }
}
