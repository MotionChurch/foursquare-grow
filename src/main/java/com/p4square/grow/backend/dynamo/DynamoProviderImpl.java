/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.dynamo;

import java.io.IOException;

import com.p4square.grow.provider.Provider;
import com.p4square.grow.provider.JsonEncodedProvider;

/**
 * Provider implementation backed by a DynamoDB Table.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class DynamoProviderImpl<V> extends JsonEncodedProvider<V> implements Provider<DynamoKey, V> {
    private final DynamoDatabase mDb;

    public DynamoProviderImpl(DynamoDatabase db, Class<V> clazz) {
        super(clazz);

        mDb = db;
    }

    @Override
    public V get(DynamoKey key) throws IOException {
        String blob = mDb.getAttribute(key);
        return decode(blob);
    }

    @Override
    public void put(DynamoKey key, V obj) throws IOException {
        String blob = encode(obj);
        mDb.putAttribute(key, blob);
    }
}
