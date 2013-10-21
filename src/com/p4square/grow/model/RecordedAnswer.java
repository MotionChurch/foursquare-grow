/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

/**
 * Simple model for a user's assessment answer.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class RecordedAnswer {
    private String mAnswerId;

    /**
     * @return The user's answer.
     */
    public String getAnswerId() {
        return mAnswerId;
    }

    /**
     * Set the answer id field.
     * @param id The new id.
     */
    public void setAnswerId(String id) {
        mAnswerId = id;
    }

    @Override
    public String toString() {
        return mAnswerId;
    }
}
