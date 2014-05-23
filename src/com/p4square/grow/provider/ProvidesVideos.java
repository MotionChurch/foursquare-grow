/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.provider;

/**
 * 
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public interface ProvidesVideos {
    /**
     * @return A Provider of Questions keyed by question id.
     */
    CollectionProvider<String, String, String> getVideoProvider();
}
