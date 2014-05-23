/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.io.IOException;

import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;

import com.p4square.grow.backend.GrowBackend;
import com.p4square.grow.model.Banner;
import com.p4square.grow.provider.JsonEncodedProvider;
import com.p4square.grow.provider.Provider;

/**
 * Fetches or sets the banner string.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class BannerResource extends ServerResource {
    private static final Logger LOG = Logger.getLogger(BannerResource.class);

    public static final ObjectMapper MAPPER = JsonEncodedProvider.MAPPER;

    private Provider<String, String> mStringProvider;

    @Override
    public void doInit() {
        super.doInit();

        final GrowBackend backend = (GrowBackend) getApplication();
        mStringProvider = backend.getStringProvider();
    }

    /**
     * Handle GET Requests.
     */
    @Override
    protected Representation get() {
        String result = null;
        try {
            result = mStringProvider.get("banner");

        } catch (IOException e) {
            LOG.warn("Exception loading banner: " + e);
        }

        if (result == null || result.length() == 0) {
            result = "{\"html\":null}";
        }

        return new StringRepresentation(result);
    }

    /**
     * Handle PUT requests
     */
    @Override
    protected Representation put(Representation entity) {
        try {
            JacksonRepresentation<Banner> representation =
                new JacksonRepresentation<>(entity, Banner.class);
            representation.setObjectMapper(MAPPER);

            Banner banner = representation.getObject();

            mStringProvider.put("banner", MAPPER.writeValueAsString(banner));
            setStatus(Status.SUCCESS_NO_CONTENT);

        } catch (IOException e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return null;
    }
}
