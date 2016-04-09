/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.io.IOException;

import org.restlet.data.Status;
import org.restlet.resource.ServerResource;
import org.restlet.representation.Representation;

import org.restlet.ext.jackson.JacksonRepresentation;

import org.apache.log4j.Logger;

import com.p4square.grow.model.UserRecord;
import com.p4square.grow.provider.Provider;
import com.p4square.grow.provider.ProvidesUserRecords;
import com.p4square.grow.provider.JsonEncodedProvider;

/**
 * Stores a document about a user.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class AccountResource extends ServerResource {
    private static final Logger LOG = Logger.getLogger(AccountResource.class);

    private Provider<String, UserRecord> mUserRecordProvider;

    private String mUserId;

    @Override
    public void doInit() {
        super.doInit();

        final ProvidesUserRecords backend = (ProvidesUserRecords) getApplication();
        mUserRecordProvider = backend.getUserRecordProvider();

        mUserId = getAttribute("userId");
    }

    /**
     * Handle GET Requests.
     */
    @Override
    protected Representation get() {
        try {
            UserRecord result = mUserRecordProvider.get(mUserId);

            if (result == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return null;
            }

            JacksonRepresentation<UserRecord> rep = new JacksonRepresentation<UserRecord>(result);
            rep.setObjectMapper(JsonEncodedProvider.MAPPER);
            return rep;

        } catch (IOException e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
            return null;
        }
    }

    /**
     * Handle PUT requests
     */
    @Override
    protected Representation put(Representation entity) {
        try {
            JacksonRepresentation<UserRecord> representation =
                new JacksonRepresentation<>(entity, UserRecord.class);
            representation.setObjectMapper(JsonEncodedProvider.MAPPER);
            UserRecord record = representation.getObject();

            mUserRecordProvider.put(mUserId, record);
            setStatus(Status.SUCCESS_NO_CONTENT);

        } catch (IOException e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return null;
    }
}
