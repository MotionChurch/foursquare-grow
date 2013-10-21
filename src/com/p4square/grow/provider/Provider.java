/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.provider;

import java.io.IOException;

/**
 * Provider provides a simple interface for loading and persisting
 * objects.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public interface Provider<K, V> {
    /**
     * Retrieve the object with the given key.
     *
     * @param key The key for the object.
     * @return The object or null if not found.
     */
    V get(K key) throws IOException;

    /**
     * Persist the object with the given key.
     *
     * @param key The key for the object.
     * @param obj The object to persist.
     */
    void put(K key, V obj) throws IOException;
}
