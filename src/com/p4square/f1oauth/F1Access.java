/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.f1oauth;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.util.Base64;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
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

    private static final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private final String mBaseUrl;
    private final String mMethod;

    private final OAuthHelper mOAuthHelper;

    private final Map<String, String> mAttributeIdByName;

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

        mAttributeIdByName = new HashMap<>();
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

    /**
     * @return An F1API authenticated by the given user.
     */
    public F1API getAuthenticatedApi(OAuthUser user) {
        return new AuthenticatedApi(user);
    }

    private class AuthenticatedApi implements F1API {
        private final OAuthUser mUser;

        public AuthenticatedApi(OAuthUser user) {
            mUser = user;
        }

        /**
         * Fetch information about a user.
         *
         * @param user The user to fetch information about.
         * @return An F1User object.
         */
        @Override
        public F1User getF1User(OAuthUser user) throws OAuthException, IOException {
            Request request = new Request(Method.GET, user.getLocation() + ".json");
            request.setChallengeResponse(mUser.getChallengeResponse());
            Response response = mOAuthHelper.getResponse(request);

            try {
                Status status = response.getStatus();
                if (status.isSuccess()) {
                    JacksonRepresentation<Map> entity =
                        new JacksonRepresentation<Map>(response.getEntity(), Map.class);
                    Map data = entity.getObject();
                    return new F1User(user, data);

                } else {
                    throw new OAuthException(status);
                }
            } finally {
                if (response.getEntity() != null) {
                    response.release();
                }
            }
        }

        @Override
        public Map<String, String> getAttributeList() throws F1Exception {
            // Note: this list is shared by all F1 users.
            synchronized (mAttributeIdByName) {
                if (mAttributeIdByName.size() == 0) {
                    // Reload attributes. Maybe it will be there now...
                    Request request = new Request(Method.GET,
                            mBaseUrl + "People/AttributeGroups.json");
                    request.setChallengeResponse(mUser.getChallengeResponse());
                    Response response = mOAuthHelper.getResponse(request);

                    Representation representation = response.getEntity();
                    try {
                        Status status = response.getStatus();
                        if (status.isSuccess()) {
                            JacksonRepresentation<Map> entity =
                                new JacksonRepresentation<Map>(response.getEntity(), Map.class);

                            Map attributeGroups = (Map) entity.getObject().get("attributeGroups");
                            List<Map> groups = (List<Map>) attributeGroups.get("attributeGroup");

                            for (Map group : groups) {
                                List<Map> attributes = (List<Map>) group.get("attribute");
                                if (attributes != null) {
                                    for (Map attribute : attributes) {
                                        String id = (String) attribute.get("@id");
                                        String name = ((String) attribute.get("name"));
                                        mAttributeIdByName.put(name.toLowerCase(), id);
                                        LOG.debug("Caching attribute '" + name
                                                + "' with id '" + id + "'");
                                    }
                                }
                            }
                        }

                    } catch (IOException e) {
                        throw new F1Exception("Could not parse AttributeGroups.", e);

                    } finally {
                        if (representation != null) {
                            representation.release();
                        }
                    }
                }

                return mAttributeIdByName;
            }
        }

        /**
         * Add an attribute to the user.
         *
         * @param user The user to add the attribute to.
         * @param attributeName The attribute to add.
         * @param attribute The attribute to add.
         */
        public boolean addAttribute(String userId, String attributeName, Attribute attribute)
                throws F1Exception {

            // Get the attribute id.
            String attributeId = getAttributeId(attributeName);
            if (attributeId == null) {
                throw new F1Exception("Could not find id for " + attributeName);
            }

            // Get Attribute Template
            Map attributeTemplate = null;

            {
                Request request = new Request(Method.GET,
                        mBaseUrl + "People/" + userId + "/Attributes/new.json");
                request.setChallengeResponse(mUser.getChallengeResponse());
                Response response = mOAuthHelper.getResponse(request);

                Representation representation = response.getEntity();
                try {
                    Status status = response.getStatus();
                    if (status.isSuccess()) {
                        JacksonRepresentation<Map> entity =
                            new JacksonRepresentation<Map>(response.getEntity(), Map.class);
                        attributeTemplate = entity.getObject();

                    } else {
                        throw new F1Exception("Failed to retrieve attribute template: "
                                + status);
                    }

                } catch (IOException e) {
                    throw new F1Exception("Could not parse attribute template.", e);

                } finally {
                    if (representation != null) {
                        representation.release();
                    }
                }
            }

            if (attributeTemplate == null) {
                throw new F1Exception("Could not retrieve attribute template.");
            }

            // Populate Attribute Template
            Map attributeMap = (Map) attributeTemplate.get("attribute");
            Map attributeGroup = (Map) attributeMap.get("attributeGroup");

            Map<String, String> attributeIdMap = new HashMap<>();
            attributeIdMap.put("@id", attributeId);
            attributeGroup.put("attribute", attributeIdMap);

            if (attribute.getStartDate() != null) {
                attributeMap.put("startDate", DATE_FORMAT.format(attribute.getStartDate()));
            }

            if (attribute.getStartDate() != null) {
                attributeMap.put("endDate", DATE_FORMAT.format(attribute.getStartDate()));
            }

            attributeMap.put("comment", attribute.getComment());

            // POST new attribute
            Status status;
            {
                Request request = new Request(Method.POST,
                        mBaseUrl + "People/" + userId + "/Attributes.json");
                request.setChallengeResponse(mUser.getChallengeResponse());
                request.setEntity(new JacksonRepresentation<Map>(attributeTemplate));
                Response response = mOAuthHelper.getResponse(request);

                Representation representation = response.getEntity();
                try {
                    status = response.getStatus();

                    if (status.isSuccess()) {
                        return true;
                    }

                } finally {
                    if (representation != null) {
                        representation.release();
                    }
                }
            }

            LOG.debug("addAttribute failed POST: " + status);
            return false;
        }

        /**
         * @return an attribute id for the given attribute name.
         */
        private String getAttributeId(String attributeName) throws F1Exception {
            Map<String, String> attributeMap = getAttributeList();

            return attributeMap.get(attributeName.toLowerCase());
        }

    }
}
