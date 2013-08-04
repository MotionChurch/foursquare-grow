/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.db;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.ColumnMutation;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

import org.apache.log4j.Logger;

/**
 * Cassandra Database Abstraction for the Backend.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class CassandraDatabase {
    private static Logger cLog = Logger.getLogger(CassandraDatabase.class);

    // Configuration fields.
    private String mClusterName;
    private String mKeyspaceName;
    private String mSeedEndpoint   = "127.0.0.1:9160";
    private int    mPort           = 9160;

    private AstyanaxContext<Keyspace>  mContext;
    private Keyspace mKeyspace;

    /**
     * Connect to Cassandra.
     *
     * Cluster and Keyspace must be set before calling init().
     */
    public void init() {
        mContext = new AstyanaxContext.Builder()
            .forCluster(mClusterName)
            .forKeyspace(mKeyspaceName)
            .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()
                .setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE)
            )
            .withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl("MyConnectionPool")
                .setPort(mPort)
                .setMaxConnsPerHost(1)
                .setSeeds(mSeedEndpoint)
            )
            .withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
            .buildKeyspace(ThriftFamilyFactory.getInstance());

        mContext.start();
        mKeyspace = mContext.getClient();
    }

    /**
     * Close the database connection.
     */
    public void close() {
        mContext.shutdown();
    }

    /**
     * Set the cluster name to connect to.
     */
    public void setClusterName(final String cluster) {
        mClusterName = cluster;
    }

    /**
     * Set the name of the keyspace to open.
     */
    public void setKeyspaceName(final String keyspace) {
        mKeyspaceName = keyspace;
    }

    /**
     * Change the seed endpoint.
     * The default is 127.0.0.1:9160.
     */
    public void setSeedEndpoint(final String endpoint) {
        mSeedEndpoint = endpoint;
    }

    /**
     * Change the port to connect to.
     * The default is 9160.
     */
    public void setPort(final int port) {
        mPort = port;
    }

    /**
     * @return The entire row associated with this key.
     */
    public ColumnList<String> getRow(final String cfName, final String key) {
        try {
            ColumnFamily<String, String> cf = new ColumnFamily(cfName,
                StringSerializer.get(),
                StringSerializer.get());

            OperationResult<ColumnList<String>> result =
                mKeyspace.prepareQuery(cf)
                    .getKey(key)
                    .execute();

            return result.getResult();

        } catch (ConnectionException e) {
            cLog.error("getRow failed due to Connection Exception", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The value associated with the given key.
     */
    public String getKey(final String cfName, final String key) {
        return getKey(cfName, key, "value");
    }

    /**
     * @return The value associated with the given key, column pair.
     */
    public String getKey(final String cfName, final String key, final String column) {
        final ColumnList<String> row = getRow(cfName, key);

        if (row != null) {
            final Column rowColumn = row.getColumnByName(column);
            if (rowColumn != null) {
                return rowColumn.getStringValue();
            }
        }

        return null;
    }

    /**
     * Assign value to key.
     */
    public void putKey(final String cfName, final String key, final String value) {
        putKey(cfName, key, "value", value);
    }

    /**
     * Assign value to the key, column pair.
     */
    public void putKey(final String cfName, final String key,
            final String column, final String value) {

        ColumnFamily<String, String> cf = new ColumnFamily(cfName,
            StringSerializer.get(),
            StringSerializer.get());

        MutationBatch m = mKeyspace.prepareMutationBatch();
        m.withRow(cf, key).putColumn(column, value);

        try {
            m.execute();
        } catch (ConnectionException e) {
            cLog.error("putKey failed due to Connection Exception", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Remove a key, column pair.
     */
    public void deleteKey(final String cfName, final String key, final String column) {
        ColumnFamily<String, String> cf = new ColumnFamily(cfName,
            StringSerializer.get(),
            StringSerializer.get());

        try {
            ColumnMutation m = mKeyspace.prepareColumnMutation(cf, key, column);
            m.deleteColumn().execute();
        } catch (ConnectionException e) {
            cLog.error("deleteKey failed due to Connection Exception", e);
            throw new RuntimeException(e);
        }
    }
}
