/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend;

import java.util.Map;

import freemarker.template.Template;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.ext.freemarker.TemplateRepresentation;

import org.apache.log4j.Logger;

import com.p4square.fmfacade.FreeMarkerPageResource;

import com.p4square.fmfacade.json.JsonRequestClient;
import com.p4square.fmfacade.json.JsonResponse;
import com.p4square.fmfacade.json.ClientException;

import com.p4square.grow.config.Config;

/**
 * This page delete's the current user's assessment.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class AssessmentResetPage extends FreeMarkerPageResource {
    private static final Logger LOG = Logger.getLogger(AssessmentResetPage.class);

    private GrowFrontend mGrowFrontend;
    private Config mConfig;
    private JsonRequestClient mJsonClient;

    private String mUserId;

    @Override
    public void doInit() {
        super.doInit();

        mGrowFrontend = (GrowFrontend) getApplication();
        mConfig = mGrowFrontend.getConfig();

        mJsonClient = new JsonRequestClient(getContext().getClientDispatcher());

        mUserId = getRequest().getClientInfo().getUser().getIdentifier();
    }

    /**
     * Return the login page.
     */
    @Override
    protected Representation get() {
        try {
            // Get the assessment results
            JsonResponse response = backendDelete("/accounts/" + mUserId + "/assessment");
            if (!response.getStatus().isSuccess()) {
                setStatus(Status.SERVER_ERROR_INTERNAL);
                return ErrorPage.BACKEND_ERROR;
            }

            String nextPage = mConfig.getString("dynamicRoot", "")
                    + "/account/assessment/question/first";
            getResponse().redirectSeeOther(nextPage);
            return new StringRepresentation("Redirecting to " + nextPage);

        } catch (Exception e) {
            LOG.fatal("Could not render page: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return ErrorPage.RENDER_ERROR;
        }
    }

    /**
     * @return The backend endpoint URI
     */
    private String getBackendEndpoint() {
        return mConfig.getString("backendUri", "riap://component/backend");
    }

    /**
     * Helper method to send a GET to the backend.
     */
    private JsonResponse backendDelete(final String uri) {
        LOG.debug("Sending backend GET " + uri);

        final JsonResponse response = mJsonClient.delete(getBackendEndpoint() + uri);
        final Status status = response.getStatus();
        if (!status.isSuccess() && !Status.CLIENT_ERROR_NOT_FOUND.equals(status)) {
            LOG.warn("Error making backend request for '" + uri + "'. status = " + response.getStatus().toString());
        }

        return response;
    }
}
