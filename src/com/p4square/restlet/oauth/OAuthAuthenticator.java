/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.restlet.oauth;

import org.apache.log4j.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.Authenticator;
import org.restlet.security.User;

/**
 * Authenticator which makes an OAuth request to authenticate the user.
 *
 * If this Authenticator is made optional than no requests are made to the
 * service provider.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class OAuthAuthenticator extends Authenticator {
    private static Logger LOG = Logger.getLogger(OAuthAuthenticator.class);

    private static final String OAUTH_TOKEN = "oauth_token";
    private static final String COOKIE_NAME = "oauth_secret";

    private final OAuthHelper mHelper;

    /**
     * Create a new Authenticator.
     *
     * @param Context the current context.
     * @param optional If true, unauthenticated users are allowed to continue.
     * @param helper The OAuthHelper which will help with the requests.
     */
    public OAuthAuthenticator(Context context, boolean optional, OAuthHelper helper) {
        super(context, false, optional, null);

        mHelper = helper;
    }

    protected boolean authenticate(Request request, Response response) {
        /*
         * The authentication workflow has three steps:
         *  1. Get RequestToken
         *  2. Authenticate the user
         *  3. Get AccessToken
         *
         * The authentication workflow is broken into two stages. In the first,
         * we generate the RequestToken (step 1) and redirect the user to the
         * authentication page. When the user comes back, we will request the
         * AccessToken (step 2).
         *
         * We determine which half we are in by the presence of the oauth_token
         * parameter in the query string.
         */

        final String token = request.getResourceRef().getQueryAsForm().getFirstValue(OAUTH_TOKEN);
        final String secret = request.getCookies().getFirstValue(COOKIE_NAME);

        try {
            if (token == null) {
                if (isOptional()) {
                    return false;
                }

                // 1. Get RequestToken
                Token requestToken = mHelper.getRequestToken();

                if (requestToken == null) {
                    return false;
                }

                // 2. Redirect user
                // TODO Encrypt cookie
                response.getCookieSettings().add(COOKIE_NAME, requestToken.getSecret());
                response.redirectSeeOther(mHelper.getLoginUrl(requestToken, request.getResourceRef().toString()));
                return false;

            } else {
                // 3. Get AccessToken
                Token requestToken = new Token(token, secret);
                User user = mHelper.getAccessToken(requestToken);
                request.getClientInfo().setUser(user);
                return true;
            }

        } catch (OAuthException e) {
            LOG.debug("Authentication failed: " + e);
            return false;
        }
    }
}
