/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.restlet.oauth;

import org.restlet.data.ChallengeResponse;
import org.restlet.security.User;

/**
 * Simple User object which also contains an OAuth AccessToken.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class OAuthUser extends User {
    private final Token mToken;
    private final String mContentLocation;

    public OAuthUser(Token token) {
        this(null, token);
    }

    public OAuthUser(String location, Token token) {
        super();
        mToken = token;
        mContentLocation = location;
    }

    /**
     * @return the Location associated with the user.
     */
    public String getLocation() {
        return mContentLocation;
    }

    /**
     * @return The AccessToken.
     */
    public Token getToken() {
        return mToken;
    }

    /**
     * Convenience method for getToken().getChallengeResponse().
     * @return A ChallengeResponse based upon the access token.
     */
    public ChallengeResponse getChallengeResponse() {
        return mToken.getChallengeResponse();
    }
}
