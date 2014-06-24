/*
 * Copyright 2013 Jesse Morgan
 */

package net.jesterpm.fmfacade;

import java.util.Map;
import java.util.HashMap;

import freemarker.template.Template;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;
import org.restlet.security.User;

import org.apache.log4j.Logger;

import net.jesterpm.fmfacade.ftl.GetMethod;

import net.jesterpm.session.Session;
import net.jesterpm.session.Sessions;

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class FreeMarkerPageResource extends ServerResource {
    private static Logger cLog = Logger.getLogger(FreeMarkerPageResource.class);

    public static Map<String, Object> baseRootObject(Context context) {
        Map<String, Object> root = new HashMap<String, Object>();

        root.put("get", new GetMethod(context.getClientDispatcher()));

        return root;
    }

    private FMFacade mFMF;
    private String mCurrentPage;

    @Override
    public void doInit() {
        mFMF = (FMFacade) getApplication();
        mCurrentPage = getReference().getRemainingPart(false, false);
    }

    protected Representation get() {
        try {
            Template t = mFMF.getTemplate("pages" + mCurrentPage + ".ftl");

            if (t == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return null;
            }

            return new TemplateRepresentation(t, getRootObject(),
                    MediaType.TEXT_HTML);

        } catch (Exception e) {
            cLog.fatal("Could not render page: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
    }

    /**
     * Build and return the root object to pass to the FTL Template.
     * @return A map of objects and methods for the template to access.
     */
    protected Map<String, Object> getRootObject() {
        Map<String, Object> root = baseRootObject(getContext());

        root.put("attributes", getRequestAttributes());
        root.put("query", getQuery().getValuesMap());
        root.put("config", mFMF.getConfig());
        
        if (getClientInfo().isAuthenticated()) {
            final User user = getClientInfo().getUser();
            final Map<String, String> userMap = new HashMap<String, String>();
            userMap.put("id", user.getIdentifier());
            userMap.put("firstName", user.getFirstName());
            userMap.put("lastName",  user.getLastName());
            userMap.put("email", user.getEmail());
            root.put("user", userMap);
        }

        Session s = Sessions.getInstance().get(getRequest());
        if (s != null) {
            root.put("session", s.getMap());
        }

        return root;
    }
}
