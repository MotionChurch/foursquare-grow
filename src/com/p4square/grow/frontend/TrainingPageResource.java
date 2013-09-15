/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import freemarker.template.Template;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;

import org.apache.log4j.Logger;

import com.p4square.fmfacade.json.JsonRequestClient;
import com.p4square.fmfacade.json.JsonResponse;

import com.p4square.fmfacade.FreeMarkerPageResource;

import com.p4square.grow.config.Config;

/**
 * TrainingPageResource handles rendering the training page.
 *
 * This resource expects the user to be authenticated and the ClientInfo User object
 * to be populated.
 * 
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class TrainingPageResource extends FreeMarkerPageResource {
    private static Logger cLog = Logger.getLogger(TrainingPageResource.class);

    private Config mConfig;
    private Template mTrainingTemplate;
    private JsonRequestClient mJsonClient;

    // Fields pertaining to this request.
    private String mChapter;
    private String mUserId;

    @Override
    public void doInit() {
        super.doInit();

        GrowFrontend growFrontend = (GrowFrontend) getApplication();
        mConfig = growFrontend.getConfig();
        mTrainingTemplate = growFrontend.getTemplate("templates/training.ftl");
        if (mTrainingTemplate == null) {
            cLog.fatal("Could not find training template.");
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        mJsonClient = new JsonRequestClient(getContext().getClientDispatcher());

        mChapter = getAttribute("chapter");
        mUserId = getRequest().getClientInfo().getUser().getIdentifier();
    }

    /**
     * Return a page of videos.
     */
    @Override
    protected Representation get() {
        try {
            // Get the training summary
            Map<String, Object> trainingRecord = null;
            Map<String, Object> completedVideos = new HashMap<String, Object>();
            Map<String, Boolean> chapters = null;
            {
                JsonResponse response = backendGet("/accounts/" + mUserId + "/training");
                if (response.getStatus().isSuccess()) {
                    trainingRecord = response.getMap();
                    completedVideos = (Map<String, Object>) trainingRecord.get("videos");
                    chapters = (Map<String, Boolean>) trainingRecord.get("chapters");
                }
            }

            // Get the current chapter (the lowest, incomplete chapter)
            if (mChapter == null) {
                int min = Integer.MAX_VALUE;
                for (Map.Entry<String, Boolean> chapter : chapters.entrySet()) {
                    int index = chapterIndex(chapter.getKey());
                    if (!chapter.getValue() && index < min) {
                        min = index;
                        mChapter = chapter.getKey();
                    }
                }

                if (mChapter == null) {
                    // Everything is completed... send them back to introduction.
                    mChapter = "introduction";
                }

                String nextPage = mConfig.getString("dynamicRoot", "");
                nextPage += "/account/training/" + mChapter;
                getResponse().redirectSeeOther(nextPage);
                return new StringRepresentation("Redirecting to " + nextPage);
            }

            // Get videos for the chapter.
            List<Map<String, Object>> videos = null;
            {
                JsonResponse response = backendGet("/training/" + mChapter);
                if (!response.getStatus().isSuccess()) {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    return null;
                }
                videos = (List<Map<String, Object>>) response.getMap().get("videos");
            }

            // Mark the completed videos as completed
            int chapterProgress = 0;
            for (Map<String, Object> video : videos) {
                boolean completed = (null != completedVideos.get(video.get("id")));
                video.put("completed", completed);

                if (completed) {
                    chapterProgress++;
                }
            }
            chapterProgress = chapterProgress * 100 / videos.size();

            Map root = getRootObject();
            root.put("chapter", mChapter);
            root.put("chapterProgress", chapterProgress);
            root.put("videos", videos);
            root.put("completedVideos", completedVideos);

            return new TemplateRepresentation(mTrainingTemplate, root, MediaType.TEXT_HTML);

        } catch (Exception e) {
            cLog.fatal("Could not render page: " + e.getMessage(), e);
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
        cLog.debug("Sending backend GET " + uri);

        final JsonResponse response = mJsonClient.get(getBackendEndpoint() + uri);
        final Status status = response.getStatus();
        if (!status.isSuccess() && !Status.CLIENT_ERROR_NOT_FOUND.equals(status)) {
            cLog.warn("Error making backend request for '" + uri + "'. status = " + response.getStatus().toString());
        }

        return response;
    }

    private JsonResponse backendPut(final String uri, final Map data) {
        cLog.debug("Sending backend PUT " + uri);

        final JsonResponse response = mJsonClient.put(getBackendEndpoint() + uri, data);
        final Status status = response.getStatus();
        if (!status.isSuccess() && !Status.CLIENT_ERROR_NOT_FOUND.equals(status)) {
            cLog.warn("Error making backend request for '" + uri + "'. status = " + response.getStatus().toString());
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
        } else if ("seeker".equals(chapter)) {
            return 1;
        } else {
            return 0;
        }
    }
}
