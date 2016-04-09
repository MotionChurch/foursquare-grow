/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.f1oauth;

import java.util.Map;

import com.p4square.restlet.oauth.OAuthException;
import com.p4square.restlet.oauth.OAuthUser;

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class F1User extends OAuthUser {
    public static final String ID = "@id";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String ICODE = "@iCode";

    private final Map mData;

    /**
     * Copy the user information from user into a new F1User.
     *
     * @param user Original user.
     * @param data F1 Person Record.
     * @throws IllegalStateException if data.get("person") is null.
     */
    public F1User(OAuthUser user, Map data) {
        super(user.getLocation(), user.getToken());

        mData = (Map) data.get("person");
        if (mData == null) {
            throw new IllegalStateException("Bad data");
        }

        setIdentifier(getString(ID));
        setFirstName(getString(FIRST_NAME));
        setLastName(getString(LAST_NAME));
    }

    /**
     * Get a String from the map.
     *
     * @param key The map key.
     * @return The value associated with the key, or null.
     */
    public String getString(String key) {
        Object blob = get(key);

        if (blob instanceof String) {
            return (String) blob;

        } else {
            return null;
        }
    }

    /**
     * Fetch an object from the F1 record.
     *
     * @param key The map key
     * @return The object in the map or null.
     */
    public Object get(String key) {
        return mData.get(key);
    }
}
