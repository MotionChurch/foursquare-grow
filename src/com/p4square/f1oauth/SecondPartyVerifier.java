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
import org.restlet.security.Verifier;

/**
 * Restlet Verifier for F1 2nd Party Authentication
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SecondPartyVerifier implements Verifier {
    private static final Logger LOG = Logger.getLogger(SecondPartyVerifier.class);

    private final F1OAuthHelper mHelper;

    public SecondPartyVerifier(F1OAuthHelper helper) {
        if (helper == null) {
            throw new IllegalArgumentException("Helper can not be null.");
        }

        mHelper = helper;
    }

    @Override
    public int verify(Request request, Response response) {
        if (request.getChallengeResponse() == null) {
            return RESULT_MISSING; // no credentials
        }

        String username = request.getChallengeResponse().getIdentifier();
        String password = new String(request.getChallengeResponse().getSecret());

        try {
            OAuthUser user = mHelper.getAccessToken(username, password);
            user.setIdentifier(username);
            user.setEmail(username);

            // This seems like a hack... but it'll work
            request.getClientInfo().setUser(user);

            return RESULT_VALID;

        } catch (OAuthException e) {
            LOG.info("OAuth Exception: " + e, e);
        }

        return RESULT_INVALID; // Invalid credentials
    }
}
