/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.backend.dynamo;

import java.io.IOException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.p4square.grow.provider.CollectionProvider;
import com.p4square.grow.provider.JsonEncodedProvider;

/**
 * 
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class DynamoCollectionProviderImpl<V> implements CollectionProvider<String, String, V> {
    private final DynamoDatabase mDb;
    private final String mTable;
    private final Class<V> mClazz;

    public DynamoCollectionProviderImpl(DynamoDatabase db, String table, Class<V> clazz) {
        mDb = db;
        mTable = table;
        mClazz = clazz;
    }

    @Override
    public V get(String collection, String key) throws IOException {
        String blob = mDb.getAttribute(DynamoKey.newAttributeKey(mTable, collection, key));
        return decode(blob);
    }

    @Override
    public Map<String, V> query(String collection) throws IOException {
        return query(collection, -1);
    }

    @Override
    public Map<String, V> query(String collection, int limit) throws IOException {
        Map<String, V> result = new LinkedHashMap<>();

        Map<String, String> row = mDb.getKey(DynamoKey.newKey(mTable, collection));
        if (row.size() > 0) {
            int count = 0;
            for (Map.Entry<String, String> c : row.entrySet()) {
                if (limit >= 0 && ++count > limit) {
                    break; // Limit reached.
                }

                String key = c.getKey();
                String blob = c.getValue();
                V obj = decode(blob);

                result.put(key, obj);
            }
        }

        return Collections.unmodifiableMap(result);
    }

    @Override
    public void put(String collection, String key, V obj) throws IOException {
        if (obj == null) {
            mDb.deleteAttribute(DynamoKey.newAttributeKey(mTable, collection, key));
        } else {
            String blob = encode(obj);
            mDb.putAttribute(DynamoKey.newAttributeKey(mTable, collection, key), blob);
        }
    }

    /**
     * Encode the object as JSON.
     *
     * @param obj The object to encode.
     * @return The JSON encoding of obj.
     * @throws IOException if the object cannot be encoded.
     */
    protected String encode(V obj) throws IOException {
        if (mClazz == String.class) {
            return (String) obj;
        } else {
            return JsonEncodedProvider.MAPPER.writeValueAsString(obj);
        }
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

        if (mClazz == String.class) {
            return (V) blob;
        }

        V obj = JsonEncodedProvider.MAPPER.readValue(blob, mClazz);
        return obj;
    }
}
