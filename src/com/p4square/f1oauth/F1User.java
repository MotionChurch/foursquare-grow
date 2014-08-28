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

    private final String mBaseUrl;
    private final Map mData;

    /**
     * Copy the user information from user into a new F1User.
     *
     * @param user Original user.
     * @param data F1 Person Record.
     * @throws IllegalStateException if data.get("person") is null.
     */
    public F1User(String baseUrl, OAuthUser user, Map data) {
        super(user.getLocation(), user.getToken());

        mBaseUrl = baseUrl;
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

    /**
     * @return the F1 API base url.
     */
    public String getBaseUrl() {
        return mBaseUrl;
    }

    /*
    public addAttribute(Attribute attribute, String comment) {
        String baseUrl = getBaseUrl();
        Map newAttributeTemplate = null;

        // Get Attribute Template
        Request request = new Request(Method.GET,
                baseUrl + "People/" + getIdentifier() + "/Attributes/new.json");
        request.setChallengeResponse(getChallengeResponse());
        Response response = getContext().getClientDispatcher().handle(request);

        Representation representation = response.getEntity();
        try {
            Status status = response.getStatus();
            if (status.isSuccess()) {
                JacksonRepresentation<Map> entity = new JacksonRepresentation<Map>(response.getEntity(), Map.class);
                newAttributeTemplate = entity.getObject();
            }

        } finally {
            if (representation != null) {
                representation.release();
            }
        }

        if (newAttributeTemplate == null) {
            LOG.error("Could not retrieve attribute template!");
            return;
        }

        // Populate Attribute Template


        // POST new attribute
        Request request = new Request(Method.POST,
                baseUrl + "People/" + getIdentifier() + "/Attributes.json");
        request.setChallengeResponse(getChallengeResponse());
        Response response = getContext().getClientDispatcher().handle(request);

        Representation representation = response.getEntity();
        try {
            Status status = response.getStatus();
            if (status.isSuccess()) {
                JacksonRepresentation<Map> entity = new JacksonRepresentation<Map>(response.getEntity(), Map.class);
                newAttributeTemplate = entity.getObject();
            }

        } finally {
            if (representation != null) {
                representation.release();
            }
        }

        if (newAttributeTemplate == null) {
            LOG.error("Could retrieve attribute template!");
            return;
        }

    }
    */
}
