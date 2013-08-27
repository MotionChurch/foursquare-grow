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
     * Return a page with a survey question.
     */
    @Override
    protected Representation get() {
        try {
            // Get the current chapter.
            if (mChapter == null) {
                // TODO: Get user's current question
                mChapter = "seeker";
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

            // Get list of completed videos
            Map<String, Object> trainingRecord = null;
            Map<String, Object> completedVideos = new HashMap<String, Object>();
            {
                JsonResponse response = backendGet("/accounts/" + mUserId + "/training");
                if (response.getStatus().isSuccess()) {
                    trainingRecord = response.getMap();
                    completedVideos = (Map<String, Object>) trainingRecord.get("videos");
                }
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
            return null;
        }
    }

    /**
     * Record a survey answer and redirect to the next question.
     */
    @Override
    protected Representation post(Representation entity) {
        return null;
        /*final Form form = new Form(entity);
        final String answerId = form.getFirstValue("answer");

        if (mQuestionId == null || answerId == null || answerId.length() == 0) {
            // Something is wrong.
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
        }

        try {
            // Find the question
            Map questionData = null;
            {
                JsonResponse response = backendGet("/assessment/question/" + mQuestionId);
                if (!response.getStatus().isSuccess()) {
                    // User is answering a question which doesn't exist
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    return null;
                }

                questionData = response.getMap();
            }

            // Store answer
            {
                Map<String, String> answer = new HashMap<String, String>();
                answer.put("answerId", answerId);
                JsonResponse response = backendPut("/accounts/" + mUserId +
                        "/assessment/answers/" + mQuestionId, answer);

                if (!response.getStatus().isSuccess()) {
                    // Something went wrong talking to the backend, error out.
                    cLog.fatal("Error recording survey answer " + response.getStatus());
                    setStatus(Status.SERVER_ERROR_INTERNAL);
                    return null;
                }
            }

            // Find the next question or finish the assessment.
            String nextPage = mConfig.getString("dynamicRoot", "");
            {
                String nextQuestionId = (String) questionData.get("nextQuestion");
                if (nextQuestionId == null) {
                    nextPage += "/account/assessment/results";
                } else {
                    nextPage += "/account/assessment/question/" + nextQuestionId;
                }
            }

            getResponse().redirectSeeOther(nextPage);
            return null;

        } catch (Exception e) {
            cLog.fatal("Could not render page: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }*/
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
}
