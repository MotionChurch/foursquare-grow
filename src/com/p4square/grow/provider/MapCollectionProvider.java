/*
 * Copyright 2015 Jesse Morgan
 */

package com.p4square.grow.provider;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * In-memory CollectionProvider implementation, useful for tests.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class MapCollectionProvider<C, K, V> implements CollectionProvider<C, K, V> {
    private final Map<C, Map<K, V>> mMap;

    public MapCollectionProvider() {
        mMap = new HashMap<>();
    }

    @Override
    public synchronized V get(C collection, K key) throws IOException {
        Map<K, V> map = mMap.get(collection);
        if (map != null) {
            return map.get(key);
        }

        return null;
    }

    @Override
    public synchronized Map<K, V> query(C collection) throws IOException {
        Map<K, V> map = mMap.get(collection);
        if (map == null) {
            map = new HashMap<K, V>();
        }

        return map;
    }

    @Override
    public synchronized Map<K, V> query(C collection, int limit) throws IOException {
        Map<K, V> map = query(collection);

        if (map.size() > limit) {
            Map<K, V> smallMap = new HashMap<>();

            Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
            for (int i = 0; i < limit; i++) {
                Map.Entry<K, V> entry = iterator.next();
                smallMap.put(entry.getKey(), entry.getValue());
            }

            return smallMap;

        } else {
            return map;
        }
    }

    @Override
    public synchronized void put(C collection, K key, V obj) throws IOException {
        Map<K, V> map = mMap.get(collection);
        if (map == null) {
            map = new HashMap<K, V>();
            mMap.put(collection, map);
        }

        map.put(key, obj);
    }
}
