/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.provider;

import java.io.IOException;

/**
 * DelegateProvider wraps an existing Provider an transforms the key from
 * type K to type D.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public abstract class DelegateProvider<K, D, V> implements Provider<K, V> {

    private Provider<D, V> mProvider;

    public DelegateProvider(final Provider<D, V> provider) {
        mProvider = provider;
    }

    @Override
    public V get(final K key) throws IOException {
        return mProvider.get(makeKey(key));
    }

    @Override
    public void put(final K key, final V obj) throws IOException {
        mProvider.put(makeKey(key), obj);
    }

    /**
     * Make a Key for questionId.
     *
     * @param questionId The question id.
     * @return a key for questionId.
     */
    protected abstract D makeKey(final K input);
}
