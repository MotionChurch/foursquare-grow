/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend.session;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.restlet.security.User;

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class Session {
    private static final long LIFETIME = 86400;

    private final String mSessionId;
    private final User mUser;
    private final Map<String, String> mData;
    private long mExpires;

    Session(User user) {
        mUser = user;
        mSessionId = UUID.randomUUID().toString();
        mExpires = System.currentTimeMillis() + LIFETIME;
        mData = new HashMap<String, String>();
    }

    void touch() {
        mExpires = System.currentTimeMillis() + LIFETIME;
    }

    boolean isExpired() {
        return System.currentTimeMillis() > mExpires;
    }

    public String getId() {
        return mSessionId;
    }

    public String get(String key) {
        return mData.get(key);
    }

    public void put(String key, String value) {
        mData.put(key, value);
    }

    public User getUser() {
        return mUser;
    }
}
