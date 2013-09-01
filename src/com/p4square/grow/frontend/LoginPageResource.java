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

            Form query = getRequest().getOriginalRef().getQueryAsForm();
            String retry = query.getFirstValue("retry");
            if ("t".equals(retry)) {
                root.put("errorMessage", "Invalid email or password.");
            }

            return new TemplateRepresentation(t, root, MediaType.TEXT_HTML);

        } catch (Exception e) {
            cLog.fatal("Could not render page: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
    }

}
