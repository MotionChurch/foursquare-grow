/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.db;

import java.io.IOException;

import com.p4square.grow.provider.Provider;
import com.p4square.grow.provider.JsonEncodedProvider;

/**
 * Provider implementation backed by a Cassandra ColumnFamily.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class CassandraProviderImpl<V> extends JsonEncodedProvider<V> implements Provider<CassandraKey, V> {
    private final CassandraDatabase mDb;

    public CassandraProviderImpl(CassandraDatabase db, Class<V> clazz) {
        super(clazz);

        mDb = db;
    }

    @Override
    public V get(CassandraKey key) throws IOException {
        String blob = mDb.getKey(key.getColumnFamily(), key.getId(), key.getColumn());
        if (mClazz == String.class) {
            return (V) blob;
        }
        return decode(blob);
    }

    @Override
    public void put(CassandraKey key, V obj) throws IOException {
        String blob;
        if (mClazz == String.class) {
            blob = (String) obj;
        } else {
            blob = encode(obj);
        }
        mDb.putKey(key.getColumnFamily(), key.getId(), key.getColumn(), blob);
    }
}
