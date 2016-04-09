/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import java.util.UUID;

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class MessageThread {
    private String mId;
    private Message mMessage;

    /**
     * Create a new thread with a probably unique id.
     *
     * @return the new thread.
     */
    public static MessageThread createNew() {
        MessageThread t = new MessageThread();
        // IDs are keyed to sort lexicographically from latest to oldest.
        t.setId(String.format("%016x-%s", Long.MAX_VALUE - System.currentTimeMillis(),
                    UUID.randomUUID().toString()));

        return t;
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
     * @return The original message.
     */
    public Message getMessage() {
        return mMessage;
    }

    /**
     * Set the original message.
     * @param id The new message.
     */
    public void setMessage(Message message) {
        mMessage = message;
    }
}
