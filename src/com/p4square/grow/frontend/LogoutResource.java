/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;

import net.jesterpm.session.Sessions;

import com.p4square.grow.config.Config;

/**
 * This Resource removes a user's session and session cookies.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class LogoutResource extends ServerResource {
    private Config mConfig;

    @Override
    protected void doInit() {
        super.doInit();

        GrowFrontend growFrontend = (GrowFrontend) getApplication();
        mConfig = growFrontend.getConfig();
    }

    @Override
    protected Representation get() {
        Sessions.getInstance().delete(getRequest(), getResponse());

        String nextPage = mConfig.getString("dynamicRoot", "");
        nextPage += "/index.html";
        getResponse().redirectSeeOther(nextPage);
        return new StringRepresentation("Redirecting to " + nextPage);
    }
}
