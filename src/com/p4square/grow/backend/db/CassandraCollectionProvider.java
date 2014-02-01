/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.db;

import java.io.IOException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnList;

import com.p4square.grow.provider.CollectionProvider;
import com.p4square.grow.provider.JsonEncodedProvider;

/**
 * CollectionProvider implementation backed by a Cassandra ColumnFamily.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class CassandraCollectionProvider<V> implements CollectionProvider<String, String, V> {
    private final CassandraDatabase mDb;
    private final String mCF;
    private final Class<V> mClazz;

    public CassandraCollectionProvider(CassandraDatabase db, String columnFamily, Class<V> clazz) {
        mDb = db;
        mCF = columnFamily;
        mClazz = clazz;
    }

    @Override
    public V get(String collection, String key) throws IOException {
        String blob = mDb.getKey(mCF, collection, key);
        return decode(blob);
    }

    @Override
    public Map<String, V> query(String collection) throws IOException {
        return query(collection, -1);
    }

    @Override
    public Map<String, V> query(String collection, int limit) throws IOException {
        Map<String, V> result = new HashMap<>();

        ColumnList<String> row = mDb.getRow(mCF, collection);
        if (!row.isEmpty()) {
            int count = 0;
            for (Column<String> c : row) {
                String key = c.getName();
                String blob = c.getStringValue();
                V obj = decode(blob);

                result.put(key, obj);

                if (limit >= 0 && ++count > limit) {
                    break; // Limit reached.
                }
            }
        }

        return Collections.unmodifiableMap(result);
    }

    @Override
    public void put(String collection, String key, V obj) throws IOException {
        String blob = encode(obj);
        mDb.putKey(mCF, collection, key, blob);
    }

    /**
     * Encode the object as JSON.
     *
     * @param obj The object to encode.
     * @return The JSON encoding of obj.
     * @throws IOException if the object cannot be encoded.
     */
    protected String encode(V obj) throws IOException {
        return JsonEncodedProvider.MAPPER.writeValueAsString(obj);
    }

    /**
     * Decode the JSON string as an object.
     *
     * @param blob The JSON data to decode.
     * @return The decoded object or null if blob is null.
     * @throws IOException If an object cannot be decoded.
     */
    protected V decode(String blob) throws IOException {
        if (blob == null) {
            return null;
        }

        V obj = JsonEncodedProvider.MAPPER.readValue(blob, mClazz);
        return obj;
    }
}
