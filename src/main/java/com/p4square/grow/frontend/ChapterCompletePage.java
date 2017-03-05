/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import com.p4square.f1oauth.FellowshipOneIntegrationDriver;
import freemarker.template.Template;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.ext.freemarker.TemplateRepresentation;

import org.apache.log4j.Logger;

import com.p4square.fmfacade.FreeMarkerPageResource;

import com.p4square.fmfacade.json.JsonRequestClient;
import com.p4square.fmfacade.json.JsonResponse;
import com.p4square.fmfacade.json.ClientException;

import com.p4square.f1oauth.Attribute;
import com.p4square.f1oauth.F1API;
import com.p4square.f1oauth.F1User;

import com.p4square.grow.config.Config;
import com.p4square.grow.model.TrainingRecord;
import com.p4square.grow.provider.Provider;
import com.p4square.grow.provider.TrainingRecordProvider;
import org.restlet.security.User;

/**
 * This resource displays the transitional page between chapters.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class ChapterCompletePage extends FreeMarkerPageResource {
    private static final Logger LOG = Logger.getLogger(ChapterCompletePage.class);

    private GrowFrontend mGrowFrontend;
    private Config mConfig;
    private JsonRequestClient mJsonClient;
    private Provider<String, TrainingRecord> mTrainingRecordProvider;

    private String mUserId;
    private String mChapter;

    @Override
    public void doInit() {
        super.doInit();

        mGrowFrontend = (GrowFrontend) getApplication();
        mConfig = mGrowFrontend.getConfig();

        mJsonClient = new JsonRequestClient(getContext().getClientDispatcher());
        mTrainingRecordProvider = new TrainingRecordProvider<String>(
                new JsonRequestProvider<TrainingRecord>(
                    getContext().getClientDispatcher(),
                    TrainingRecord.class)) {
            @Override
            public String makeKey(String userid) {
                return getBackendEndpoint() + "/accounts/" + userid + "/training";
            }
        };

        mUserId = getRequest().getClientInfo().getUser().getIdentifier();

        mChapter = getAttribute("chapter");
    }

    /**
     * Return the login page.
     */
    @Override
    protected Representation get() {
        try {
            Map<String, Object> root = getRootObject();

            // Get the training summary
            TrainingRecord trainingRecord = mTrainingRecordProvider.get(mUserId);
            if (trainingRecord == null) {
                // Wait. What? Everyone has a training record...
                setStatus(Status.SERVER_ERROR_INTERNAL);
                return new ErrorPage("Could not retrieve your training record.");
            }

            // Verify they completed the chapter.
            Map<String, Boolean> chapters = trainingRecord.getPlaylist().getChapterStatuses();
            Boolean completed = chapters.get(mChapter);
            if (completed == null || !completed) {
                // Redirect back to training page...
                String nextPage = mConfig.getString("dynamicRoot", "");
                nextPage += "/account/training/" + mChapter;
                getResponse().redirectSeeOther(nextPage);
                return new StringRepresentation("Redirecting to " + nextPage);
            }

            // Publish the training chapter complete attribute.
            assignAttribute();

            // Find the next chapter
            String nextChapter = null;
            {
                int min = Integer.MAX_VALUE;
                for (Map.Entry<String, Boolean> chapter : chapters.entrySet()) {
                    int index = chapterIndex(chapter.getKey());
                    if (!chapter.getValue() && index < min) {
                        min = index;
                        nextChapter = chapter.getKey();
                    }
                }
            }

            String nextOverride = getQueryValue("next");
            if (nextOverride != null) {
                nextChapter = nextOverride;
            }

            root.put("stage", mChapter);
            root.put("nextstage", nextChapter);

            /*
             * We will display one of two transitional pages:
             * 
             * If the next chapter has a forward page, display the forward page.
             * Else, if this chapter is not "Introduction", display the chapter
             * complete message.
             */
            Template t = mGrowFrontend.getTemplate("templates/stage-"
                    + nextChapter + "-forward.ftl");

            if (t == null) {
                // Skip the chapter complete message for "Introduction"
                if ("introduction".equals(mChapter)) {
                    String nextPage = mConfig.getString("dynamicRoot", "");
                    nextPage += "/account/training/" + nextChapter;
                    getResponse().redirectSeeOther(nextPage);
                    return new StringRepresentation("Redirecting to " + nextPage);
                }

                t = mGrowFrontend.getTemplate("templates/stage-complete.ftl");
                if (t == null) {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    return ErrorPage.TEMPLATE_NOT_FOUND;
                }
            }

            return new TemplateRepresentation(t, root, MediaType.TEXT_HTML);

        } catch (Exception e) {
            LOG.fatal("Could not render page: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return ErrorPage.RENDER_ERROR;
        }
    }

    private void assignAttribute() throws IOException {
        final ProgressReporter reporter = mGrowFrontend.getThirdPartyIntegrationFactory().getProgressReporter();

        final User user = getRequest().getClientInfo().getUser();
        final Date completionDate = new Date();

        reporter.reportChapterComplete(user, mChapter, completionDate);
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
            LOG.warn("Error making backend request for '" + uri
                    + "'. status = " + response.getStatus().toString());
        }

        return response;
    }

    int chapterIndex(String chapter) {
        if ("leader".equals(chapter)) {
            return 5;
        } else if ("teacher".equals(chapter)) {
            return 4;
        } else if ("disciple".equals(chapter)) {
            return 3;
        } else if ("believer".equals(chapter)) {
            return 2;
        } else if ("seeker".equals(chapter)) {
            return 1;
        } else {
            return Integer.MAX_VALUE;
        }
    }
}
