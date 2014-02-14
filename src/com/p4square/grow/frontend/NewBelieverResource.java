/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.frontend;

import freemarker.template.Template;

import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.ext.freemarker.TemplateRepresentation;

import org.apache.log4j.Logger;

import net.jesterpm.fmfacade.FreeMarkerPageResource;

/**
 * This resource displays the transitional page between chapters.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class NewBelieverResource extends FreeMarkerPageResource {
    private static final Logger LOG = Logger.getLogger(NewBelieverResource.class);

    public static final String COOKIE_NAME = "seeker";

    private GrowFrontend mGrowFrontend;

    @Override
    public void doInit() {
        super.doInit();

        mGrowFrontend = (GrowFrontend) getApplication();
    }

    /**
     * Display the New Believer page.
     *
     * The New Believer page creates a cookie to remember the user,
     * explains what's going on, and then asks the user to go to the login
     * page.
     *
     * When the user hits the {@link AccountRedirectResource} the cookie
     * is read and the user is moved ahead to the training section.
     */
    @Override
    protected Representation get() {
        Template t = mGrowFrontend.getTemplate("templates/newbeliever.ftl");

        try {
            if (t == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return ErrorPage.TEMPLATE_NOT_FOUND;
            }

            // Set the new believer cookie
            CookieSetting cookie = new CookieSetting(COOKIE_NAME, "true");
            cookie.setPath("/");
            getRequest().getCookies().add(cookie);
            getResponse().getCookieSettings().add(cookie);

            return new TemplateRepresentation(t, getRootObject(), MediaType.TEXT_HTML);

        } catch (Exception e) {
            LOG.fatal("Could not render page: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return ErrorPage.RENDER_ERROR;
        }
    }
}
