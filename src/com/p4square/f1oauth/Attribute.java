/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.f1oauth;

import java.util.Date;

/**
 * F1 Attribute Data.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class Attribute {
    private Date mStartDate;
    private Date mEndDate;
    private String mComment;

    /**
     * @return the start date for the attribute.
     */
    public Date getStartDate() {
        return mStartDate;
    }

    /**
     * Set the start date for the attribute.
     */
    public void setStartDate(Date date) {
        mStartDate = date;
    }

    /**
     * @return the end date for the attribute.
     */
    public Date getEndDate() {
        return mEndDate;
    }

    /**
     * Set the end date for the attribute.
     */
    public void setEndDate(Date date) {
        mEndDate = date;
    }

    /**
     * @return The comment on the Attribute.
     */
    public String getComment() {
        return mComment;
    }

    /**
     * Set the comment on the attribute.
     */
    public void setComment(String comment) {
        mComment = comment;
    }
}
