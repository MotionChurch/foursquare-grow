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
import org.restlet.resource.ServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import org.apache.log4j.Logger;

import com.p4square.grow.backend.GrowBackend;
import com.p4square.grow.backend.db.CassandraDatabase;

/**
 * This resource manages assessment questions.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SurveyResource extends ServerResource {
    private static final Logger LOG = Logger.getLogger(SurveyResource.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private CassandraDatabase mDb;

    private String mQuestionId;

    @Override
    public void doInit() {
        super.doInit();

        final GrowBackend backend = (GrowBackend) getApplication();
        mDb = backend.getDatabase();

        mQuestionId = getAttribute("questionId");
    }

    /**
     * Handle GET Requests.
     */
    @Override
    protected Representation get() {
        String result = "{}";

        if (mQuestionId == null) {
            // TODO: List all question ids

        } else if (mQuestionId.equals("first")) {
            // Get the first question id from db?
            Map<?, ?> questionSummary = getQuestionsSummary();
            mQuestionId = (String) questionSummary.get("first");

        } else if (mQuestionId.equals("count")) {
            // Get the first question id from db?
            Map<?, ?> questionSummary = getQuestionsSummary();

            return new StringRepresentation("{\"count\":" +
                    String.valueOf((Integer) questionSummary.get("count")) + "}");
        }

        if (mQuestionId != null) {
            // Get a question by id
            result = mDb.getKey("strings", "/questions/" + mQuestionId);

            if (result == null) {
                // 404
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return null;
            }
        }

        return new StringRepresentation(result);
    }

    private Map<?, ?> getQuestionsSummary() {
        try {
            String json = mDb.getKey("strings", "/questions");

            if (json != null) {
                return MAPPER.readValue(json, Map.class);
            }

        } catch (IOException e) {
            LOG.info("Exception reading questions summary.", e);
        }

        return null;
    }
}
