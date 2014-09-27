/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.f1oauth;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.p4square.restlet.oauth.OAuthException;
import com.p4square.restlet.oauth.OAuthUser;

/**
 * F1 API methods which require an authenticated user.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public interface F1API {
    /**
     * Fetch information about a user.
     *
     * @param user The user to fetch information about.
     * @return An F1User object.
     */
    F1User getF1User(OAuthUser user) throws OAuthException, IOException;

    /**
     * Fetch a list of all attributes ids and names.
     *
     * @return A Map of attribute name to attribute id.
     */
    Map<String, String> getAttributeList() throws F1Exception;

    /**
     * Add an attribute to the user.
     *
     * @param user The user to add the attribute to.
     * @param attributeName The attribute to add.
     * @param attribute The attribute to add.
     */
    boolean addAttribute(String userId, Attribute attribute) throws F1Exception;

    /**
     * Return attributes assigned to user.
     *
     * A user may be assigned multiple attributes with the same name, thus even if
     * attributeName is specified, multiple attributes may be returned.
     *
     * @param userId The user to query.
     * @param attributeName A specific attribute to return, null for all.
     * @return A list of Attributes
     */
    List<Attribute> getAttribute(String userId, String attributeName) throws F1Exception;

}
