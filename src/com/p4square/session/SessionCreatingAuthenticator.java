/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.session;

import org.apache.log4j.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.Authenticator;
import org.restlet.security.User;

/**
 * Authenticator which creates a Session for the request and adds a cookie
 * to the response.
 *
 * The Request MUST be Authenticated and MUST have a User object associated.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SessionCreatingAuthenticator extends Authenticator {
    private static final Logger LOG = Logger.getLogger(SessionCreatingAuthenticator.class);

    public SessionCreatingAuthenticator(Context context) {
        super(context, true);
    }

    protected boolean authenticate(Request request, Response response) {
        if (Sessions.getInstance().get(request) != null) {
            return true;
        }

        User user = request.getClientInfo().getUser();

        if (request.getClientInfo().isAuthenticated() && user != null) {
            Sessions.getInstance().create(request, response);
            LOG.debug(response);
            return true;
        }

        return false;
    }

}
