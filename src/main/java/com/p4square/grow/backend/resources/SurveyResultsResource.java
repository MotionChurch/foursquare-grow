/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;

import org.apache.log4j.Logger;

import com.p4square.grow.backend.GrowBackend;
import com.p4square.grow.model.Answer;
import com.p4square.grow.model.Question;
import com.p4square.grow.model.RecordedAnswer;
import com.p4square.grow.model.Score;
import com.p4square.grow.model.UserRecord;
import com.p4square.grow.provider.CollectionProvider;
import com.p4square.grow.provider.Provider;


/**
 * Store the user's answers to the assessment and generate their score.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SurveyResultsResource extends ServerResource {
    private static final Logger LOG = Logger.getLogger(SurveyResultsResource.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static enum RequestType {
        ASSESSMENT, ANSWER
    }

    private CollectionProvider<String, String, String> mAnswerProvider;
    private Provider<String, Question> mQuestionProvider;
    private Provider<String, UserRecord> mUserRecordProvider;

    private RequestType mRequestType;
    private String mUserId;
    private String mQuestionId;

    @Override
    public void doInit() {
        super.doInit();

        final GrowBackend backend = (GrowBackend) getApplication();
        mAnswerProvider = backend.getAnswerProvider();
        mQuestionProvider = backend.getQuestionProvider();
        mUserRecordProvider = backend.getUserRecordProvider();

        mUserId = getAttribute("userId");
        mQuestionId = getAttribute("questionId");

        mRequestType = RequestType.ASSESSMENT;
        if (mQuestionId != null) {
            mRequestType = RequestType.ANSWER;
        }
    }

    /**
     * Handle GET Requests.
     */
    @Override
    protected Representation get() {
        try {
            String result = null;

            switch (mRequestType) {
                case ANSWER:
                    result = mAnswerProvider.get(mUserId, mQuestionId);
                    break;

                case ASSESSMENT:
                    result = mAnswerProvider.get(mUserId, "summary");
                    if (result == null || result.length() == 0) {
                        result = buildAssessment();
                    }
                    break;
            }

            if (result == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return null;
            }

            return new StringRepresentation(result);
        } catch (IOException e) {
            LOG.error("IOException getting answer: ", e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
    }

    /**
     * Handle PUT requests
     */
    @Override
    protected Representation put(Representation entity) {
        boolean success = false;

        switch (mRequestType) {
            case ANSWER:
                try {
                    mAnswerProvider.put(mUserId, mQuestionId, entity.getText());
                    mAnswerProvider.put(mUserId, "lastAnswered", mQuestionId);
                    mAnswerProvider.put(mUserId, "summary", null);
                    success = true;

                } catch (Exception e) {
                    LOG.warn("Caught exception putting answer: " + e.getMessage(), e);
                }
                break;

            default:
                setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
                return null;
        }

        if (success) {
            setStatus(Status.SUCCESS_NO_CONTENT);

        } else {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return null;
    }

    /**
     * Clear assessment results.
     */
    @Override
    protected Representation delete() {
        boolean success = false;

        switch (mRequestType) {
            case ANSWER:
                try {
                    mAnswerProvider.put(mUserId, mQuestionId, null);
                    mAnswerProvider.put(mUserId, "summary", null);
                    success = true;

                } catch (Exception e) {
                    LOG.warn("Caught exception putting answer: " + e.getMessage(), e);
                }
                break;

            case ASSESSMENT:
                try {
                    mAnswerProvider.put(mUserId, "summary", null);
                    mAnswerProvider.put(mUserId, "lastAnswered", null);
                    // TODO Delete answers

                    UserRecord record = mUserRecordProvider.get(mUserId);
                    if (record != null) {
                        record.setLanding("assessment");
                        mUserRecordProvider.put(mUserId, record);
                    }

                    success = true;

                } catch (Exception e) {
                    LOG.warn("Caught exception putting answer: " + e.getMessage(), e);
                }
                break;

            default:
                setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
                return null;
        }

        if (success) {
            setStatus(Status.SUCCESS_NO_CONTENT);

        } else {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return null;

    }

    /**
     * This method compiles assessment results.
     */
    private String buildAssessment() throws IOException {
        StringBuilder sb = new StringBuilder("{ ");

        // Last question answered
        final String lastAnswered = mAnswerProvider.get(mUserId, "lastAnswered");
        if (lastAnswered != null && lastAnswered.length() > 0) {
            sb.append("\"lastAnswered\": \"" + lastAnswered + "\", ");
        }

        // Compute score
        Map<String, String> row = mAnswerProvider.query(mUserId);
        if (row.size() > 0) {
            Score score = new Score();
            boolean scoringDone = false;
            int totalAnswers = 0;
            for (Map.Entry<String, String> c : row.entrySet()) {
                if (c.getKey().equals("lastAnswered") || c.getKey().equals("summary")) {
                    continue;
                }

                try {
                    Question question = mQuestionProvider.get(c.getKey());
                    RecordedAnswer userAnswer = MAPPER.readValue(c.getValue(), RecordedAnswer.class);

                    if (question == null) {
                        LOG.warn("Answer for unknown question: " + c.getKey());
                        continue;
                    }

                    LOG.debug("Scoring questionId: " + c.getKey());
                    scoringDone = !question.scoreAnswer(score, userAnswer);

                } catch (Exception e) {
                    LOG.error("Failed to score question: {userid: \"" + mUserId +
                            "\", questionid:\"" + c.getKey() +
                            "\", userAnswer:\"" + c.getValue() + "\"}", e);
                }

                totalAnswers++;
            }

            sb.append("\"score\":" + score.getScore());
            sb.append(", \"sum\":" + score.getSum());
            sb.append(", \"count\":" + score.getCount());
            sb.append(", \"totalAnswers\":" + totalAnswers);
            sb.append(", \"result\":\"" + score.toString() + "\"");
        }

        sb.append(" }");
        String summary = sb.toString();

        // Persist summary
        mAnswerProvider.put(mUserId, "summary", summary);

        return summary;
    }
}
