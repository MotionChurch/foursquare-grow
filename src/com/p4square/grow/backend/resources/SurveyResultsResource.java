/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.util.Map;
import java.util.HashMap;

import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnList;

import org.codehaus.jackson.map.ObjectMapper;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import org.apache.log4j.Logger;

import com.p4square.grow.model.Answer;
import com.p4square.grow.model.Question;
import com.p4square.grow.backend.GrowBackend;
import com.p4square.grow.backend.db.CassandraDatabase;

/**
 * Store the user's answers to the assessment and generate their score.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SurveyResultsResource extends ServerResource {
    private final static Logger cLog = Logger.getLogger(SurveyResultsResource.class);

    private final static ObjectMapper MAPPER = new ObjectMapper();

    static enum RequestType {
        ASSESSMENT, ANSWER
    }

    private CassandraDatabase mDb;

    private RequestType mRequestType;
    private String mUserId;
    private String mQuestionId;

    @Override
    public void doInit() {
        super.doInit();

        final GrowBackend backend = (GrowBackend) getApplication();
        mDb = backend.getDatabase();

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
                    cLog.warn("Caught exception putting answer: " + e.getMessage(), e);
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

                final String questionId = c.getName();
                final String answerId   = c.getStringValue();
                if (!scoringDone) {
                    scoringDone = !scoreQuestion(score, questionId, answerId);
                }

                totalAnswers++;
            }

            sb.append(", \"score\":" + score.sum / score.count);
            sb.append(", \"sum\":" + score.sum);
            sb.append(", \"count\":" + score.count);
            sb.append(", \"totalAnswers\":" + totalAnswers);
            sb.append(", \"result\":\"" + score.toString() + "\"");
        }

        sb.append(" }");
        String summary = sb.toString();

        // Persist summary
        mDb.putKey("assessments", mUserId, "summary", summary);

        return summary;
    }

    private boolean scoreQuestion(final Score score, final String questionId,
            final String answerJson) {

        final String data = mDb.getKey("strings", "/questions/" + questionId);

        try {
            final Map<?,?> questionMap = MAPPER.readValue(data, Map.class);
            final Map<?,?> answerMap = MAPPER.readValue(answerJson, Map.class);
            final Question question = new Question((Map<String, Object>) questionMap);
            final String answerId = (String) answerMap.get("answerId");

            switch (question.getType()) {
                case TEXT:
                case IMAGE:
                    final Answer answer = question.getAnswers().get(answerId);
                    if (answer == null) {
                        cLog.warn("Got unknown answer " + answerId
                                + " for question " + questionId);
                    } else {
                        if (!scoreAnswer(score, answer)) {
                            return false; // Quit scoring
                        }
                    }
                    break;

                case SLIDER:
                    score.sum += Double.valueOf(answerId) * 4 + 1;
                    score.count++;
                    break;

                case CIRCLE:
                case QUAD:
                    scoreQuad(score, question, answerId);
                    break;
            }

        } catch (Exception e) {
            cLog.error("Exception parsing question id " + questionId, e);
        }

        return true;
    }

    private boolean scoreAnswer(final Score score, final Answer answer) {
        switch (answer.getType()) {
            case TRUMP:
                score.sum = answer.getScoreFactor();
                score.count = 1;
                return false; // Quit scoring.

            case AVERAGE:
                score.sum += answer.getScoreFactor();
                score.count++;
                break;

            case NONE:
                break;
        }

        return true; // Continue scoring
    }

    private boolean scoreQuad(final Score score, final Question question,
            final String answerId) {

        Point[] answers = new Point[question.getAnswers().size()];
        {
            int i = 0;
            for (String answer : question.getAnswers().keySet()) {
               answers[i++] = Point.valueOf(answer);
            }
        }

        Point userAnswer = Point.valueOf(answerId);

        double minDistance = Double.MAX_VALUE;
        int answerIndex = 0;
        for (int i = 0; i < answers.length; i++) {
            final double distance = userAnswer.distance(answers[i]);
            if (distance < minDistance) {
                minDistance = distance;
                answerIndex = i;
            }
        }

        cLog.debug("Quad " + question.getId() + ": Got answer "
                + answers[answerIndex].toString() + " for user point " + answerId);

        final Answer answer = question.getAnswers().get(answers[answerIndex].toString());
        score.sum += answer.getScoreFactor();
        score.count++;

        return true; // Continue scoring
    }
}
