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
    private final String mAttributeName;
    private String mId;
    private Date mStartDate;
    private Date mEndDate;
    private String mComment;

    /**
     * @param name The attribute name.
     */
    public Attribute(final String name) {
        mAttributeName = name;
    }

    /**
     * @return the Attribute name.
     */
    public String getAttributeName() {
        return mAttributeName;
    }

    /**
     * @return the id of this specific attribute instance.
     */
    public String getId() {
        return mId;
    }

    /**
     * Set the attribute id to id.
     */
    public void setId(final String id) {
        mId = id;
    }

    /**
     * @return the start date for the attribute.
     */
    public Date getStartDate() {
        return mStartDate;
    }

    /**
     * Set the start date for the attribute.
     */
    public void setStartDate(final Date date) {
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
    public void setEndDate(final Date date) {
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
    public void setComment(final String comment) {
        mComment = comment;
    }
}
