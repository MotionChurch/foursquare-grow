/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.frontend;

import java.io.IOException;
import java.util.List;

import org.restlet.Context;
import org.restlet.Restlet;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import com.p4square.grow.config.Config;
import com.p4square.grow.frontend.JsonRequestProvider;
import com.p4square.grow.model.Message;
import com.p4square.grow.model.MessageThread;
import com.p4square.grow.provider.JsonEncodedProvider;
import com.p4square.grow.provider.Provider;

/**
 * Fetch feed data for a topic.
 */
public class FeedData {

    private final Config mConfig;
    private final String mBackendURI;

    private final Provider<String, List<MessageThread>> mThreadProvider;
    private final Provider<String, List<Message>> mMessageProvider;

    public FeedData(final Context context, final Config config) {
        mConfig = config;
        mBackendURI = mConfig.getString("backendUri", "riap://component/backend") + "/feed";

        Restlet clientDispatcher = context.getClientDispatcher();

        TypeFactory factory = JsonEncodedProvider.MAPPER.getTypeFactory();

        JavaType threadType = factory.constructCollectionType(List.class, MessageThread.class);
        mThreadProvider = new JsonRequestProvider<List<MessageThread>>(clientDispatcher, threadType);

        JavaType messageType = factory.constructCollectionType(List.class, Message.class);
        mMessageProvider = new JsonRequestProvider<List<Message>>(clientDispatcher, messageType);
    }

    public List<MessageThread> getThreads(final String topic) throws IOException {
        return mThreadProvider.get(makeUrl(topic));
    }

    public List<Message> getMessages(final String topic, final String threadId) throws IOException {
        return mMessageProvider.get(makeUrl(topic, threadId));
    }

    private String makeUrl(String... parts) {
        String url = mBackendURI;
        for (String part : parts) {
            url += "/" + part;
        }

        return url;
    }
}
