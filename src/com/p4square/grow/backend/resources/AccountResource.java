/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.io.IOException;

import org.restlet.data.Status;
import org.restlet.resource.ServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import org.apache.log4j.Logger;

import com.p4square.grow.backend.GrowBackend;
import com.p4square.grow.backend.db.CassandraDatabase;

/**
 * Stores a document about a user.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class AccountResource extends ServerResource {
    private static final Logger LOG = Logger.getLogger(AccountResource.class);

    private CassandraDatabase mDb;

    private String mUserId;

    @Override
    public void doInit() {
        super.doInit();

        final GrowBackend backend = (GrowBackend) getApplication();
        mDb = backend.getDatabase();

        mUserId = getAttribute("userId");
    }

    /**
     * Handle GET Requests.
     */
    @Override
    protected Representation get() {
        String result = mDb.getKey("accounts", mUserId);

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
        try {
            mDb.putKey("accounts", mUserId, entity.getText());
            setStatus(Status.SUCCESS_NO_CONTENT);

        } catch (IOException e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return null;
    }
}
