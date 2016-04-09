/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.feed;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.p4square.grow.model.MessageThread;
import com.p4square.grow.model.Message;
import com.p4square.grow.provider.CollectionProvider;

/**
 * Implementing this interface indicates you can provide a data source for the Feed.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public interface FeedDataProvider {
    public static final Collection<String> TOPICS = Collections.unmodifiableCollection(
            Arrays.asList(new String[] { "seeker", "believer", "disciple", "teacher", "leader" }));

    /**
     * @return a CollectionProvider of Threads.
     */
    CollectionProvider<String, String, MessageThread> getThreadProvider();

    /**
     * @return a CollectionProvider of Messages.
     */
    CollectionProvider<String, String, Message> getMessageProvider();
}
