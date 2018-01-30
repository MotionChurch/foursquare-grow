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

    /**
     * Return a ProgressReporter for this Church Management System.
     *
     * The ProgressReporter should be thread-safe.
     *
     * @return The ProgressReporter.
     */
    ProgressReporter getProgressReporter();

    /**
     * Check if the IntegrationDriver is configured correctly and working.
     *
     * This method should try to contact the CMS to ensure endpoints,
     * credentials, etc. are working correctly.
     *
     * @return true for success.
     */
    boolean doHealthCheck();
}
