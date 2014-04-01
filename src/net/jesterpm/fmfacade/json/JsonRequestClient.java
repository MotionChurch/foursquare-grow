/*
 * Copyright 2013 Jesse Morgan
 */

package net.jesterpm.fmfacade.json;

import java.util.Map;

import java.io.IOException;

import org.apache.log4j.Logger;

import org.restlet.data.Status;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;

import org.restlet.ext.jackson.JacksonRepresentation;

/**
 * 
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class JsonRequestClient {
    private final Restlet mDispatcher;

    public JsonRequestClient(Restlet dispatcher) {
        mDispatcher = dispatcher;
    }

    /**
     * Perform a GET request for the given URI and parse the response as a
     * JSON map.
     *
     * @return A JsonResponse object which can be used to retrieve the
     *         response as a JSON map.
     */
    public JsonResponse get(final String uri) {
        final Request request = new Request(Method.GET, uri);
        final Response response = mDispatcher.handle(request);

        return new JsonResponse(response);
    }

    /**
     * Perform a PUT request for the given URI and parse the response as a
     * JSON map.
     *
     * @return A JsonResponse object which can be used to retrieve the
     *         response as a JSON map.
     */
    public JsonResponse put(final String uri, Representation entity) {
        final Request request = new Request(Method.PUT, uri);
        request.setEntity(entity);

        final Response response = mDispatcher.handle(request);
        return new JsonResponse(response);
    }

    /**
     * Perform a PUT request for the given URI and parse the response as a
     * JSON map.
     *
     * @return A JsonResponse object which can be used to retrieve the
     *         response as a JSON map.
     */
    public JsonResponse put(final String uri, Map map) {
        return put(uri, new JacksonRepresentation<Map>(map));
    }

    /**
     * Perform a POST request for the given URI and parse the response as a
     * JSON map.
     *
     * @return A JsonResponse object which can be used to retrieve the
     *         response as a JSON map.
     */
    public JsonResponse post(final String uri, Representation entity) {
        final Request request = new Request(Method.POST, uri);
        request.setEntity(entity);

        final Response response = mDispatcher.handle(request);
        return new JsonResponse(response);
    }
    
    /**
     * Perform a POST request for the given URI and parse the response as a
     * JSON map.
     *
     * @return A JsonResponse object which can be used to retrieve the
     *         response as a JSON map.
     */
    public JsonResponse post(final String uri, Map map) {
        return post(uri, new JacksonRepresentation<Map>(map));
    }
    
    /**
     * Perform a DELETE request for the given URI.
     *
     * @return A JsonResponse object with the status of the request.
     */
    public JsonResponse delete(final String uri) {
        final Request request = new Request(Method.DELETE, uri);
        final Response response = mDispatcher.handle(request);
        return new JsonResponse(response);
    }
}
