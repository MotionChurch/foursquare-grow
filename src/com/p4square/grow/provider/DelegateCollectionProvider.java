/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.provider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public abstract class DelegateCollectionProvider<C, DC, K, DK, V>
    implements CollectionProvider<C, K, V> {

    private CollectionProvider<DC, DK, V> mProvider;

    public DelegateCollectionProvider(final CollectionProvider<DC, DK, V> provider) {
        mProvider = provider;
    }

    public V get(C collection, K key) throws IOException {
        return mProvider.get(makeCollectionKey(collection), makeKey(key));
    }

    public Map<K, V> query(C collection) throws IOException {
        return query(collection, -1);
    }

    public Map<K, V> query(C collection, int limit) throws IOException {
        Map<DK, V> delegateResult =  mProvider.query(makeCollectionKey(collection), limit);
        Map<K, V> result = new HashMap<>();
        for (Map.Entry<DK, V> entry : delegateResult.entrySet()) {
            result.put(unmakeKey(entry.getKey()), entry.getValue());
        }

        return result;
    }

    public void put(C collection, K key, V obj) throws IOException {
        mProvider.put(makeCollectionKey(collection), makeKey(key), obj);
    }

    /**
     * Make a collection key for the delegated provider.
     *
     * @param input The pre-transform key.
     * @return the post-transform key.
     */
    protected abstract DC makeCollectionKey(final C input);

    /**
     * Make a key for the delegated provider.
     *
     * @param input The pre-transform key.
     * @return the post-transform key.
     */
    protected abstract DK makeKey(final K input);

    /**
     * Transform a key for the delegated provider to an input key.
     *
     * @param input The post-transform key.
     * @return the pre-transform key.
     */
    protected abstract K unmakeKey(final DK input);
}
