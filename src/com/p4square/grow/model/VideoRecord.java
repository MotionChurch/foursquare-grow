/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Simple bean containing video completion data.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class VideoRecord implements Cloneable {
    private Boolean mComplete;
    private Boolean mRequired;
    private Date mCompletionDate;

    public VideoRecord() {
        mComplete = null;
        mRequired = null;
        mCompletionDate = null;
    }

    public boolean getComplete() {
        if (mComplete == null) {
            return false;
        }
        return mComplete;
    }

    public void setComplete(boolean complete) {
        mComplete = complete;
    }

    @JsonIgnore
    public boolean isCompleteSet() {
        return mComplete != null;
    }

    public boolean getRequired() {
        if (mRequired == null) {
            return true;
        }
        return mRequired;
    }

    public void setRequired(boolean complete) {
        mRequired = complete;
    }

    @JsonIgnore
    public boolean isRequiredSet() {
        return mRequired != null;
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

    /**
     * @return an identical clone of this record.
     */
    public VideoRecord clone() throws CloneNotSupportedException {
        VideoRecord r = (VideoRecord) super.clone();
        r.mComplete = mComplete;
        r.mRequired = mRequired;
        r.mCompletionDate = mCompletionDate;
        return r;
    }
}
