/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.f1oauth;

import org.apache.log4j.Logger;

import com.p4square.restlet.oauth.OAuthException;
import com.p4square.restlet.oauth.OAuthUser;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.Authenticator;

/**
 * Restlet Authenticator for 2nd
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SecondPartyAuthenticator extends Authenticator {
    private static final Logger LOG = Logger.getLogger(SecondPartyAuthenticator.class);

    private final F1OAuthHelper mHelper;

    public SecondPartyAuthenticator(Context context, boolean optional, F1OAuthHelper helper) {
        super(context, optional);

        mHelper = helper;
    }

    protected boolean authenticate(Request request, Response response) {
        if (request.getChallengeResponse() == null) {
            return false; // no credentials
        }

        String username = request.getChallengeResponse().getIdentifier();
        String password = new String(request.getChallengeResponse().getSecret());

        try {
            OAuthUser user = mHelper.getAccessToken(username, password);
            request.getClientInfo().setUser(user);

            return true;

        } catch (OAuthException e) {
            LOG.info("OAuth Exception: " + e);
        }

        return false; // Invalid credentials
    }
}
