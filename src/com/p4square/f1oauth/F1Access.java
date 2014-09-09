/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.f1oauth;

import java.net.URLEncoder;

import org.apache.log4j.Logger;

import org.restlet.Context;
import org.restlet.Response;
import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.util.Base64;
import org.restlet.representation.StringRepresentation;

import com.p4square.restlet.oauth.OAuthException;
import com.p4square.restlet.oauth.OAuthHelper;
import com.p4square.restlet.oauth.OAuthUser;
import com.p4square.restlet.oauth.Token;

/**
 * F1 API Access.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class F1Access {
    public enum UserType {
        WEBLINK, PORTAL;
    }

    private static final Logger LOG = Logger.getLogger(F1Access.class);

    private static final String VERSION_STRING = "/v1/";
    private static final String REQUESTTOKEN_URL = "Tokens/RequestToken";
    private static final String AUTHORIZATION_URL = "Login";
    private static final String ACCESSTOKEN_URL= "Tokens/AccessToken";
    private static final String TRUSTED_ACCESSTOKEN_URL = "/AccessToken";

    private final String mBaseUrl;
    private final String mMethod;

    private final OAuthHelper mOAuthHelper;

    /**
     */
    public F1Access(Context context, String consumerKey, String consumerSecret,
            String baseUrl, String churchCode, UserType userType) {

        switch (userType) {
            case WEBLINK:
                mMethod = "WeblinkUser";
                break;
            case PORTAL:
                mMethod = "PortalUser";
                break;
            default:
                throw new IllegalArgumentException("Unknown UserType");
        }

        mBaseUrl = "https://" + churchCode + "." + baseUrl + VERSION_STRING;

        // Create the OAuthHelper. This implicitly registers the helper to
        // handle outgoing requests which need OAuth authentication.
        mOAuthHelper = new OAuthHelper(context, consumerKey, consumerSecret) {
            @Override
            protected String getRequestTokenUrl() {
                return mBaseUrl + REQUESTTOKEN_URL;
            }

            @Override
            public String getLoginUrl(Token requestToken, String callback) {
                String loginUrl = mBaseUrl + mMethod + AUTHORIZATION_URL
                                    + "?oauth_token=" + URLEncoder.encode(requestToken.getToken());

                if (callback != null) {
                    loginUrl += "&oauth_callback=" + URLEncoder.encode(callback);
                }

                return loginUrl;
            }

            @Override
            protected String getAccessTokenUrl() {
                return mBaseUrl + ACCESSTOKEN_URL;
            }
        };

    }

    /**
     * Request an AccessToken for a particular username and password.
     *
     * This is an F1 extension to OAuth:
     * http://developer.fellowshipone.com/docs/v1/Util/AuthDocs.help#2creds
     */
    public OAuthUser getAccessToken(String username, String password) throws OAuthException {
        Request request = new Request(Method.POST, mBaseUrl +  mMethod + TRUSTED_ACCESSTOKEN_URL);
        request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_OAUTH));

        String base64String = Base64.encode((username + " " + password).getBytes(), false);
        request.setEntity(new StringRepresentation(base64String));

        return mOAuthHelper.processAccessTokenRequest(request);
    }

    /**
     * Create a new Account.
     *
     * @param firstname The user's first name.
     * @param lastname The user's last name.
     * @param email The user's email address.
     * @param redirect The URL to send the user to after confirming his address.
     *
     * @return true if created, false if the account already exists.
     */
    public boolean createAccount(String firstname, String lastname, String email, String redirect)
            throws OAuthException {
        String req = String.format("{\n\"account\":{\n\"firstName\":\"%s\",\n"
                                 + "\"lastName\":\"%s\",\n\"email\":\"%s\",\n"
                                 + "\"urlRedirect\":\"%s\"\n}\n}",
                                 firstname, lastname, email, redirect);

        Request request = new Request(Method.POST, mBaseUrl + "Accounts");
        request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_OAUTH));
        request.setEntity(new StringRepresentation(req, MediaType.APPLICATION_JSON));

        Response response = mOAuthHelper.getResponse(request);

        Status status = response.getStatus();
        if (Status.SUCCESS_NO_CONTENT.equals(status)) {
            return true;

        } else if (Status.CLIENT_ERROR_CONFLICT.equals(status)) {
            return false;

        } else {
            throw new OAuthException(status);
        }
    }

    /*
    public addAttribute(Attribute attribute, String comment) {
        String baseUrl = getBaseUrl();
        Map newAttributeTemplate = null;

        // Get Attribute Template
        Request request = new Request(Method.GET,
                baseUrl + "People/" + getIdentifier() + "/Attributes/new.json");
        request.setChallengeResponse(getChallengeResponse());
        Response response = getContext().getClientDispatcher().handle(request);

        Representation representation = response.getEntity();
        try {
            Status status = response.getStatus();
            if (status.isSuccess()) {
                JacksonRepresentation<Map> entity = new JacksonRepresentation<Map>(response.getEntity(), Map.class);
                newAttributeTemplate = entity.getObject();
            }

        } finally {
            if (representation != null) {
                representation.release();
            }
        }

        if (newAttributeTemplate == null) {
            LOG.error("Could not retrieve attribute template!");
            return;
        }

        // Populate Attribute Template


        // POST new attribute
        Request request = new Request(Method.POST,
                baseUrl + "People/" + getIdentifier() + "/Attributes.json");
        request.setChallengeResponse(getChallengeResponse());
        Response response = getContext().getClientDispatcher().handle(request);

        Representation representation = response.getEntity();
        try {
            Status status = response.getStatus();
            if (status.isSuccess()) {
                JacksonRepresentation<Map> entity = new JacksonRepresentation<Map>(response.getEntity(), Map.class);
                newAttributeTemplate = entity.getObject();
            }

        } finally {
            if (representation != null) {
                representation.release();
            }
        }

        if (newAttributeTemplate == null) {
            LOG.error("Could retrieve attribute template!");
            return;
        }

    }
    */
}
