/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend;

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

import net.jesterpm.fmfacade.json.JsonRequestClient;
import net.jesterpm.fmfacade.json.JsonResponse;

import net.jesterpm.fmfacade.FreeMarkerPageResource;

/**
 * SurveyPageResource handles rendering the survey and processing user's answers.
 *
 * This resource expects the user to be authenticated and the ClientInfo User object
 * to be populated. Each question is requested from the backend along with the
 * user's previous answer. Each answer is sent to the backend and the user is redirected
 * to the next question. After the last question the user is sent to his results.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SurveyPageResource extends FreeMarkerPageResource {
    private static Logger cLog = Logger.getLogger(SurveyPageResource.class);

    private Template mSurveyTemplate;
    private JsonRequestClient mJsonClient;

    // Fields pertaining to this request.
    private String mQuestionId;
    private String mUserId;

    @Override
    public void doInit() {
        super.doInit();

        GrowFrontend growFrontend = (GrowFrontend) getApplication();
        mSurveyTemplate = growFrontend.getTemplate("templates/survey.ftl");
        if (mSurveyTemplate == null) {
            cLog.fatal("Could not find survey template.");
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        mJsonClient = new JsonRequestClient(getContext().getClientDispatcher());

        mQuestionId = getAttribute("questionId");
        mUserId = getRequest().getClientInfo().getUser().getIdentifier();
    }

    /**
     * Return a page with a survey question.
     */
    @Override
    protected Representation get() {
        try {
            // Get the current question.
            if (mQuestionId == null) {
                // TODO: Get user's current question
                mQuestionId = "1";
            }

            Map questionData = null;
            {
                JsonResponse response = backendGet("/assessment/question/" + mQuestionId);
                if (!response.getStatus().isSuccess()) {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    return null;
                }
                questionData = response.getMap();
            }

            // Get any previous answer to the question
            String selectedAnswer = null;
            {
                JsonResponse response = backendGet("/accounts/" + mUserId + "/assessment/answers/" + mQuestionId);
                if (response.getStatus().isSuccess()) {
                    selectedAnswer = (String) response.getMap().get("answerId");
                }
            }

            Map root = getRootObject();
            root.put("question", questionData);
            root.put("selectedAnswerId", selectedAnswer);

            return new TemplateRepresentation(mSurveyTemplate, root, MediaType.TEXT_HTML);

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
        final Form form = new Form(entity);
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
            String nextPage;
            {
                String nextQuestionId = (String) questionData.get("nextQuestion");
                if (nextQuestionId == null) {
                    nextPage = "/account/assessment/results";
                } else {
                    nextPage = "/account/assessment/question/" + nextQuestionId;
                }
            }

            getResponse().redirectSeeOther(nextPage);
            return null;

        } catch (Exception e) {
            cLog.fatal("Could not render page: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
    }

    /**
     * @return The backend endpoint URI
     */
    private String getBackendEndpoint() {
        // TODO: Config
        return "http://localhost:9095";
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

    protected JsonResponse backendPut(final String uri, final Map data) {
        cLog.debug("Sending backend PUT " + uri);

        final JsonResponse response = mJsonClient.put(getBackendEndpoint() + uri, data);
        final Status status = response.getStatus();
        if (!status.isSuccess() && !Status.CLIENT_ERROR_NOT_FOUND.equals(status)) {
            cLog.warn("Error making backend request for '" + uri + "'. status = " + response.getStatus().toString());
        }

        return response;
    }
}
