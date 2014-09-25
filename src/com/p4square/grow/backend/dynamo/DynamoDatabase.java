/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.backend.dynamo;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableResult;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.amazonaws.services.dynamodbv2.model.UpdateTableRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateTableResult;

import com.p4square.grow.config.Config;

/**
 * A wrapper around the Dynamo API.
 */
public class DynamoDatabase {
    private final AmazonDynamoDBClient mClient;
    private final String mTablePrefix;

    public DynamoDatabase(final Config config) {
        AWSCredentials creds;

        String awsAccessKey = config.getString("awsAccessKey");
        if (awsAccessKey != null) {
            creds = new AWSCredentials() {
                @Override
                public String getAWSAccessKeyId() {
                    return config.getString("awsAccessKey");
                }
                @Override
                public String getAWSSecretKey() {
                    return config.getString("awsSecretKey");
                }
            };
        } else {
            creds = new DefaultAWSCredentialsProviderChain().getCredentials();
        }

        mClient = new AmazonDynamoDBClient(creds);

        String endpoint = config.getString("dynamoEndpoint");
        if (endpoint != null) {
            mClient.setEndpoint(endpoint);
        }

        String region = config.getString("awsRegion");
        if (region != null) {
            mClient.setRegion(Region.getRegion(Regions.fromName(region)));
        }

        mTablePrefix = config.getString("dynamoTablePrefix", "");
    }

    public void createTable(String name, long reads, long writes) {
        ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(new AttributeDefinition()
                .withAttributeName("id")
                .withAttributeType("S"));

        ArrayList<KeySchemaElement> ks = new ArrayList<>();
        ks.add(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH));

        ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput()
            .withReadCapacityUnits(reads)
            .withWriteCapacityUnits(writes);

        CreateTableRequest request = new CreateTableRequest()
            .withTableName(mTablePrefix + name)
            .withAttributeDefinitions(attributeDefinitions)
            .withKeySchema(ks)
            .withProvisionedThroughput(provisionedThroughput);

