/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend;

import java.util.Map;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;

import org.apache.log4j.Logger;

import net.jesterpm.fmfacade.json.JsonRequestClient;
import net.jesterpm.fmfacade.json.JsonResponse;
import net.jesterpm.fmfacade.json.ClientException;

import net.jesterpm.fmfacade.FreeMarkerPageResource;

import com.p4square.grow.config.Config;

/**
 * This resource simply redirects the user to either the assessment
 * or the training page.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class AccountRedirectResource extends ServerResource {
    private static final Logger LOG = Logger.getLogger(AccountRedirectResource.class);

    private Config mConfig;
    private JsonRequestClient mJsonClient;

    // Fields pertaining to this request.
    private String mUserId;

    @Override
    public void doInit() {
        super.doInit();

        GrowFrontend growFrontend = (GrowFrontend) getApplication();
        mConfig = growFrontend.getConfig();

        mJsonClient = new JsonRequestClient(getContext().getClientDispatcher());

        mUserId = getRequest().getClientInfo().getUser().getIdentifier();
    }

    /**
     * Redirect to the correct landing.
     */
    @Override
    protected Representation get() {
        try {
            Map<?, ?> account = null;
            try {
                JsonResponse response = backendGet("/accounts/" + mUserId);
                if (response.getStatus().isSuccess()) {
                    account = response.getMap();
                }
            } catch (ClientException e) {

            }

            String landing = null;

            if (account != null) {
                landing = (String) account.get("landing");
            }

            if (landing == null) {
                landing = "assessment";
            }

            String nextPage = mConfig.getString("dynamicRoot", "");
            nextPage += "/account/" + landing;
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
    private JsonResponse backendGet(final String uri) {
        LOG.debug("Sending backend GET " + uri);

        final JsonResponse response = mJsonClient.get(getBackendEndpoint() + uri);
        final Status status = response.getStatus();
        if (!status.isSuccess() && !Status.CLIENT_ERROR_NOT_FOUND.equals(status)) {
            LOG.warn("Error making backend request for '" + uri + "'. status = " + response.getStatus().toString());
        }

        return response;
    }
}
