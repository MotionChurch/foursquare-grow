/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import freemarker.template.Template;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;

import org.apache.log4j.Logger;

import net.jesterpm.fmfacade.json.JsonRequestClient;
import net.jesterpm.fmfacade.json.JsonResponse;

import net.jesterpm.fmfacade.FreeMarkerPageResource;

import com.p4square.grow.config.Config;
import com.p4square.grow.model.TrainingRecord;
import com.p4square.grow.model.VideoRecord;
import com.p4square.grow.model.Playlist;
import com.p4square.grow.provider.TrainingRecordProvider;
import com.p4square.grow.provider.Provider;

/**
 * TrainingPageResource handles rendering the training page.
 *
 * This resource expects the user to be authenticated and the ClientInfo User object
 * to be populated.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class TrainingPageResource extends FreeMarkerPageResource {
    private static final Logger LOG = Logger.getLogger(TrainingPageResource.class);

    private static final String[] CHAPTERS = { "introduction", "seeker", "believer", "disciple", "teacher" };
    private static final Comparator<Map<String, Object>> VIDEO_COMPARATOR = new Comparator<Map<String, Object>>() {
        @Override
        public int compare(Map<String, Object> left, Map<String, Object> right) {
            String leftNumberStr = (String) left.get("number");
            String rightNumberStr = (String) right.get("number");

            if (leftNumberStr == null || rightNumberStr == null) {
                return -1;
            }

            double leftNumber = Double.valueOf(leftNumberStr);
            double rightNumber = Double.valueOf(rightNumberStr);

            return Double.compare(leftNumber, rightNumber);
        }
    };

    private Config mConfig;
    private Template mTrainingTemplate;
    private JsonRequestClient mJsonClient;

    private Provider<String, TrainingRecord> mTrainingRecordProvider;
    private FeedData mFeedData;

    // Fields pertaining to this request.
    protected String mChapter;
    protected String mUserId;

    @Override
    public void doInit() {
        super.doInit();

        GrowFrontend growFrontend = (GrowFrontend) getApplication();
        mConfig = growFrontend.getConfig();
        mTrainingTemplate = growFrontend.getTemplate("templates/training.ftl");
        if (mTrainingTemplate == null) {
            LOG.fatal("Could not find training template.");
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        mJsonClient = new JsonRequestClient(getContext().getClientDispatcher());
        mTrainingRecordProvider = new TrainingRecordProvider<String>(new JsonRequestProvider<TrainingRecord>(getContext().getClientDispatcher(), TrainingRecord.class)) {
            @Override
            public String makeKey(String userid) {
                return getBackendEndpoint() + "/accounts/" + userid + "/training";
            }
        };

        mFeedData = new FeedData(getContext(), mConfig);

        mChapter = getAttribute("chapter");
        mUserId = getRequest().getClientInfo().getUser().getIdentifier();
    }

    /**
     * Return a page of videos.
     */
    @Override
    protected Representation get() {
        try {
            // Get the training summary
            TrainingRecord trainingRecord = mTrainingRecordProvider.get(mUserId);
            if (trainingRecord == null) {
                setStatus(Status.SERVER_ERROR_INTERNAL);
                return new ErrorPage("Could not retrieve TrainingRecord.");
            }

            Playlist playlist = trainingRecord.getPlaylist();
            Map<String, Boolean> chapters = playlist.getChapterStatuses();
            Map<String, Boolean> allowedChapters = new LinkedHashMap<String, Boolean>();

            // The user is not allowed to view chapters after his highest completed chapter.
            // In this loop we find which chapters are allowed and check if the user tried
            // to skip ahead.
            boolean allowUserToSkip = mConfig.getBoolean("allowUserToSkip", false) || getQueryValue("magicskip") != null;
            String defaultChapter = null;
            boolean userTriedToSkip = false;
            int overallProgress = 0;

            boolean foundRequired = false;
            for (String chapterId : getChaptersInOrder()) {
                boolean allowed = true;

                Boolean completed = chapters.get(chapterId);
                if (completed != null) {
                    if (!foundRequired) {
                       if (!completed) {
                            // The first incomplete chapter is the highest allowed chapter.
                            foundRequired = true;
                            defaultChapter = chapterId;
                       }

                    } else {
                        allowed = allowUserToSkip;

                        if (!allowUserToSkip && chapterId.equals(mChapter)) {
                            userTriedToSkip = true;
                        }
                    }

                    allowedChapters.put(chapterId, allowed);

                    if (completed) {
                        overallProgress++;
                    }
                }
            }

            // Overall progress is the percentage of chapters complete
            overallProgress = (int) ((double) overallProgress / getChaptersInOrder().length * 100);

            if (defaultChapter == null) {
                // Everything is completed... send them back to introduction.
                defaultChapter = "introduction";
            }

            if (mChapter == null || userTriedToSkip) {
                // No chapter was specified or the user tried to skip ahead.
                // Either case, redirect.
                String nextPage = mConfig.getString("dynamicRoot", "");
                nextPage += "/account/training/" + defaultChapter;
                getResponse().redirectSeeOther(nextPage);
                return new StringRepresentation("Redirecting to " + nextPage);
            }


            // Get videos for the chapter.
            List<Map<String, Object>> videos = null;
            {
                JsonResponse response = backendGet("/training/" + mChapter);
                if (!response.getStatus().isSuccess()) {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    return null;
                }
                videos = (List<Map<String, Object>>) response.getMap().get("videos");
                Collections.sort(videos, VIDEO_COMPARATOR);
            }

            // Mark the completed videos as completed
            int chapterProgress = 0;
            for (Map<String, Object> video : videos) {
                boolean completed = false;
                VideoRecord record = playlist.find((String) video.get("id"));
                LOG.info("VideoId: " + video.get("id"));
                if (record != null) {
                    LOG.info("VideoRecord: " + record.getComplete());
                    completed = record.getComplete();
                }
                video.put("completed", completed);

                if (completed) {
                    chapterProgress++;
                }
            }
            chapterProgress = chapterProgress * 100 / videos.size();

            Map root = getRootObject();
            root.put("chapter", mChapter);
            root.put("chapters", allowedChapters.keySet());
            root.put("isChapterAllowed", allowedChapters);
            root.put("chapterProgress", chapterProgress);
            root.put("overallProgress", overallProgress);
            root.put("videos", videos);
            root.put("allowUserToSkip", allowUserToSkip);

            boolean showfeed = getQueryValue("showfeed") != null;
            root.put("showfeed", showfeed);
            if (showfeed) {
                root.put("feeddata", mFeedData);
            }

            return new TemplateRepresentation(mTrainingTemplate, root, MediaType.TEXT_HTML);

        } catch (Exception e) {
            LOG.fatal("Could not render page: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return ErrorPage.RENDER_ERROR;
        }
    }

    /**
     * This method returns a list of chapters in the correct order.
     */
    protected String[] getChaptersInOrder() {
        return CHAPTERS;
    }

    /**
     * @return The backend endpoint URI
     */
    private String getBackendEndpoint() {
        return mConfig.getString("backendUri", "riap://component/backend");
    }

    /**
     * Helper method to send a GET to the backend.
     */
    private JsonResponse backendGet(final String uri) {
        LOG.debug("Sending backend GET " + uri);

        final JsonResponse response = mJsonClient.get(getBackendEndpoint() + uri);
        final Status status = response.getStatus();
        if (!status.isSuccess() && !Status.CLIENT_ERROR_NOT_FOUND.equals(status)) {
            LOG.warn("Error making backend request for '" + uri + "'. status = " + response.getStatus().toString());
        }

        return response;
    }

}
