package com.p4square.grow.frontend;

import org.restlet.security.Verifier;

/**
 * An IntegrationDriver is used to create implementations of various objects
 * used to integration Grow with a particular Church Management System.
 */
public interface IntegrationDriver {

    /**
     * Create a new Restlet Verifier to authenticate users when they login to the site.
     *
     * @return A Verifier.
     */
    Verifier newUserAuthenticationVerifier();
}
