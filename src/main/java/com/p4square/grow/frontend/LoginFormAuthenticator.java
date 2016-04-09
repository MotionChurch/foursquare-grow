/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend;

import org.apache.log4j.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.security.Authenticator;
import org.restlet.security.Verifier;

/**
 * LoginFormAuthenticator changes
 *
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class LoginFormAuthenticator extends Authenticator {
    private static final Logger LOG = Logger.getLogger(LoginFormAuthenticator.class);

    private final Verifier mVerifier;

    private String mLoginPage    = "/login.html";
    private String mLoginPostUrl = "/authenticate";
    private String mDefaultRedirect = "/index.html";

    public LoginFormAuthenticator(Context context, boolean optional, Verifier verifier) {
        super(context, false, optional, null);

        mVerifier = verifier;
    }

    public void setLoginFormUrl(String url) {
        mLoginPage = url;
    }

    public void setLoginPostUrl(String url) {
        mLoginPostUrl = url;
    }

    public void setDefaultPage(String url) {
        mDefaultRedirect = url;
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        if (!isLoginAttempt(request) && request.getClientInfo().isAuthenticated()) {
            // TODO: Logout
            LOG.debug("Already authenticated. Skipping");
            return CONTINUE;

        } else {
            return super.beforeHandle(request, response);
        }
    }


    @Override
    protected boolean authenticate(Request request, Response response) {
        boolean isLoginAttempt = isLoginAttempt(request);

        Form query = request.getOriginalRef().getQueryAsForm();
        String redirect = query.getFirstValue("redirect");
        if (redirect == null || redirect.length() == 0) {
            if (isLoginAttempt) {
                redirect = mDefaultRedirect;
            } else {
                redirect = request.getResourceRef().getPath();
            }
        }

        boolean authenticationFailed = false;

        if (isLoginAttempt) {
            LOG.debug("Attempting authentication");

            // Process login form
            final Form form = new Form(request.getEntity());
            final String email = form.getFirstValue("email");
            final String password = form.getFirstValue("password");

            boolean authenticated = false;

            if (email != null && !"".equals(email) &&
                password != null && !"".equals(password)) {

                LOG.debug("Got login request from " + email);

                request.setChallengeResponse(
                    new ChallengeResponse(ChallengeScheme.HTTP_BASIC, email, password.toCharArray()));

                // We expect the verifier to setup the User object.
                int result = mVerifier.verify(request, response);
                if (result == Verifier.RESULT_VALID) {
                    return true;
                }
            }

            authenticationFailed = true;
        }

        if (!isOptional() || authenticationFailed) {
            Reference ref = new Reference(mLoginPage);
            ref.addQueryParameter("redirect", redirect);

            if (authenticationFailed) {
                ref.addQueryParameter("retry", "t");
            }

            LOG.debug("Redirecting to " + ref);
            response.redirectSeeOther(ref.toString());
        }
        LOG.debug("Failing authentication.");
        return false;
    }

    @Override
    protected int authenticated(Request request, Response response) {
        super.authenticated(request, response);

        Form query = request.getOriginalRef().getQueryAsForm();
        String redirect = query.getFirstValue("redirect");
        if (redirect == null || redirect.length() == 0) {
            redirect = mDefaultRedirect;
        }

        // TODO: Ensure redirect is a relative url.
        LOG.debug("Redirecting to " + redirect);
        response.redirectSeeOther(redirect);

        return CONTINUE;
    }

    private boolean isLoginAttempt(Request request) {
        String requestPath = request.getResourceRef().getPath();
        return request.getMethod() == Method.POST && mLoginPostUrl.equals(requestPath);
    }
}
