/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.feed;

import java.io.IOException;

import java.util.Date;
import java.util.Map;

import com.p4square.grow.backend.NotificationService;
import com.p4square.grow.provider.ProvidesNotificationService;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;
import org.restlet.representation.Representation;

import org.restlet.ext.jackson.JacksonRepresentation;

import org.apache.log4j.Logger;

import com.p4square.grow.model.Message;
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

    private NotificationService mNotifier;

    @Override
    public void doInit() {
        super.doInit();

        mBackend = (FeedDataProvider) getApplication();
        mTopic = getAttribute("topic");

        mNotifier = ((ProvidesNotificationService) getApplication()).getNotificationService();
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

        // Parse limit query parameter.
        int limit = -1;
        String limitString = getQueryValue("limit");
        if (limitString != null) {
           try {
               limit = Integer.parseInt(limitString);
           } catch (NumberFormatException e) {
               setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
               return null;
           }
        }

        try {
            Map<String, MessageThread> threads = mBackend.getThreadProvider().query(mTopic, limit);
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
            // Deserialize the incoming message.
            JacksonRepresentation<MessageThread> jsonRep =
                new JacksonRepresentation<>(entity, MessageThread.class);

            // Get the message from the request.
            // Throw away the wrapping MessageThread because we'll create our own later.
            Message message = jsonRep.getObject().getMessage();
            if (message.getCreated() == null) {
                message.setCreated(new Date());
            }

            // Create the new thread.
            MessageThread newThread = MessageThread.createNew();

            // Force the thread id and message to be what we expect.
            message.setId(Message.generateId());
            message.setThreadId(newThread.getId());
            newThread.setMessage(message);

            mBackend.getThreadProvider().put(mTopic, newThread.getId(), newThread);

            // Send a notification email
            mNotifier.sendNotification(
                    String.format("A new question was posted on the %s topic:\n\n%s", mTopic, message.getMessage()));


            setLocationRef(mTopic + "/" + newThread.getId());
            return new JacksonRepresentation(newThread);

        } catch (IOException e) {
            LOG.error("Unexpected exception: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
    }
}
