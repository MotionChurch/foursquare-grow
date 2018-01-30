/*
 * Copyright 2018 Jesse Morgan
 */

package com.p4square.grow.frontend;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import org.apache.log4j.Logger;

import com.p4square.grow.config.Config;
import org.restlet.resource.ServerResource;

/**
 * This page verifies all of the dependencies are working.
 * If so, it returns a 200, otherwise a 500.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class HealthCheckPage extends ServerResource {
    private static final Logger LOG = Logger.getLogger(HealthCheckPage.class);

    private GrowFrontend mGrowFrontend;
    private Config mConfig;

    @Override
    public void doInit() {
        super.doInit();

        mGrowFrontend = (GrowFrontend) getApplication();
        mConfig = mGrowFrontend.getConfig();
    }

    /**
     * Health check
     */
    @Override
    protected Representation get() {
        try {
            // Check the backend
            boolean backendOk = checkBackend();

            // Check the Third-Party Integration driver
            boolean integrationOk = mGrowFrontend.getThirdPartyIntegrationFactory().doHealthCheck();

            if (backendOk && integrationOk) {
                return new StringRepresentation("SUCCESS");
            }

        } catch (Exception e) {
            LOG.fatal("Health check exception: " + e.getMessage(), e);
        }

        // Something went wrong...
        setStatus(Status.SERVER_ERROR_INTERNAL);
        return new StringRepresentation("FAIL");
    }

    private boolean checkBackend() {
        final Request request = new Request(Method.GET, getBackendEndpoint() + "/ping");
        final Response response = getContext().getClientDispatcher().handle(request);

        if (response.getStatus().isSuccess()) {
            return true;
        } else {
            LOG.warn("Backend health check failed. Got " + response.getStatus().toString());
            return false;
        }
    }

    /**
     * @return The backend endpoint URI
     */
    private String getBackendEndpoint() {
        return mConfig.getString("backendUri", "riap://component/backend");
    }
}
