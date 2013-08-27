/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend.session;

import org.apache.log4j.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.Authenticator;

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SessionCookieAuthenticator extends Authenticator {
    private static final Logger LOG = Logger.getLogger(SessionCookieAuthenticator.class);

    private static final String COOKIE_NAME  = "S";

    private final Sessions mSessions;

    public SessionCookieAuthenticator(Context context, boolean optional, Sessions sessions) {
        super(context, optional);

        mSessions = sessions;
    }

    protected boolean authenticate(Request request, Response response) {
        final String cookie = request.getCookies().getFirstValue(COOKIE_NAME);

        if (request.getClientInfo().isAuthenticated()) {
            // Request is already authenticated... create session if it doesn't exist.
            if (cookie == null) {
                Session s = mSessions.create(request.getClientInfo().getUser());
                response.getCookieSettings().add(COOKIE_NAME, s.getId());
            }

            return true;

        } else {
            // Check for authentication cookie
            if (cookie != null) {
                LOG.debug("Got cookie: " + cookie);

                Session s = mSessions.get(cookie);
                if (s != null) {
                    request.getClientInfo().setUser(s.getUser());
                    return true;
                }
            }

            return false;
        }
    }

}
