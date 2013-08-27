/*
 * Copyright 2013 Jesse Morgan
 */

package net.jesterpm.restlet.oauth;

import org.restlet.data.Status;

/**
 * Exception throw when the service provider returns an error.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class OAuthException extends Exception {
    private final Status mStatus;

    public OAuthException(Status status) {
        super("Service provider failed request: " + status.getDescription());
        mStatus = status;
    }

    public Status getStatus() {
        return mStatus;
    }
}
