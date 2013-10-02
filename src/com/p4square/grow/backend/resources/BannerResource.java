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
 * Fetches or sets the banner string.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class BannerResource extends ServerResource {
    private static final Logger LOG = Logger.getLogger(BannerResource.class);

    private CassandraDatabase mDb;

    @Override
    public void doInit() {
        super.doInit();

        final GrowBackend backend = (GrowBackend) getApplication();
        mDb = backend.getDatabase();
    }

    /**
     * Handle GET Requests.
     */
    @Override
    protected Representation get() {
        String result = mDb.getKey("strings", "banner");

        if (result == null || result.length() == 0) {
            result = "{}";
        }

        return new StringRepresentation(result);
    }

    /**
     * Handle PUT requests
     */
    @Override
    protected Representation put(Representation entity) {
        try {
            mDb.putKey("strings", "banner", entity.getText());
            setStatus(Status.SUCCESS_NO_CONTENT);

        } catch (IOException e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return null;
    }
}
