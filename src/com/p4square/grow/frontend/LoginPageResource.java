/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend;

import java.util.Map;

import freemarker.template.Template;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;
import org.restlet.representation.Representation;
import org.restlet.ext.freemarker.TemplateRepresentation;

import org.apache.log4j.Logger;

import net.jesterpm.fmfacade.FreeMarkerPageResource;

/**
 * LoginPageResource presents a login page template and processes the response.
 * Upon successful authentication, the user is redirected to another page and
 * a cookie is set.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class LoginPageResource extends FreeMarkerPageResource {
    private static Logger cLog = Logger.getLogger(LoginPageResource.class);

    private GrowFrontend mGrowFrontend;

    private String mErrorMessage;

    @Override
    public void doInit() {
        super.doInit();

        mGrowFrontend = (GrowFrontend) getApplication();

        mErrorMessage = null;
    }

    /**
     * Return the login page.
     */
    @Override
    protected Representation get() {
        Template t = mGrowFrontend.getTemplate("pages/login.html.ftl");

        try {
            if (t == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return null;
            }

            Map<String, Object> root = getRootObject();

            root.put("errorMessage", mErrorMessage);

            return new TemplateRepresentation(t, root, MediaType.TEXT_HTML);

        } catch (Exception e) {
            cLog.fatal("Could not render page: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
    }

    /**
     * Process login and authenticate the user.
     */
    @Override
    protected Representation post(Representation entity) {
        final Form form = new Form(entity);
        final String email = form.getFirstValue("email");
        final String password = form.getFirstValue("password");

        boolean authenticated = false;

        // TODO: Do something real here
        if (email != null && !"".equals(email)) {
            cLog.debug("Got login request from " + email);

            // TODO: Encrypt user info
            getResponse().getCookieSettings().add(LoginAuthenticator.COOKIE_NAME, email);

            authenticated = true;
        }

        if (authenticated) {
            // TODO: Better return url.
            getResponse().redirectSeeOther("/index.html");
            return null;

        } else {
            // Send them back to the login page...
            mErrorMessage = "Incorrect Email or Password.";
            return get();
        }
    }
}
