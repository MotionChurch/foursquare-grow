/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.f1oauth;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.p4square.restlet.oauth.OAuthException;
import com.p4square.restlet.oauth.OAuthUser;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.security.Verifier;

/**
 * Restlet Verifier for F1 2nd Party Authentication
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SecondPartyVerifier implements Verifier {
    private static final Logger LOG = Logger.getLogger(SecondPartyVerifier.class);

    private final Restlet mDispatcher;
    private final F1Access mHelper;

    public SecondPartyVerifier(Context context, F1Access helper) {
        if (helper == null) {
            throw new IllegalArgumentException("Helper can not be null.");
        }

        mDispatcher = context.getClientDispatcher();
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
            OAuthUser ouser = mHelper.getAccessToken(username, password);

            // Once we have a user, fetch the people record to get the user id.
            F1User user = mHelper.getAuthenticatedApi(ouser).getF1User(ouser);
            user.setEmail(username);

            // This seems like a hack... but it'll work
            request.getClientInfo().setUser(user);

            return RESULT_VALID;

        } catch (Exception e) {
            LOG.info("OAuth Exception: " + e, e);
        }

        return RESULT_INVALID; // Invalid credentials
    }

}
