/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend;

import org.apache.log4j.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.Authenticator;
import org.restlet.security.User;

/**
 * LoginAuthenticator decrypts a cookie containing the user's session info
 * and makes that information available as the ClientInfo's User object.
 *
 * If this Authenticator is not optional, the user will be redirected to a
 * login page.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class LoginAuthenticator extends Authenticator {
    private static Logger cLog = Logger.getLogger(LoginAuthenticator.class);

    public static final String COOKIE_NAME = "growsession";

    private final String mLoginPage;

    public LoginAuthenticator(Context context, boolean optional, String loginPage) {
        super(context, optional);

        mLoginPage = loginPage;
    }

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
}
