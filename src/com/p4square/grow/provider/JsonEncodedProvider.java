/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.provider;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Provider provides a simple interface for loading and persisting
 * objects.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public abstract class JsonEncodedProvider<K, V> implements Provider<K, V> {
    public static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        MAPPER.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private final Class<V> mClazz;
    private final JavaType mType;

    public JsonEncodedProvider(Class<V> clazz) {
        mClazz = clazz;
        mType = null;
    }

    public JsonEncodedProvider(JavaType type) {
        mType = type;
        mClazz = null;
    }

    /**
     * Encode the object as JSON.
     *
     * @param obj The object to encode.
     * @return The JSON encoding of obj.
     * @throws IOException if the object cannot be encoded.
     */
    protected String encode(V obj) throws IOException {
        return MAPPER.writeValueAsString(obj);
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

        V obj;
        if (mClazz != null) {
            obj = MAPPER.readValue(blob, mClazz);

        } else {
            obj = MAPPER.readValue(blob, mType);
        }

        return obj;
    }
}

