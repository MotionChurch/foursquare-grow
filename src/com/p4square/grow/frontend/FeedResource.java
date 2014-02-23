/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.frontend;

import java.io.IOException;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;

import org.apache.log4j.Logger;

import com.p4square.grow.config.Config;
import com.p4square.grow.model.Message;
import com.p4square.grow.model.UserRecord;

/**
 * This resource handles user interactions with the feed.
 */
public class FeedResource extends ServerResource {
    private static final Logger LOG = Logger.getLogger(FeedResource.class);

    private Config mConfig;

    private FeedData mFeedData;

    // Fields pertaining to this request.
    protected String mTopic;
    protected String mThread;

    @Override
    public void doInit() {
        super.doInit();

        GrowFrontend growFrontend = (GrowFrontend) getApplication();
        mConfig = growFrontend.getConfig();

        mFeedData = new FeedData(getContext(), mConfig);

        mTopic = getAttribute("topic");
        if (mTopic != null) {
            mTopic = mTopic.trim();
        }

        mThread = getAttribute("thread");
        if (mThread != null) {
            mThread = mThread.trim();
        }
    }

    /**
     * Create a new MessageThread.
     */
    @Override
    protected Representation post(Representation entity) {
        try {
            if (mTopic == null || mTopic.length() == 0 || !FeedData.TOPICS.contains(mTopic)) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return ErrorPage.NOT_FOUND;
            }

            Form form = new Form(entity);

            String question = form.getFirstValue("question");

            Message message = new Message();
            message.setMessage(question);

            UserRecord user = new UserRecord(getRequest().getClientInfo().getUser());
            message.setAuthor(user);

            if (mThread != null && mThread.length() != 0) {
                // Post a response
                mFeedData.createResponse(mTopic, mThread, message);

            } else {
                // Post a new thread
                mFeedData.createThread(mTopic, message);
            }

            /*
             * Can't trust the referrer, so we'll send them to the
             * appropriate part of the training page
             * TODO: This could be better done.
             */
            String nextPage = mConfig.getString("dynamicRoot", "");
            nextPage += "/account/training/" + mTopic;
            getResponse().redirectSeeOther(nextPage);
            return null;

        } catch (IOException e) {
            LOG.fatal("Could not save message: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return ErrorPage.BACKEND_ERROR;

        }
    }
}
