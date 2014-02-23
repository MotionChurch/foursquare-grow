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

    // TODO: Elegantly merge the List and individual providers.
    private final JsonRequestProvider<List<MessageThread>> mThreadsProvider;
    private final JsonRequestProvider<MessageThread> mThreadProvider;

    private final JsonRequestProvider<List<Message>> mMessagesProvider;
    private final JsonRequestProvider<Message> mMessageProvider;

    public FeedData(final Context context, final Config config) {
        mConfig = config;
        mBackendURI = mConfig.getString("backendUri", "riap://component/backend") + "/feed";

        Restlet clientDispatcher = context.getClientDispatcher();

        TypeFactory factory = JsonEncodedProvider.MAPPER.getTypeFactory();

        JavaType threadType = factory.constructCollectionType(List.class, MessageThread.class);
        mThreadsProvider = new JsonRequestProvider<List<MessageThread>>(clientDispatcher, threadType);
        mThreadProvider = new JsonRequestProvider<MessageThread>(clientDispatcher, MessageThread.class);

        JavaType messageType = factory.constructCollectionType(List.class, Message.class);
        mMessagesProvider = new JsonRequestProvider<List<Message>>(clientDispatcher, messageType);
        mMessageProvider = new JsonRequestProvider<Message>(clientDispatcher, Message.class);
    }

    public List<MessageThread> getThreads(final String topic) throws IOException {
        return mThreadsProvider.get(makeUrl(topic));
    }

    public List<Message> getMessages(final String topic, final String threadId) throws IOException {
        return mMessagesProvider.get(makeUrl(topic, threadId));
    }

    public void createThread(final String topic, final Message message) throws IOException {
        MessageThread thread = new MessageThread();
        thread.setMessage(message);

        mThreadProvider.post(makeUrl(topic), thread);
    }

    public void createResponse(final String topic, final String thread, final Message message)
        throws IOException {

        mMessageProvider.post(makeUrl(topic, thread), message);
    }

    private String makeUrl(String... parts) {
        String url = mBackendURI;
        for (String part : parts) {
            url += "/" + part;
        }

        return url;
    }
}
