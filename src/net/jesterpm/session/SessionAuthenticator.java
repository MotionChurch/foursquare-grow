/*
 * Copyright 2013 Jesse Morgan
 */

package net.jesterpm.session;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.Authenticator;
import org.restlet.security.User;

/**
 * 
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SessionAuthenticator /*extends Authenticator*/ {
    /*
    @Override
    protected boolean authenticate(Request request, Response response) {
        // Check for authentication cookie
        final String cookie = request.getCookies().getFirstValue(COOKIE_NAME);
        if (cookie != null) {
            cLog.debug("Got cookie: " + cookie);
            // TODO Decrypt user info
            User user = new User(cookie);
            request.getClientInfo().setUser(user);
            return true;
        }

        // Challenge the user if not authenticated
        response.redirectSeeOther(mLoginPage);
        return false;
    }
    */
}
