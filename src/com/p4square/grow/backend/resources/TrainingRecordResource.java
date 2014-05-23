/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.io.IOException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import org.restlet.ext.jackson.JacksonRepresentation;

import org.apache.log4j.Logger;

import com.p4square.grow.backend.GrowBackend;

import com.p4square.grow.model.Chapter;
import com.p4square.grow.model.Playlist;
import com.p4square.grow.model.VideoRecord;
import com.p4square.grow.model.TrainingRecord;

import com.p4square.grow.provider.CollectionProvider;
import com.p4square.grow.provider.JsonEncodedProvider;
import com.p4square.grow.provider.Provider;
import com.p4square.grow.provider.ProvidesAssessments;
import com.p4square.grow.provider.ProvidesTrainingRecords;

import com.p4square.grow.model.Score;

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class TrainingRecordResource extends ServerResource {
    private static final Logger LOG = Logger.getLogger(TrainingRecordResource.class);
    private static final ObjectMapper MAPPER = JsonEncodedProvider.MAPPER;

    static enum RequestType {
        SUMMARY, VIDEO
    }

    private Provider<String, TrainingRecord> mTrainingRecordProvider;
    private CollectionProvider<String, String, String> mAnswerProvider;

    private RequestType mRequestType;
    private String mUserId;
    private String mVideoId;
    private TrainingRecord mRecord;

    @Override
    public void doInit() {
        super.doInit();

        mTrainingRecordProvider = ((ProvidesTrainingRecords) getApplication()).getTrainingRecordProvider();
        mAnswerProvider = ((ProvidesAssessments) getApplication()).getAnswerProvider();

        mUserId = getAttribute("userId");
        mVideoId = getAttribute("videoId");

        try {
            Playlist defaultPlaylist = ((GrowBackend) getApplication()).getDefaultPlaylist();

            mRecord = mTrainingRecordProvider.get(mUserId);
            if (mRecord == null) {
                mRecord = new TrainingRecord();
                mRecord.setPlaylist(defaultPlaylist);
                skipAssessedChapters(mUserId, mRecord);
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

    /**
     * Mark the chapters which the user assessed through as not required.
     */
    private void skipAssessedChapters(String userId, TrainingRecord record) {
        // Get the user's score.
        double assessedScore = 0;

        try {
            String summaryString = mAnswerProvider.get(userId, "summary");
            if (summaryString == null) {
                LOG.warn("Asked to create training record for unassessed user " + userId);
                return;
            }
            Map<?,?> summary = MAPPER.readValue(summaryString, Map.class);

            if (summary.containsKey("score")) {
                assessedScore = (Double) summary.get("score");
            }

        } catch (IOException e) {
            LOG.error("IOException fetching assessment record for " + userId, e);
            return;
        }

        // Mark the correct videos as not required.
        Playlist playlist = record.getPlaylist();

        for (Map.Entry<String, Chapter> entry : playlist.getChaptersMap().entrySet()) {
            String chapterId = entry.getKey();
            Chapter chapter = entry.getValue();
            boolean required;

            if ("introduction".equals(chapter)) {
                // Introduction chapter is always required
                required = true;

            } else {
                // Chapter required if the floor of the score is <= the chapter's numeric value.
                required = Math.floor(assessedScore) <= Score.numericScore(chapterId);
            }

            if (!required) {
                for (VideoRecord video : chapter.getVideos().values()) {
                    video.setRequired(required);
                }
            }
        }
    }
}
