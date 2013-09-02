/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend;

import java.util.HashMap;
import java.util.Map;

import freemarker.template.Template;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;

import org.apache.log4j.Logger;

import net.jesterpm.fmfacade.json.JsonRequestClient;
import net.jesterpm.fmfacade.json.JsonResponse;

import com.p4square.grow.config.Config;

/**
 * VideosResource returns JSON blobs with video information and records watched
 * videos.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class VideosResource extends ServerResource {
    private static Logger cLog = Logger.getLogger(VideosResource.class);

    private Config mConfig;
    private JsonRequestClient mJsonClient;

    // Fields pertaining to this request.
    private String mChapter;
    private String mVideoId;
    private String mUserId;

    @Override
    public void doInit() {
        super.doInit();

        GrowFrontend growFrontend = (GrowFrontend) getApplication();
        mConfig = growFrontend.getConfig();

        mJsonClient = new JsonRequestClient(getContext().getClientDispatcher());

        mChapter = getAttribute("chapter");
        mVideoId = getAttribute("videoId");
        mUserId = getRequest().getClientInfo().getUser().getIdentifier();
    }

    /**
     * Fetch a video record from the backend.
     */
    @Override
    protected Representation get() {
        try {
            JsonResponse response = backendGet("/training/" + mChapter + "/videos/" + mVideoId);

            if (response.getStatus().isSuccess()) {
                return new JacksonRepresentation<Map>(response.getMap());

            } else {
                setStatus(response.getStatus());
                return null;
            }

        } catch (Exception e) {
            cLog.fatal("Could not render page: " + e.getMessage(), e);
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
    }

    /**
     * Mark a video as completed.
     */
    @Override
    protected Representation post(Representation entity) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("completed", "t");
        JsonResponse response = backendPut("/accounts/" + mUserId + "/training/videos/" + mVideoId, data);

        if (!response.getStatus().isSuccess()) {
            // Something went wrong talking to the backend, error out.
            cLog.fatal("Error recording completed video " + response.getStatus());
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return ErrorPage.BACKEND_ERROR;
        }

        setStatus(Status.SUCCESS_NO_CONTENT);
        return null;
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
        cLog.debug("Sending backend GET " + uri);

        final JsonResponse response = mJsonClient.get(getBackendEndpoint() + uri);
        final Status status = response.getStatus();
        if (!status.isSuccess() && !Status.CLIENT_ERROR_NOT_FOUND.equals(status)) {
            cLog.warn("Error making backend request for '" + uri + "'. status = " + response.getStatus().toString());
        }

        return response;
    }

    private JsonResponse backendPut(final String uri, final Map data) {
        cLog.debug("Sending backend PUT " + uri);

        final JsonResponse response = mJsonClient.put(getBackendEndpoint() + uri, data);
        final Status status = response.getStatus();
        if (!status.isSuccess() && !Status.CLIENT_ERROR_NOT_FOUND.equals(status)) {
            cLog.warn("Error making backend request for '" + uri + "'. status = " + response.getStatus().toString());
        }

        return response;
    }
}
