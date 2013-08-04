/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnList;

import org.restlet.data.Status;
import org.restlet.resource.ServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import org.apache.log4j.Logger;

import com.p4square.grow.backend.GrowBackend;
import com.p4square.grow.backend.db.CassandraDatabase;

/**
 * This resource returns a listing of training items for a particular level.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class TrainingResource extends ServerResource {
    private final static Logger cLog = Logger.getLogger(TrainingResource.class);

    private GrowBackend mBackend;
    private CassandraDatabase mDb;

    private String mLevel;
    private String mVideoId;

    @Override
    public void doInit() {
        super.doInit();

        mBackend = (GrowBackend) getApplication();
        mDb = mBackend.getDatabase();

        mLevel = getAttribute("level");
        mVideoId = getAttribute("videoId");
    }

    /**
     * Handle GET Requests.
     */
    @Override
    protected Representation get() {
        String result = null;

        if (mLevel == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }

        if (mVideoId == null) {
            // Get all videos
            ColumnList<String> row = mDb.getRow("strings", "/training/" + mLevel);
            if (!row.isEmpty()) {
                StringBuilder sb = new StringBuilder("{ \"level\": \"" + mLevel + "\"");
                sb.append(", \"videos\": [");
                boolean first = true;
                for (Column<String> c : row) {
                    if (!first) {
                        sb.append(", ");
                    }
                    sb.append(c.getStringValue());
                    first = false;
                }
                sb.append("] }");
                result = sb.toString();
            }

        } else {
            // Get single video
            result = mDb.getKey("strings", "/training/" + mLevel, mVideoId);
        }

        if (result == null) {
            // 404
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }

        return new StringRepresentation(result);
    }
}
