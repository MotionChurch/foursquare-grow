/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.util.Map;
import java.util.HashMap;

import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnList;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import org.apache.log4j.Logger;

import com.p4square.grow.backend.GrowBackend;
import com.p4square.grow.backend.db.CassandraDatabase;

/**
 * 
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class TrainingRecordResource extends ServerResource {
    private final static Logger cLog = Logger.getLogger(TrainingRecordResource.class);

    static enum RequestType {
        SUMMARY, VIDEO
    }
    
    private GrowBackend mBackend;
    private CassandraDatabase mDb;

    private RequestType mRequestType;
    private String mUserId;
    private String mVideoId;

    @Override
    public void doInit() {
        super.doInit();

        mBackend = (GrowBackend) getApplication();
        mDb = mBackend.getDatabase();

        mUserId = getAttribute("userId");
        mVideoId = getAttribute("videoId");

        mRequestType = RequestType.SUMMARY;
        if (mVideoId != null) {
            mRequestType = RequestType.VIDEO;
        }
    }

    /**
     * Handle GET Requests.
     */
    @Override
    protected Representation get() {
        String result = null;

        switch (mRequestType) {
            case VIDEO:
                result = mDb.getKey("training", mUserId, mVideoId);
                break;

            case SUMMARY:
                result = buildSummary();
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
            case VIDEO:
                try {
                    mDb.putKey("training", mUserId, mVideoId, entity.getText());
                    mDb.putKey("training", mUserId, "lastVideo", mVideoId);
                    success = true;

                } catch (Exception e) {
                    cLog.warn("Caught exception updating training record: " + e.getMessage(), e);
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
     * This method compiles the summary of the training completed.
     */
    private String buildSummary() {
        StringBuilder sb = new StringBuilder("{ ");

        // Last question answered
        final String lastVideo = mDb.getKey("training", mUserId, "lastVideo");
        if (lastVideo != null) {
            sb.append("\"lastVideo\": \"" + lastVideo + "\", ");
        }

        // List of videos watched
        sb.append("\"videos\": { ");
        ColumnList<String> row = mDb.getRow("training", mUserId);
        if (!row.isEmpty()) {
            boolean first = true;
            for (Column<String> c : row) {
                if ("lastVideo".equals(c.getName())) {
                    continue;
                }

                if (first) {
                    sb.append("\"" + c.getName() + "\": ");
                    first = false;
                } else {
                    sb.append(", \"" + c.getName() + "\": ");
                }

                sb.append(c.getStringValue()); 
            }
        }
        sb.append(" }");


        sb.append(" }");
        return sb.toString();
    }
     
}
