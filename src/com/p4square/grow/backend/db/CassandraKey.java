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
    private final String mId;
    private final String mColumn;

    public CassandraKey(String id, String column) {
        mId = id;
        mColumn = column;
    }

    public String getId() {
        return mId;
    }

    public String getColumn() {
        return mColumn;
    }
}
