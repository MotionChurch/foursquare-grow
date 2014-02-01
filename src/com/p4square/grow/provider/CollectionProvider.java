/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.provider;

import java.io.IOException;
import java.util.Map;

/**
 * ListProvider is the logical extension of Provider for dealing with lists of
 * items.
 *
 * @param C The type of the collection key.
 * @param K The type of the item key.
 * @param V The type of the value.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public interface CollectionProvider<C, K, V> {
    /**
     * Retrieve a specific object from the collection.
     *
     * @param collection The collection key.
     * @param key The key for the object in the collection.
     * @return The object or null if not found.
     */
    V get(C collection, K key) throws IOException;

    /**
     * Retrieve a collection.
     *
     * The returned map will never be null.
     *
     * @param collection The collection key.
     * @return A Map of keys to values.
     */
    Map<K, V> query(C collection) throws IOException;

    /**
     * Retrieve a portion of a collection.
     *
     * The returned map will never be null.
     *
     * @param collection The collection key.
     * @param limit Max number of items to return.
     * @return A Map of keys to values.
     */
    Map<K, V> query(C collection, int limit) throws IOException;

    /**
     * Persist the object with the given key.
     *
     * @param collection The collection key.
     * @param key The key for the object in the collection.
     * @param obj The object to persist.
     */
    void put(C collection, K key, V obj) throws IOException;
}
