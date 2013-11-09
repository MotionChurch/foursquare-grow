/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.util.Map;
import java.util.HashMap;

import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnList;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;

import org.apache.log4j.Logger;

import com.p4square.grow.backend.GrowBackend;
import com.p4square.grow.backend.db.CassandraDatabase;
import com.p4square.grow.model.Answer;
import com.p4square.grow.model.Question;
import com.p4square.grow.model.RecordedAnswer;
import com.p4square.grow.model.Score;
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

    private CassandraDatabase mDb;
    private Provider<String, Question> mQuestionProvider;

    private RequestType mRequestType;
    private String mUserId;
    private String mQuestionId;

    @Override
    public void doInit() {
        super.doInit();

        final GrowBackend backend = (GrowBackend) getApplication();
        mDb = backend.getDatabase();
        mQuestionProvider = backend.getQuestionProvider();

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
        String result = null;

        switch (mRequestType) {
            case ANSWER:
                result = mDb.getKey("assessments", mUserId, mQuestionId);
                break;

            case ASSESSMENT:
                result = mDb.getKey("assessments", mUserId, "summary");
                if (result == null) {
                    result = buildAssessment();
                }
                break;
        }

        if (result == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }

        return new StringRepresentation(result);
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
                    mDb.putKey("assessments", mUserId, mQuestionId, entity.getText());
                    mDb.putKey("assessments", mUserId, "lastAnswered", mQuestionId);
                    mDb.deleteKey("assessments", mUserId, "summary");
                    success = true;

                } catch (Exception e) {
                    LOG.warn("Caught exception putting answer: " + e.getMessage(), e);
                }
                break;

            default:
                setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
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
    private String buildAssessment() {
        StringBuilder sb = new StringBuilder("{ ");

        // Last question answered
        final String lastAnswered = mDb.getKey("assessments", mUserId, "lastAnswered");
        if (lastAnswered != null) {
            sb.append("\"lastAnswered\": \"" + lastAnswered + "\"");
        }

        // Compute score
        ColumnList<String> row = mDb.getRow("assessments", mUserId);
        if (!row.isEmpty()) {
            Score score = new Score();
            boolean scoringDone = false;
            int totalAnswers = 0;
            for (Column<String> c : row) {
                if (c.getName().equals("lastAnswered") || c.getName().equals("summary")) {
                    continue;
                }

                try {
                    Question question = mQuestionProvider.get(c.getName());
                    RecordedAnswer userAnswer = MAPPER.readValue(c.getStringValue(), RecordedAnswer.class);

                    if (question == null) {
                        LOG.warn("Answer for unknown question: " + c.getName());
                        continue;
                    }

                    LOG.error("Scoring questionId: " + c.getName());
                    scoringDone = !question.scoreAnswer(score, userAnswer);

                } catch (Exception e) {
                    LOG.error("Failed to score question: {userid: \"" + mUserId +
                            "\", questionid:\"" + c.getName() +
                            "\", userAnswer:\"" + c.getStringValue() + "\"}", e);
                }

                totalAnswers++;
            }

            sb.append(", \"score\":" + score.getScore());
            sb.append(", \"sum\":" + score.getSum());
            sb.append(", \"count\":" + score.getCount());
            sb.append(", \"totalAnswers\":" + totalAnswers);
            sb.append(", \"result\":\"" + score.toString() + "\"");
        }

        sb.append(" }");
        String summary = sb.toString();

        // Persist summary
        mDb.putKey("assessments", mUserId, "summary", summary);

        return summary;
    }
}
