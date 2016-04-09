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
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;

import org.apache.log4j.Logger;

import com.p4square.grow.backend.GrowBackend;
import com.p4square.grow.model.Question;
import com.p4square.grow.provider.JsonEncodedProvider;
import com.p4square.grow.provider.Provider;

/**
 * This resource manages assessment questions.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SurveyResource extends ServerResource {
    private static final Logger LOG = Logger.getLogger(SurveyResource.class);

    private static final ObjectMapper MAPPER = JsonEncodedProvider.MAPPER;

    private Provider<String, Question> mQuestionProvider;
    private Provider<String, String> mStringProvider;

    private String mQuestionId;

    @Override
    public void doInit() {
        super.doInit();

        final GrowBackend backend = (GrowBackend) getApplication();
        mQuestionProvider = backend.getQuestionProvider();
        mStringProvider = backend.getStringProvider();

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
            Question question = null;
            try {
                question = mQuestionProvider.get(mQuestionId);
            } catch (IOException e) {
                LOG.error("IOException loading question: " + e);
            }

            if (question == null) {
                // 404
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return null;
            }

            JacksonRepresentation<Question> rep = new JacksonRepresentation<>(question);
            rep.setObjectMapper(MAPPER);
            return rep;
        }

        return new StringRepresentation(result);
    }

    private Map<?, ?> getQuestionsSummary() {
        try {
            // TODO: This could be better. Quick fix for provider support.
            String json = mStringProvider.get("/questions");

            if (json != null) {
                return MAPPER.readValue(json, Map.class);
            }

        } catch (IOException e) {
            LOG.info("Exception reading questions summary.", e);
        }

        return null;
    }
}
