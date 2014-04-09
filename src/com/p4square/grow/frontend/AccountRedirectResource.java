/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;

import org.apache.log4j.Logger;

import net.jesterpm.fmfacade.FreeMarkerPageResource;

import com.p4square.grow.config.Config;
import com.p4square.grow.model.UserRecord;
import com.p4square.grow.provider.Provider;
import com.p4square.grow.provider.DelegateProvider;
import com.p4square.grow.provider.JsonEncodedProvider;

/**
 * This resource simply redirects the user to either the assessment
 * or the training page.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class AccountRedirectResource extends ServerResource {
    private static final Logger LOG = Logger.getLogger(AccountRedirectResource.class);

    private Config mConfig;
    private Provider<String, UserRecord> mUserRecordProvider;

    // Fields pertaining to this request.
    private String mUserId;

    @Override
    public void doInit() {
        super.doInit();

        GrowFrontend growFrontend = (GrowFrontend) getApplication();
        mConfig = growFrontend.getConfig();

        mUserRecordProvider = new DelegateProvider<String, String, UserRecord>(
                new JsonRequestProvider<UserRecord>(getContext().getClientDispatcher(),
                    UserRecord.class)) {
            @Override
            public String makeKey(String userid) {
                return getBackendEndpoint() + "/accounts/" + userid;
            }
        };

        mUserId = getRequest().getClientInfo().getUser().getIdentifier();
    }

    /**
     * Redirect to the correct landing.
     */
    @Override
    protected Representation get() {
        if (mUserId == null || mUserId.length() == 0) {
            // This shouldn't happen, but I want to be safe because of the DB insert below.
            setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return new ErrorPage("Not Authenticated!");
        }

        try {
            // Fetch account Map.
            UserRecord user = null;
            try {
                user = mUserRecordProvider.get(mUserId);
            } catch (NotFoundException e) {
                // User record doesn't exist, so create a new one.
                user = new UserRecord(getRequest().getClientInfo().getUser());
                mUserRecordProvider.put(mUserId, user);
            }

            // Check for the new believers cookie
            String cookie = getRequest().getCookies().getFirstValue(NewBelieverResource.COOKIE_NAME);
            if (cookie != null && cookie.length() != 0) {
                user.setLanding("training");
                user.setNewBeliever(true);
                mUserRecordProvider.put(mUserId, user);
            }

            String landing = user.getLanding();
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
}
