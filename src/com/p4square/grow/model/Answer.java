/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import org.apache.log4j.Logger;

/**
 * This is the model of an assessment question's answer.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class Answer {
    private static final Logger LOG = Logger.getLogger(Answer.class);

    /**
     * ScoreType determines how the answer will be scored.
     *
     */
    public static enum ScoreType {
        /**
         * This question has no effect on the score.
         */
        NONE,

        /**
         * The score of this question is part of the average.
         */
        AVERAGE,

        /**
         * The score of this question is the total score, no other questions
         * matter after this point.
         */
        TRUMP;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private String mAnswerText;
    private ScoreType mType;
    private float mScoreFactor;
    private String mNextQuestionId;

    public Answer() {
        mType = ScoreType.AVERAGE;
    }

    /**
     * @return The text associated with the answer.
     */
    public String getText() {
        return mAnswerText;
    }

    /**
     * Set the text associated with the answer.
     * @param text The new text.
     */
    public void setText(String text) {
        mAnswerText = text;
    }

    /**
     * @return the ScoreType for the Answer.
     */
    public ScoreType getType() {
        return mType;
    }

    /**
     * Set the ScoreType for the answer.
     * @param type The new ScoreType.
     */
    public void setType(ScoreType type) {
        mType = type;
    }

    /**
     * @return the delta of the score if this answer is selected.
     */
    public float getScore() {
        if (mType == ScoreType.NONE) {
            return 0;
        }

        return mScoreFactor;
    }

    /**
     * Set the score delta for this answer.
     * @param score The new delta.
     */
    public void setScore(float score) {
        mScoreFactor = score;
    }

    /**
     * @return the id of the next question if this answer is selected, or null
     *         if selecting this answer has no effect.
     */
    public String getNextQuestion() {
        return mNextQuestionId;
    }

    /**
     * Set the id of the next question when this answer is selected.
     * @param id The next question id or null to proceed as usual.
     */
    public void setNextQuestion(String id) {
        mNextQuestionId = id;
    }

    /**
     * Adjust the running score for the selection of this answer.
     * @param score The running score to adjust.
     * @return true if scoring should continue, false if this answer trumps all.
     */
    public boolean score(final Score score) {
        switch (getType()) {
            case TRUMP:
                score.sum = getScore();
                score.count = 1;
                return false; // Quit scoring.

            case AVERAGE:
                LOG.error("ScoreType.AVERAGE: { delta: \"" + getScore() + "\" }");
                score.sum += getScore();
                score.count++;
                break;

            case NONE:
                break;
        }

        return true; // Continue scoring
    }
}
