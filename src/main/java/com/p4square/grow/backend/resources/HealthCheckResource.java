/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.io.IOException;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;

import org.apache.log4j.Logger;

import com.p4square.grow.backend.GrowBackend;
import com.p4square.grow.provider.Provider;

/**
 * Check if dependencies are healthy.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class HealthCheckResource extends ServerResource {
    private static final Logger LOG = Logger.getLogger(HealthCheckResource.class);

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
        try {
            // Try loading the banner string
            mStringProvider.get("banner");

            // If nothing exploded, we'll say it works.
            return new StringRepresentation("SUCCESS");

        } catch (IOException e) {
            LOG.warn("Health Check Failed: " + e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return new StringRepresentation("FAIL");
        }
    }
}
