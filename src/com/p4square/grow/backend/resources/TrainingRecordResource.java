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

import org.codehaus.jackson.map.ObjectMapper;

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
    private static final String[] CHAPTERS = { "seeker", "believer", "disciple", "teacher" };

    private static final Logger LOG = Logger.getLogger(TrainingRecordResource.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

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

                    Playlist playlist = Playlist.load(mDb, mUserId);
                    if (playlist != null) {
                        VideoRecord r = playlist.find(mVideoId);
                        if (r != null && !r.getComplete()) {
                            r.complete();
                            Playlist.save(mDb, mUserId, playlist);
                        }
                    }

                    success = true;

                } catch (Exception e) {
                    LOG.warn("Caught exception updating training record: " + e.getMessage(), e);
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

        // Last watch video
        final String lastVideo = mDb.getKey("training", mUserId, "lastVideo");
        if (lastVideo != null) {
            sb.append("\"lastVideo\": \"" + lastVideo + "\", ");
        }

        // Get the user's video history
        sb.append("\"videos\": { ");
        ColumnList<String> row = mDb.getRow("training", mUserId);
        if (!row.isEmpty()) {
            boolean first = true;
            for (Column<String> c : row) {
                if ("lastVideo".equals(c.getName()) ||
                    "playlist".equals(c.getName())) {
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

        // Get the user's playlist
        try {
            Playlist playlist = Playlist.load(mDb, mUserId);
            if (playlist == null) {
                playlist = createInitialPlaylist();
            }

            sb.append(", \"playlist\": ");
            sb.append(playlist.toString());

            // Last Completed Section
            Map<String, Boolean> chapters = playlist.getChapterStatuses();
            String chaptersString = MAPPER.writeValueAsString(chapters);
            sb.append(", \"chapters\":");
            sb.append(chaptersString);


        } catch (IOException e) {
            LOG.warn("IOException loading playlist for user " + mUserId, e);
        }


        sb.append(" }");
        return sb.toString();
    }

    /**
     * Create the user's initial playlist.
     *
     * @return Returns the String representation of the initial playlist.
     */
    private Playlist createInitialPlaylist() throws IOException {
        Playlist playlist = new Playlist();

        // Get assessment score
        String summaryString = mDb.getKey("assessments", mUserId, "summary");
        if (summaryString == null) {
            return null;
        }
        Map<?,?> summary = MAPPER.readValue(summaryString, Map.class);
        double score = (Double) summary.get("score");

        // Get videos for each section and build playlist
        for (String chapter : CHAPTERS) {
            // Chapter required if the floor of the score is <= the chapter's numeric value.
            boolean required = score < Score.numericScore(chapter) + 1;

            ColumnList<String> row = mDb.getRow("strings", "/training/" + chapter);
            if (!row.isEmpty()) {
                for (Column<String> c : row) {
                    VideoRecord r = playlist.add(chapter, c.getName());
                    r.setRequired(required);
                }
            }
        }

        Playlist.save(mDb, mUserId, playlist);

        return playlist;
    }
}
