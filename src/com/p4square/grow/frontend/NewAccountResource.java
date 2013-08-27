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
import org.restlet.representation.StringRepresentation;
import org.restlet.ext.freemarker.TemplateRepresentation;

import org.apache.log4j.Logger;

import com.p4square.f1oauth.F1OAuthHelper;
import net.jesterpm.restlet.oauth.OAuthException;

import net.jesterpm.fmfacade.FreeMarkerPageResource;

/**
 * This resource creates a new InFellowship account.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class NewAccountResource extends FreeMarkerPageResource {
    private static Logger LOG = Logger.getLogger(NewAccountResource.class);

    private GrowFrontend mGrowFrontend;
    private F1OAuthHelper mHelper;

    private String mErrorMessage;

    private String mLoginPageUrl;
    private String mVerificationPage;

    @Override
    public void doInit() {
        super.doInit();

        mGrowFrontend = (GrowFrontend) getApplication();
        mHelper = mGrowFrontend.getHelper();

        mErrorMessage = null;

        mLoginPageUrl = "";
        mVerificationPage = "";
    }

    /**
     * Return the login page.
     */
    @Override
    protected Representation get() {
        Template t = mGrowFrontend.getTemplate("pages/newaccount.html.ftl");

        try {
            if (t == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return ErrorPage.TEMPLATE_NOT_FOUND;
            }

            Map<String, Object> root = getRootObject();
            root.put("errorMessage", mErrorMessage);

            return new TemplateRepresentation(t, root, MediaType.TEXT_HTML);

        } catch (Exception e) {
            LOG.fatal("Could not render page: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return ErrorPage.RENDER_ERROR;
        }
    }

    @Override
    protected Representation post(Representation rep) {
        Form form = new Form(rep);

        String firstname = form.getFirstValue("firstname");
        String lastname  = form.getFirstValue("lastname");
        String email     = form.getFirstValue("email");

        if (isEmpty(firstname)) {
            mErrorMessage += "First Name is a required field. ";
        }
        if (isEmpty(lastname)) {
            mErrorMessage += "Last Name is a required field. ";
        }
        if (isEmpty(email)) {
            mErrorMessage += "Email is a required field. ";
        }

        if (mErrorMessage.length() > 0) {
            return get();
        }

        try {
            mHelper.createAccount(firstname, lastname, email, mLoginPageUrl);
            getResponse().redirectSeeOther(mVerificationPage);
            return new StringRepresentation("Redirecting to " + mVerificationPage);

        } catch (OAuthException e) {
            return new ErrorPage(e.getStatus().getDescription());
        }
    }

    private boolean isEmpty(String s) {
        return s != null && s.trim().length() > 0;
    }
}
