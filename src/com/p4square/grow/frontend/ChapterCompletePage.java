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
 * This resource displays the transitional page between chapters.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class ChapterCompletePage extends FreeMarkerPageResource {
    private static final Logger LOG = Logger.getLogger(ChapterCompletePage.class);

    private GrowFrontend mGrowFrontend;
    private Config mConfig;
    private JsonRequestClient mJsonClient;

    private String mUserId;
    private String mChapter;

    @Override
    public void doInit() {
        super.doInit();

        mGrowFrontend = (GrowFrontend) getApplication();
        mConfig = mGrowFrontend.getConfig();

        mJsonClient = new JsonRequestClient(getContext().getClientDispatcher());

        mUserId = getRequest().getClientInfo().getUser().getIdentifier();

        mChapter = getAttribute("chapter");
    }

    /**
     * Return the login page.
     */
    @Override
    protected Representation get() {
        Template t = mGrowFrontend.getTemplate("templates/stage-complete.ftl");

        try {
            if (t == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return ErrorPage.TEMPLATE_NOT_FOUND;
            }

            Map<String, Object> root = getRootObject();

            // Get the training summary
            Map<String, Object> trainingRecord = null;
            Map<String, Boolean> chapters = null;
            {
                JsonResponse response = backendGet("/accounts/" + mUserId + "/training");
                if (response.getStatus().isSuccess()) {
                    trainingRecord = response.getMap();
                    chapters = (Map<String, Boolean>) trainingRecord.get("chapters");
                }
            }

            // Verify they completed the chapter.
            Boolean completed = chapters.get(mChapter);
            if (completed == null || !completed) {
                // Redirect back to training page...
                String nextPage = mConfig.getString("dynamicRoot", "");
                nextPage += "/account/training/" + mChapter;
                getResponse().redirectSeeOther(nextPage);
                return new StringRepresentation("Redirecting to " + nextPage);
            }

            // Find the next chapter
            String nextChapter = null;
            {
                int min = Integer.MAX_VALUE;
                for (Map.Entry<String, Boolean> chapter : chapters.entrySet()) {
                    int index = chapterIndex(chapter.getKey());
                    if (!chapter.getValue() && index < min) {
                        min = index;
                        nextChapter = chapter.getKey();
                    }
                }
            }

            root.put("stage", mChapter);
            root.put("nextstage", nextChapter);
            return new TemplateRepresentation(t, root, MediaType.TEXT_HTML);

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

    int chapterIndex(String chapter) {
        if ("teacher".equals(chapter)) {
            return 4;
        } else if ("disciple".equals(chapter)) {
            return 3;
        } else if ("believer".equals(chapter)) {
            return 2;
        } else {
            return 1;
        }
    }
}
