/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.backend.dynamo;

/**
 * DynamoKey represents a table, hash key, and range key tupl.
 */
public class DynamoKey {
    private final String mTable;
    private final String mHashKey;
    private final String mRangeKey;
    private final String mAttribute;

    public static DynamoKey newKey(final String table, final String hashKey) {
        return new DynamoKey(table, hashKey, null, null);
    }

    public static DynamoKey newRangeKey(final String table, final String hashKey,
            final String rangeKey) {

        return new DynamoKey(table, hashKey, rangeKey, null);
    }

    public static DynamoKey newAttributeKey(final String table, final String hashKey,
            final String attribute) {

        return new DynamoKey(table, hashKey, null, attribute);
    }

    public DynamoKey(final String table, final String hashKey, final String rangeKey,
            final String attribute) {

        mTable = table;
        mHashKey = hashKey;
        mRangeKey = rangeKey;
        mAttribute = attribute;
    }

    public String getTable() {
        return mTable;
    }

    public String getHashKey() {
        return mHashKey;
    }

    public String getRangeKey() {
        return mRangeKey;
    }

    public String getAttribute() {
        return mAttribute;
    }
}
