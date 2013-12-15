/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.io.IOException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnList;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import org.restlet.ext.jackson.JacksonRepresentation;

import org.apache.log4j.Logger;

import com.p4square.grow.backend.GrowBackend;
import com.p4square.grow.backend.db.CassandraDatabase;

import com.p4square.grow.model.Playlist;
import com.p4square.grow.model.VideoRecord;
import com.p4square.grow.model.TrainingRecord;

import com.p4square.grow.provider.Provider;
import com.p4square.grow.provider.ProvidesTrainingRecords;
import com.p4square.grow.provider.JsonEncodedProvider;

import com.p4square.grow.model.Score;

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class TrainingRecordResource extends ServerResource {
    private static final Logger LOG = Logger.getLogger(TrainingRecordResource.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static enum RequestType {
        SUMMARY, VIDEO
    }

    private CassandraDatabase mDb;
    private Provider<String, TrainingRecord> mTrainingRecordProvider;

    private RequestType mRequestType;
    private String mUserId;
    private String mVideoId;
    private TrainingRecord mRecord;

    @Override
    public void doInit() {
        super.doInit();

        mDb = ((GrowBackend) getApplication()).getDatabase();
        mTrainingRecordProvider = ((ProvidesTrainingRecords) getApplication()).getTrainingRecordProvider();

        mUserId = getAttribute("userId");
        mVideoId = getAttribute("videoId");

        try {
            Playlist defaultPlaylist = ((GrowBackend) getApplication()).getDefaultPlaylist();

            mRecord = mTrainingRecordProvider.get(mUserId);
            if (mRecord == null) {
                mRecord = new TrainingRecord();
                mRecord.setPlaylist(defaultPlaylist);
            } else {
                // Merge the playlist with the most recent version.
                mRecord.getPlaylist().merge(defaultPlaylist);
            }

        } catch (IOException e) {
            LOG.error("IOException loading TrainingRecord: " + e.getMessage(), e);
            mRecord = null;
        }

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
        JacksonRepresentation<?> rep = null;

        if (mRecord == null) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }

        switch (mRequestType) {
            case VIDEO:
                VideoRecord video = mRecord.getPlaylist().find(mVideoId);
                if (video == null) {
                    break; // Fall through and return 404
                }
                rep = new JacksonRepresentation<VideoRecord>(video);
                break;

            case SUMMARY:
                rep = new JacksonRepresentation<TrainingRecord>(mRecord);
                break;
        }

        if (rep == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;

        } else {
            rep.setObjectMapper(JsonEncodedProvider.MAPPER);
            return rep;
        }
    }

    /**
     * Handle PUT requests
     */
    @Override
    protected Representation put(Representation entity) {
        if (mRecord == null) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }

        switch (mRequestType) {
            case VIDEO:
                try {
                    JacksonRepresentation<VideoRecord> representation = 
                        new JacksonRepresentation<>(entity, VideoRecord.class);
                    representation.setObjectMapper(JsonEncodedProvider.MAPPER);
                    VideoRecord update = representation.getObject();
                    VideoRecord video = mRecord.getPlaylist().find(mVideoId);

                    if (video == null) {
                        // TODO: Video isn't on their playlist...
                        LOG.warn("Skipping video completion for video missing from playlist.");

                    } else if (update.getComplete() && !video.getComplete()) {
                        // Video was newly completed
                        video.complete();
                        mRecord.setLastVideo(mVideoId);

                        mTrainingRecordProvider.put(mUserId, mRecord);
                    }

                    setStatus(Status.SUCCESS_NO_CONTENT);

                } catch (Exception e) {
                    LOG.warn("Caught exception updating training record: " + e.getMessage(), e);
                    setStatus(Status.SERVER_ERROR_INTERNAL);
                }
                break;

            default:
                setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        }

        return null;
    }

}
