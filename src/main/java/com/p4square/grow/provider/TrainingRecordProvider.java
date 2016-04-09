/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.provider;

import java.io.IOException;

import com.p4square.grow.model.TrainingRecord;

/**
 * TrainingRecordProvider wraps an existing Provider to get and put TrainingRecords.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public abstract class TrainingRecordProvider<K> implements Provider<String, TrainingRecord> {

    private Provider<K, TrainingRecord> mProvider;

    public TrainingRecordProvider(Provider<K, TrainingRecord> provider) {
        mProvider = provider;
    }

    @Override
    public TrainingRecord get(String key) throws IOException {
        return mProvider.get(makeKey(key));
    }

    @Override
    public void put(String key, TrainingRecord obj) throws IOException {
        mProvider.put(makeKey(key), obj);
    }

    /**
     * Make a Key for a TrainingRecord..
     *
     * @param userId The user id.
     * @return a key for the TrainingRecord of userid.
     */
    protected abstract K makeKey(String userId);
}
