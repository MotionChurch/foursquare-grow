/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.util.Map;
import java.util.HashMap;

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
    private final static Logger cLog = Logger.getLogger(SurveyResource.class);

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
        String result = "";

        if (mQuestionId == null) {
            // TODO: List all question ids

        } else if (mQuestionId.equals("first")) {
            // TODO: Get the first question id from db?
            result = "1";

        } else {
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
}
