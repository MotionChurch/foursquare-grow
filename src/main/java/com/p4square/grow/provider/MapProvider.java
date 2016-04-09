/*
 * Copyright 2015 Jesse Morgan
 */

package com.p4square.grow.provider;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * In-memory Provider implementation, useful for tests.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class MapProvider<K, V> implements Provider<K, V> {
    private final Map<K, V> mMap = new HashMap<K, V>();

    @Override
    public V get(K key) throws IOException {
        return mMap.get(key);
    }

    @Override
    public void put(K key, V obj) throws IOException {
        mMap.put(key, obj);
    }
}
