/*
 * Copyright 2013 Jesse Morgan
 */

package net.jesterpm.fmfacade.json;

import java.util.Map;

import java.io.IOException;

import org.restlet.data.Status;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.Response;

import org.restlet.ext.jackson.JacksonRepresentation;

/**
 * JsonResponse wraps a Restlet Response object and parses the entity, if any,
 * as a JSON map.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class JsonResponse {
    private final Response mResponse;
    private final Representation mRepresentation;

    private Map<String, Object> mMap;

    JsonResponse(Response response) {
        mResponse = response;
        mRepresentation = response.getEntity();
        mMap = null;

        if (!response.getStatus().isSuccess()) {
            if (mRepresentation != null) {
                mRepresentation.release();
            }
        }
    }

    /**
     * @return the Status info from the response.
     */
    public Status getStatus() {
        return mResponse.getStatus();
    }

    /**
     * @return the Reference for a redirect.
     */
    public Reference getRedirectLocation() {
        return mResponse.getLocationRef();
    }

    /**
     * Return the parsed json map from the response.
     */
    public Map<String, Object> getMap() throws ClientException {
        if (mMap == null) {
            Representation representation = mRepresentation;

            // Parse response
            if (representation == null) {
                return null;
            }

            JacksonRepresentation<Map> mapRepresentation;
            if (representation instanceof JacksonRepresentation) {
                mapRepresentation = (JacksonRepresentation<Map>) representation;
            } else {
                mapRepresentation = new JacksonRepresentation<Map>(
                        representation, Map.class);
            }

            try {
                mMap = (Map<String, Object>) mapRepresentation.getObject();

            } catch (IOException e) {
                throw new ClientException("Failed to parse response: " + e.getMessage(), e);
            }
        }

        return mMap;
    }

}
