/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.io.IOException;
import java.util.Map;

import org.restlet.data.Status;
import org.restlet.resource.ServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import org.apache.log4j.Logger;

import com.p4square.grow.backend.GrowBackend;
import com.p4square.grow.backend.db.CassandraDatabase;

import com.p4square.grow.provider.CollectionProvider;
/**
 * This resource returns a listing of training items for a particular level.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class TrainingResource extends ServerResource {
    private final static Logger LOG = Logger.getLogger(TrainingResource.class);

    private CollectionProvider<String, String, String> mVideoProvider;

    private String mLevel;
    private String mVideoId;

    @Override
    public void doInit() {
        super.doInit();

        GrowBackend backend = (GrowBackend) getApplication();
        mVideoProvider = backend.getVideoProvider();

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

        try {
            if (mVideoId == null) {
                // Get all videos
                // TODO: This could be improved, but this is the quickest way to get
                // providers working.
                Map<String, String> videos = mVideoProvider.query(mLevel);
                if (videos.size() > 0) {
                    StringBuilder sb = new StringBuilder("{ \"level\": \"" + mLevel + "\"");
                    sb.append(", \"videos\": [");
                    boolean first = true;
                    for (String value : videos.values()) {
                        if (!first) {
                            sb.append(", ");
                        }
                        sb.append(value);
                        first = false;
                    }
                    sb.append("] }");
                    result = sb.toString();
                }

            } else {
                // Get single video
                result = mVideoProvider.get(mLevel, mVideoId);
            }

            if (result == null) {
                // 404
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return null;
            }

            return new StringRepresentation(result);

        } catch (IOException e) {
            LOG.error("IOException fetch video: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
    }
}
