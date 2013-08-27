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
 * Authenticator which succeeds if a valid Session exists.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SessionCheckingAuthenticator extends Authenticator {
    private static final Logger LOG = Logger.getLogger(SessionCheckingAuthenticator.class);

    public SessionCheckingAuthenticator(Context context, boolean optional) {
        super(context, optional);
    }

    protected boolean authenticate(Request request, Response response) {
        Session s = Sessions.getInstance().get(request);

        if (s != null) {
            request.getClientInfo().setUser(s.getUser());
            return true;

        } else {
            return false;
        }
    }

}
