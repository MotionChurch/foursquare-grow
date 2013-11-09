/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.db;

/**
 * CassandraKey represents a Cassandra key / column pair.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class CassandraKey {
    private final String mColumnFamily;
    private final String mId;
    private final String mColumn;

    public CassandraKey(String columnFamily, String id, String column) {
        mColumnFamily = columnFamily;
        mId = id;
        mColumn = column;
    }

    public String getColumnFamily() {
        return mColumnFamily;
    }

    public String getId() {
        return mId;
    }

    public String getColumn() {
        return mColumn;
    }
}
