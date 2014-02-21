/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend;

import java.io.IOException;

import com.fasterxml.jackson.databind.JavaType;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import com.p4square.grow.provider.JsonEncodedProvider;

/**
 * Fetch a JSON object via a Request.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class JsonRequestProvider<V> extends JsonEncodedProvider<String, V> {

    private final Restlet mDispatcher;

    public JsonRequestProvider(Restlet dispatcher, Class<V> clazz) {
        super(clazz);

        mDispatcher = dispatcher;
    }

    public JsonRequestProvider(Restlet dispatcher, JavaType type) {
        super(type);

        mDispatcher = dispatcher;
    }

    @Override
    public V get(String url) throws IOException {
        Request request = new Request(Method.GET, url);
        Response response = mDispatcher.handle(request);
        Representation representation = response.getEntity();

        if (!response.getStatus().isSuccess()) {
            if (representation != null) {
                representation.release();
            }

            throw new IOException("Could not get object. " + response.getStatus());
        }

        return decode(representation.getText());
    }

    @Override
    public void put(String url, V obj) throws IOException {
        final Request request = new Request(Method.PUT, url);
        request.setEntity(new StringRepresentation(encode(obj)));

        final Response response = mDispatcher.handle(request);

        if (!response.getStatus().isSuccess()) {
            throw new IOException("Could not put object. " + response.getStatus());
        }

    }

}
