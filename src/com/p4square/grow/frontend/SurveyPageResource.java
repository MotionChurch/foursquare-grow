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
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;

import org.apache.log4j.Logger;

import com.p4square.fmfacade.json.JsonRequestClient;
import com.p4square.fmfacade.json.JsonResponse;
import com.p4square.fmfacade.json.ClientException;

import com.p4square.fmfacade.FreeMarkerPageResource;

import com.p4square.grow.config.Config;
import com.p4square.grow.model.Question;

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
    private static final Logger LOG = Logger.getLogger(SurveyPageResource.class);

    private Config mConfig;
    private Template mSurveyTemplate;
    private JsonRequestClient mJsonClient;

    // Fields pertaining to this request.
    private String mQuestionId;
    private String mUserId;

    @Override
    public void doInit() {
        super.doInit();

        GrowFrontend growFrontend = (GrowFrontend) getApplication();
        mConfig = growFrontend.getConfig();
        mSurveyTemplate = growFrontend.getTemplate("templates/survey.ftl");
        if (mSurveyTemplate == null) {
            LOG.fatal("Could not find survey template.");
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
                // Get user's current question
                mQuestionId = getCurrentQuestionId();

                if (mQuestionId != null) {
                    Question lastQuestion = getQuestion(mQuestionId);
                    return redirectToNextQuestion(lastQuestion, getAnswer(mQuestionId));
                }
            }

            // If we don't have a current question, get the first one.
            if (mQuestionId == null) {
                mQuestionId = "first";
            }

            Question question = getQuestion(mQuestionId);
            if (question == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return new ErrorPage("Could not find the question.");
            }

            // Set the real question id if a meta-id was used (i.e. first)
            mQuestionId = question.getId();

            // Get any previous answer to the question
            String selectedAnswer = getAnswer(mQuestionId);

            Map root = getRootObject();
            root.put("question", question.getMap());
            root.put("selectedAnswerId", selectedAnswer);

            // Get the question count and compute progress
            {
                JsonResponse response = backendGet("/assessment/question/count");
                if (response.getStatus().isSuccess()) {
                    Map countData = response.getMap();
                    if (countData != null) {
                        response = backendGet("/accounts/" + mUserId + "/assessment");
                        if (response.getStatus().isSuccess()) {
                            Integer completed = (Integer) response.getMap().get("count");
                            Integer total = (Integer) countData.get("count");

                            if (completed != null && total != null && total != 0) {
                                root.put("percentComplete", String.valueOf((int) (100.0 * completed) / total));
                            }
                        }
                    }
                }
            }

            return new TemplateRepresentation(mSurveyTemplate, root, MediaType.TEXT_HTML);

        } catch (Exception e) {
            LOG.fatal("Could not render page: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return ErrorPage.RENDER_ERROR;
        }
    }

    /**
     * Record a survey answer and redirect to the next question.
     */
    @Override
    protected Representation post(Representation entity) {
        final Form form = new Form(entity);
        final String answerId = form.getFirstValue("answer");
        final String direction = form.getFirstValue("direction");
        boolean justGoBack = false; // FIXME: Ugly hack

        if (mQuestionId == null || answerId == null || answerId.length() == 0) {
            if ("previous".equals(direction)) {
                // Just go back
                justGoBack = true;

            } else {
                // Something is wrong.
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return new ErrorPage("Question or answer messing.");
            }
        }

        try {
            // Find the question
            Question question = getQuestion(mQuestionId);
            if (question == null) {
                // User is answering a question which doesn't exist
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return new ErrorPage("Question not found.");
            }

            // Store answer
            if (!justGoBack) {
                Map<String, String> answer = new HashMap<String, String>();
                answer.put("answerId", answerId);
                JsonResponse response = backendPut("/accounts/" + mUserId +
                        "/assessment/answers/" + mQuestionId, answer);

                if (!response.getStatus().isSuccess()) {
                    // Something went wrong talking to the backend, error out.
                    LOG.fatal("Error recording survey answer " + response.getStatus());
                    setStatus(Status.SERVER_ERROR_INTERNAL);
                    return ErrorPage.BACKEND_ERROR;
                }
            }

            // Find the next question or finish the assessment.
            if ("previous".equals(direction)) {
                return redirectToPreviousQuestion(question);

            } else {
                return redirectToNextQuestion(question, answerId);
            }

        } catch (Exception e) {
            LOG.fatal("Could not render page: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return ErrorPage.RENDER_ERROR;
        }
    }

    private Map<?, ?> getAccount(String id) {
        try {
            Map<?, ?> account = null;

            JsonResponse response = backendGet("/accounts/" + id);
            if (!response.getStatus().isSuccess()) {
                return null;
            }
            account = response.getMap();

            return account;

        } catch (ClientException e) {
            LOG.warn("Error fetching account.", e);
            return null;
        }
    }

    private Question getQuestion(String id) {
        try {
            Map<?, ?> questionData = null;

            JsonResponse response = backendGet("/assessment/question/" + id);
            if (!response.getStatus().isSuccess()) {
                return null;
            }
            questionData = response.getMap();

            return new Question((Map<String, Object>) questionData);

        } catch (ClientException e) {
            LOG.warn("Error fetching question.", e);
            return null;
        }
    }

    private String getAnswer(String questionId) {
        try {
            JsonResponse response = backendGet("/accounts/" + mUserId + "/assessment/answers/" + questionId);
            if (response.getStatus().isSuccess()) {
                return (String) response.getMap().get("answerId");
            }

        } catch (ClientException e) {
            LOG.warn("Error fetching answer to question " + questionId, e);
        }

        return null;
    }

    private Representation redirectToNextQuestion(Question question, String answerid) {
        String nextQuestionId = question.getNextQuestion(answerid);

        if (nextQuestionId == null) {
            // Just finished the last question. Update the user's account
            Map account = getAccount(mUserId);
            if (account == null) {
                account = new HashMap();
            }
            account.put("landing", "training");
            backendPut("/accounts/" + mUserId, account);

            String nextPage = mConfig.getString("dynamicRoot", "");
            nextPage += "/account/assessment/results";
            getResponse().redirectSeeOther(nextPage);
            return new StringRepresentation("Redirecting to " + nextPage);
        }

        return redirectToQuestion(nextQuestionId);
    }

    private Representation redirectToPreviousQuestion(Question question) {
        String nextQuestionId = question.getPreviousQuestion();

        if (nextQuestionId == null) {
            nextQuestionId = (String) question.getId();
        }

        return redirectToQuestion(nextQuestionId);
    }

    private Representation redirectToQuestion(String id) {
        String nextPage = mConfig.getString("dynamicRoot", "");
        nextPage += "/account/assessment/question/" + id;
        getResponse().redirectSeeOther(nextPage);
        return new StringRepresentation("Redirecting to " + nextPage);
    }

    private String getCurrentQuestionId() {
        String id = null;
        try {
            JsonResponse response = backendGet("/accounts/" + mUserId + "/assessment");

            if (response.getStatus().isSuccess()) {
                return (String) response.getMap().get("lastAnswered");

            } else {
                LOG.warn("Failed to get assessment results: " + response.getStatus());
            }

        } catch (ClientException e) {
            LOG.error("Exception getting assessment results.", e);
        }

        return null;
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

    protected JsonResponse backendPut(final String uri, final Map data) {
        LOG.debug("Sending backend PUT " + uri);

        final JsonResponse response = mJsonClient.put(getBackendEndpoint() + uri, data);
        final Status status = response.getStatus();
        if (!status.isSuccess() && !Status.CLIENT_ERROR_NOT_FOUND.equals(status)) {
            LOG.warn("Error making backend request for '" + uri + "'. status = " + response.getStatus().toString());
        }

        return response;
    }
}
