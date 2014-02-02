/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.f1oauth;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import net.jesterpm.restlet.oauth.OAuthException;
import net.jesterpm.restlet.oauth.OAuthUser;

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
    private final F1OAuthHelper mHelper;

    public SecondPartyVerifier(Context context, F1OAuthHelper helper) {
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
            F1User user = getF1User(ouser);
            user.setEmail(username);

            // This seems like a hack... but it'll work
            request.getClientInfo().setUser(user);

            return RESULT_VALID;

        } catch (Exception e) {
            LOG.info("OAuth Exception: " + e, e);
        }

        return RESULT_INVALID; // Invalid credentials
    }

    private F1User getF1User(OAuthUser user) throws OAuthException, IOException {
        Request request = new Request(Method.GET, user.getLocation() + ".json");
        request.setChallengeResponse(user.getChallengeResponse());
        Response response = mDispatcher.handle(request);

        Status status = response.getStatus();
        if (status.isSuccess()) {
            JacksonRepresentation<Map> entity = new JacksonRepresentation<Map>(response.getEntity(), Map.class);
            Map data = entity.getObject();
            return new F1User(user, data);

        } else {
            throw new OAuthException(status);
        }
    }
}
