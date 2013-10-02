/*
 * Copyright 2013 Jesse Morgan
 */

package net.jesterpm.restlet.oauth;

import java.net.URLEncoder;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;

/**
 * Helper Class for OAuth 1.0 Authentication.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public abstract class OAuthHelper {
    private final Restlet mDispatcher;
    private final Token mConsumerToken;

    /**
     * Create a new OAuth Helper.
     * As currently implemented, there can only be one OAuthHelper per Restlet
     * Engine since this class registers its own provider for the OAuth
     * authentication protocol.
     *
     * FIXME: This could be improved by making OAuthAuthenticationHelper and
     * maybe Token aware of multiple service providers.
     *
     * @param context The restlet context which provides a ClientDispatcher.
     * @param consumerKey The OAuth consumer key for this application.
     * @param consumerSecret the OAuth consumer secret for this application.
     */
    public OAuthHelper(Context context, String consumerKey, String consumerSecret) {
        mDispatcher = context.getClientDispatcher();
        mConsumerToken = new Token(consumerKey, consumerSecret);

        Engine.getInstance().getRegisteredAuthenticators().add(new OAuthAuthenticatorHelper(mConsumerToken));
    }

    /**
     * @return the URL for the initial RequestToken request.
     */
    protected abstract String getRequestTokenUrl();

    /**
     * Request a RequestToken.
     *
     * @return a Token containing the RequestToken.
     * @throws OAuthException if the request fails.
     */
    public Token getRequestToken() throws OAuthException {
        Request request = new Request(Method.GET, getRequestTokenUrl());
        request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_OAUTH));

        Response response = mDispatcher.handle(request);

        return processTokenRequest(response);
    }

    /**
     * @return the URL to redirect the user to for Authentication.
     */
    public abstract String getLoginUrl(Token requestToken, String callback);

    /**
     * @return the URL for the AccessToken request.
     */
    protected abstract String getAccessTokenUrl();

    /**
     * Request an AccessToken for a previously authenticated RequestToken.
     *
     * @return an OAuthUser object containing the AccessToken.
     * @throws OAuthException if the request fails.
     */
    public OAuthUser getAccessToken(Token requestToken) throws OAuthException {
        Request request = new Request(Method.GET, getAccessTokenUrl());
        request.setChallengeResponse(requestToken.getChallengeResponse());

        return processAccessTokenRequest(request);
    }

    /**
     * Helper method to decode the token returned from an OAuth Request.
     *
     * @param response The Response object from the Request.
     * @return the Token from the oauth_token and oauth_token_secret parameters.
     * @throws OAuthException is the server reported an error.
     */
    protected Token processTokenRequest(Response response) throws OAuthException {
        Status status = response.getStatus();
        Representation entity = response.getEntity();

        try {
            if (status.isSuccess()) {
                Form form = new Form(entity);
                String token = form.getFirstValue("oauth_token");
                String secret = form.getFirstValue("oauth_token_secret");

                return new Token(token, secret);

            } else {
                throw new OAuthException(status);
            }
        } finally {
            entity.release();
        }
    }

    /**
     * Helper method to create an OAuthUser from the AccessToken request.
     *
     * The User's identifier is set to the Content-Location header, if present.
     *
     * @param response The Response to the AccessToken Request.
     * @return An OAuthUser object wrapping the AccessToken.
     * @throws OAuthException if the request failed.
     */
    protected OAuthUser processAccessTokenRequest(Request request) throws OAuthException {
        Response response = getResponse(request);
        Token accessToken = processTokenRequest(response);

        Reference ref = response.getEntity().getLocationRef();
        if (ref != null) {
            return new OAuthUser(ref.toString(), accessToken);

        } else {
            return new OAuthUser(accessToken);
        }
    }

    /**
     * Helper method to get a Response for a Request.
     */
    protected Response getResponse(Request request) {
        return mDispatcher.handle(request);
    }
}
