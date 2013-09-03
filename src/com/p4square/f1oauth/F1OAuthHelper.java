/*
 * Copyright 2013 Jesse Morgan
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

import net.jesterpm.restlet.oauth.OAuthException;
import net.jesterpm.restlet.oauth.OAuthHelper;
import net.jesterpm.restlet.oauth.OAuthUser;
import net.jesterpm.restlet.oauth.Token;

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class F1OAuthHelper extends OAuthHelper {
    public enum UserType {
        WEBLINK, PORTAL;
    }

    private static final Logger LOG = Logger.getLogger(F1OAuthHelper.class);

    private static final String VERSION_STRING = "/v1/";
    private static final String REQUESTTOKEN_URL = "Tokens/RequestToken";
    private static final String AUTHORIZATION_URL = "Login";
    private static final String ACCESSTOKEN_URL= "Tokens/AccessToken";
    private static final String TRUSTED_ACCESSTOKEN_URL = "/AccessToken";

    private final String mBaseUrl;
    private final String mMethod;

    /**
     * @param method Either WeblinkUser or PortalUser.
     */
    public F1OAuthHelper(Context context, String consumerKey, String consumerSecret,
            String baseUrl, String churchCode, UserType userType) {
        super(context, consumerKey, consumerSecret);

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
    }

    /**
     * @return the URL for the initial RequestToken request.
     */
    protected String getRequestTokenUrl() {
        return mBaseUrl + REQUESTTOKEN_URL;
    }

    /**
     * @return the URL to redirect the user to for Authentication.
     */
    public String getLoginUrl(Token requestToken, String callback) {
        String loginUrl = mBaseUrl + mMethod + AUTHORIZATION_URL
                            + "?oauth_token=" + URLEncoder.encode(requestToken.getToken());

        if (callback != null) {
            loginUrl += "&oauth_callback=" + URLEncoder.encode(callback);
        }

        return loginUrl;
    }


    /**
     * @return the URL for the AccessToken request.
     */
    protected String getAccessTokenUrl() {
        return mBaseUrl + ACCESSTOKEN_URL;
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

        return processAccessTokenRequest(request);
    }

    public boolean createAccount(String firstname, String lastname, String email, String redirect)
            throws OAuthException {
        String req = String.format("{\n\"account\":{\n\"firstName\":\"%s\",\n"
                                 + "\"lastName\":\"%s\",\n\"email\":\"%s\",\n"
                                 + "\"urlRedirect\":\"%s\"\n}\n}",
                                 firstname, lastname, email, redirect);

        Request request = new Request(Method.POST, mBaseUrl + "Accounts");
        request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_OAUTH));
        request.setEntity(new StringRepresentation(req, MediaType.APPLICATION_JSON));

        Response response = getResponse(request);

        Status status = response.getStatus();
        if (Status.SUCCESS_NO_CONTENT.equals(status)) {
            return true;

        } else if (Status.CLIENT_ERROR_CONFLICT.equals(status)) {
            return false;

        } else {
            throw new OAuthException(status);
        }
    }
}
