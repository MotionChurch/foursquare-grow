/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.restlet.oauth;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;

/**
 * Token wraps the two Strings which make up an OAuth Token: the public
 * component and the private component.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class Token {
    private final String mToken;
    private final String mSecret;

    public Token(String token, String secret) {
        mToken = token;
        mSecret = secret;
    }

    /**
     * @return the public component.
     */
    public String getToken() {
        return mToken;
    }

    /**
     * @return the secret component.
     */
    public String getSecret() {
        return mSecret;
    }

    @Override
    public String toString() {
        return mToken + "&" + mSecret;
    }

    /**
     * Generate a ChallengeResponse based on this Token.
     *
     * @return a ChallengeResponse object using the OAUTH ChallengeScheme.
     */
    public ChallengeResponse getChallengeResponse() {
        return new ChallengeResponse(ChallengeScheme.HTTP_OAUTH, mToken, mSecret);
    }
}