        CreateTableResult result = mClient.createTable(request);
    }

    public void updateTable(String name, long reads, long writes) {
        ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput()
            .withReadCapacityUnits(reads)
            .withWriteCapacityUnits(writes);

        UpdateTableRequest request = new UpdateTableRequest()
            .withTableName(mTablePrefix + name)
            .withProvisionedThroughput(provisionedThroughput);

        UpdateTableResult result = mClient.updateTable(request);
    }

    public void deleteTable(String name) {
        DeleteTableRequest deleteTableRequest = new DeleteTableRequest()
            .withTableName(mTablePrefix + name);

        DeleteTableResult result = mClient.deleteTable(deleteTableRequest);
    }

    /**
     * Get all rows from a table.
     *
     * The key parameter must specify a table. If hash/range key is specified,
     * the scan will begin after that key.
     *
     * @param key Previous key to start with.
     * @return An ordered map of all results.
     */
    public Map<DynamoKey, Map<String, String>> getAll(final DynamoKey key) {
        ScanRequest scanRequest = new ScanRequest().withTableName(mTablePrefix + key.getTable());

        if (key.getHashKey() != null) {
            scanRequest.setExclusiveStartKey(generateKey(key));
        }

        ScanResult scanResult = mClient.scan(scanRequest);

        Map<DynamoKey, Map<String, String>> result = new LinkedHashMap<>();
        for (Map<String, AttributeValue> map : scanResult.getItems()) {
            String id = null;
            String range = null;
            Map<String, String> row = new LinkedHashMap<>();
            for (Map.Entry<String, AttributeValue> entry : map.entrySet()) {
                if ("id".equals(entry.getKey())) {
                    id = entry.getValue().getS();
                } else if ("range".equals(entry.getKey())) {
                    range = entry.getValue().getS();
                } else {
                    row.put(entry.getKey(), entry.getValue().getS());
                }
            }
            result.put(DynamoKey.newRangeKey(key.getTable(), id, range), row);
        }

        return result;
    }

    public Map<String, String> getKey(final DynamoKey key) {
        GetItemRequest getItemRequest = new GetItemRequest()
            .withTableName(mTablePrefix + key.getTable())
            .withKey(generateKey(key));

        GetItemResult getItemResult = mClient.getItem(getItemRequest);
        Map<String, AttributeValue> map = getItemResult.getItem();

        Map<String, String> result = new LinkedHashMap<>();
        if (map != null) {
            for (Map.Entry<String, AttributeValue> entry : map.entrySet()) {
                if (!"id".equals(entry.getKey())) {
                    result.put(entry.getKey(), entry.getValue().getS());
                }
            }
        }

        return result;
    }

    public String getAttribute(final DynamoKey key) {
        checkAttributeKey(key);

        GetItemRequest getItemRequest = new GetItemRequest()
            .withTableName(mTablePrefix + key.getTable())
            .withKey(generateKey(key))
            .withAttributesToGet(key.getAttribute());

        GetItemResult result = mClient.getItem(getItemRequest);
        Map<String, AttributeValue> map = result.getItem();

        if (map == null) {
            return null;
        }

        AttributeValue value = map.get(key.getAttribute());
        if (value != null) {
            return value.getS();

        } else {
            return null;
        }
    }

    /**
     * Set all attributes for the given key.
     *
     * @param key The key.
     * @param values Map of attributes to values.
     */
    public void putKey(final DynamoKey key, final Map<String, String> values) {
        Map<String, AttributeValue> item = new HashMap<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            item.put(entry.getKey(), new AttributeValue().withS(entry.getValue()));
        }

        // Set the Key
        item.putAll(generateKey(key));

        PutItemRequest putItemRequest = new PutItemRequest()
            .withTableName(mTablePrefix + key.getTable())
            .withItem(item);

        PutItemResult result = mClient.putItem(putItemRequest);
    }

    /**
     * Set the particular attributes of the given key.
     *
     * @param key The key.
     * @param value The new value.
     */
    public void putAttribute(final DynamoKey key, final String value) {
        checkAttributeKey(key);

        Map<String, AttributeValueUpdate> updateItem = new HashMap<>();
        updateItem.put(key.getAttribute(),
                new AttributeValueUpdate()
                .withAction(AttributeAction.PUT)
                .withValue(new AttributeValue().withS(value)));

        UpdateItemRequest updateItemRequest = new UpdateItemRequest()
            .withTableName(mTablePrefix + key.getTable())
            .withKey(generateKey(key))
            .withAttributeUpdates(updateItem);
        // TODO: Check conditions.

        UpdateItemResult result = mClient.updateItem(updateItemRequest);
    }

    /**
     * Delete the given key.
     *
     * @param key The key.
     */
    public void deleteKey(final DynamoKey key) {
        DeleteItemRequest deleteItemRequest = new DeleteItemRequest()
            .withTableName(mTablePrefix + key.getTable())
            .withKey(generateKey(key));

        DeleteItemResult result = mClient.deleteItem(deleteItemRequest);
    }

    /**
     * Delete an attribute from the given key.
     *
     * @param key The key.
     */
    public void deleteAttribute(final DynamoKey key) {
        checkAttributeKey(key);

        Map<String, AttributeValueUpdate> updateItem = new HashMap<>();
        updateItem.put(key.getAttribute(),
                new AttributeValueUpdate().withAction(AttributeAction.DELETE));

        UpdateItemRequest updateItemRequest = new UpdateItemRequest()
            .withTableName(mTablePrefix + key.getTable())
            .withKey(generateKey(key))
            .withAttributeUpdates(updateItem);

        UpdateItemResult result = mClient.updateItem(updateItemRequest);
    }

    /**
     * Generate a DynamoDB Key Map from the DynamoKey.
     */
    private Map<String, AttributeValue> generateKey(final DynamoKey key) {
        HashMap<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put("id", new AttributeValue().withS(key.getHashKey()));

        String range = key.getRangeKey();
        if (range != null) {
            keyMap.put("range", new AttributeValue().withS(range));
        }

        return keyMap;
    }

    private void checkAttributeKey(DynamoKey key) {
        if (null == key.getAttribute()) {
            throw new IllegalArgumentException("Attribute must be non-null");
        }
    }
}
