/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.feed;

import java.io.IOException;

import java.util.Map;

import org.restlet.data.Status;
import org.restlet.resource.ServerResource;
import org.restlet.representation.Representation;

import org.restlet.ext.jackson.JacksonRepresentation;

import org.apache.log4j.Logger;

import com.p4square.grow.model.MessageThread;

/**
 * TopicResource manages the threads contained in a topic.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class TopicResource extends ServerResource {
    private static final Logger LOG = Logger.getLogger(TopicResource.class);

    private FeedDataProvider mBackend;
    private String mTopic;

    @Override
    public void doInit() {
        super.doInit();

        mBackend = (FeedDataProvider) getApplication();
        mTopic = getAttribute("topic");
    }

    /**
     * GET a list of threads in the topic.
     */
    @Override
    protected Representation get() {
        // If no topic is provided, return a list of topics.
        if (mTopic == null || mTopic.length() == 0) {
            return new JacksonRepresentation(FeedDataProvider.TOPICS);
        }

        // TODO: Support limit query parameter.

        try {
            Map<String, MessageThread> threads = mBackend.getThreadProvider().query(mTopic);
            return new JacksonRepresentation(threads.values());

        } catch (IOException e) {
            LOG.error("Unexpected exception: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
    }

    /**
     * POST a new thread to the topic.
     */
    @Override
    protected Representation post(Representation entity) {
        // If no topic is provided, respond with not allowed.
        if (mTopic == null || !mBackend.TOPICS.contains(mTopic)) {
            setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            return null;
        }

        try {
            MessageThread newThread = MessageThread.createNew();
            mBackend.getThreadProvider().put(mTopic, newThread.getId(), newThread);

            setStatus(Status.SUCCESS_NO_CONTENT);
            setLocationRef(mTopic + "/" + newThread.getId());
            return null;

        } catch (IOException e) {
            LOG.error("Unexpected exception: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
    }
}
