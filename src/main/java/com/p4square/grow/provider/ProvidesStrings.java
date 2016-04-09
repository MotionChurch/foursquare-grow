/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.provider;

/**
 * Indicates the ability to provide a String provider.
 *
 * Strings are typically configuration settings stored as a String.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public interface ProvidesStrings {
    /**
     * @return A Provider of Questions keyed by question id.
     */
    Provider<String, String> getStringProvider();
}