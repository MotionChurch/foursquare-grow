/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.feed;

import java.io.IOException;

import java.util.Date;
import java.util.Map;

import org.restlet.data.Status;
import org.restlet.resource.ServerResource;
import org.restlet.representation.Representation;

import org.restlet.ext.jackson.JacksonRepresentation;

import org.apache.log4j.Logger;

import com.p4square.grow.model.Message;

/**
 * ThreadResource manages the messages that make up a thread.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class ThreadResource extends ServerResource {
    private static final Logger LOG = Logger.getLogger(ThreadResource.class);

    private FeedDataProvider mBackend;
    private String mTopic;
    private String mThreadId;

    @Override
    public void doInit() {
        super.doInit();

        mBackend = (FeedDataProvider) getApplication();
        mTopic = getAttribute("topic");
        mThreadId = getAttribute("thread");
    }

    /**
     * GET a list of messages in a thread.
     */
    @Override
    protected Representation get() {
        // If the topic or threadId are missing, return a 404.
        if (mTopic == null || mTopic.length() == 0 ||
                mThreadId == null || mThreadId.length() == 0) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }

        // TODO: Support limit query parameter.

        try {
            String collectionKey = mTopic + "/" + mThreadId;
            Map<String, Message> messages = mBackend.getMessageProvider().query(collectionKey);
            return new JacksonRepresentation(messages.values());

        } catch (IOException e) {
            LOG.error("Unexpected exception: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
    }

    /**
     * POST a new message to the thread.
     */
    @Override
    protected Representation post(Representation entity) {
        // If the topic and thread are not provided, respond with not allowed.
        // TODO: Check if the thread exists.
        if (mTopic == null || !mBackend.TOPICS.contains(mTopic) ||
                mThreadId == null || mThreadId.length() == 0) {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            return null;
        }

        try {
            JacksonRepresentation<Message> jsonRep = new JacksonRepresentation<Message>(entity, Message.class);
            Message message = jsonRep.getObject();

            // Force the thread id and message to be what we expect.
            message.setThreadId(mThreadId);
            message.setId(Message.generateId());

            if (message.getCreated() == null) {
                message.setCreated(new Date());
            }

            String collectionKey = mTopic + "/" + mThreadId;
            mBackend.getMessageProvider().put(collectionKey, message.getId(), message);

            setLocationRef(mThreadId + "/" + message.getId());
            return new JacksonRepresentation(message);

        } catch (IOException e) {
            LOG.error("Unexpected exception: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
    }
}
