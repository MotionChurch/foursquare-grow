/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.util.Map;

/**
 * This is the model of an assessment question's answer.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
class Answer {
    public static enum ScoreType {
        NONE, AVERAGE, TRUMP;
    }

    private final String mAnswerId;
    private final String mAnswerText;
    private final ScoreType mType;
    private final float mScoreFactor;

    public Answer(final String id, final Map<String, Object> answer) {
        mAnswerId = id;
        mAnswerText = (String) answer.get("text");
        final String typeStr = (String) answer.get("type");
        if (typeStr == null) {
            mType = ScoreType.AVERAGE;
        } else {
            mType = ScoreType.valueOf(typeStr.toUpperCase());
        }

        if (mType != ScoreType.NONE) {
            mScoreFactor = Float.valueOf((String) answer.get("score"));
        } else {
            mScoreFactor = 0;
        }

    }

    public String getId() {
        return mAnswerId;
    }

    public String getText() {
        return mAnswerText;
    }

    public ScoreType getType() {
        return mType;
    }

    public float getScoreFactor() {
        return mScoreFactor;
    }
}
