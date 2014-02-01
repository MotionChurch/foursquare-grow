/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import java.util.Date;

/**
 * A feed message.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class Message {
    private String mThreadId;
    private String mId;
    private String mAuthor;
    private Date mCreated;
    private String mMessage;

    /**
     * @return The id of the thread that the message belongs to.
     */
    public String getThreadId() {
        return mThreadId;
    }

    /**
     * Set the id of the thread that the message belongs to.
     * @param id The new thread id.
     */
    public void setThreadId(String id) {
        mThreadId = id;
    }

    /**
     * @return The id the message.
     */
    public String getId() {
        return mId;
    }

    /**
     * Set the id of the message.
     * @param id The new message id.
     */
    public void setId(String id) {
        mId = id;
    }

    /**
     * @return The author of the message.
     */
    public String getAuthor() {
        return mAuthor;
    }

    /**
     * Set the author of the message.
     * @param author The new author.
     */
    public void setAuthor(String author) {
        mAuthor = author;
    }

    /**
     * @return The Date the message was created.
     */
    public Date getCreated() {
        return mCreated;
    }

    /**
     * Set the Date the message was created.
     * @param date The new creation date.
     */
    public void setCreated(Date date) {
        mCreated = date;
    }

    /**
     * @return The message text.
     */
    public String getMessage() {
        return mMessage;
    }

    /**
     * Set the message text.
     * @param text The message text.
     */
    public void setMessage(String text) {
        mMessage = text;
    }
}
