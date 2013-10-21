/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.db;

import java.io.IOException;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import com.p4square.grow.provider.JsonEncodedProvider;

/**
 * Provider implementation backed by a Cassandra ColumnFamily.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class CassandraProviderImpl<V> extends JsonEncodedProvider<CassandraKey, V> {
    private final CassandraDatabase mDb;
    private final String mColumnFamily;

    public CassandraProviderImpl(CassandraDatabase db, String columnFamily, Class<V> clazz) {
        super(clazz);

        mDb = db;
        mColumnFamily = columnFamily;
    }

    @Override
    public V get(CassandraKey key) throws IOException {
        String blob = mDb.getKey(mColumnFamily, key.getId(), key.getColumn());
        return decode(blob);
    }

    @Override
    public void put(CassandraKey key, V obj) throws IOException {
        String blob = encode(obj);
        mDb.putKey(mColumnFamily, key.getId(), key.getColumn(), blob);
    }
}
