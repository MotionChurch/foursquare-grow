/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.util.Date;

/**
 * Simple bean containing video completion data.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
class VideoRecord {
    private boolean mComplete;
    private boolean mRequired;
    private Date mCompletionDate;

    public VideoRecord() {
        mComplete = false;
        mRequired = true;
        mCompletionDate = null;
    }

    public boolean getComplete() {
        return mComplete;
    }

    public void setComplete(boolean complete) {
        mComplete = complete;
    }

    public boolean getRequired() {
        return mRequired;
    }

    public void setRequired(boolean complete) {
        mRequired = complete;
    }

    public Date getCompletionDate() {
        return mCompletionDate;
    }

    public void setCompletionDate(Date date) {
        mCompletionDate = date;
    }

    /**
     * Convenience method to mark a video complete.
     */
    public void complete() {
        mComplete = true;
        mCompletionDate = new Date();
    }
}
