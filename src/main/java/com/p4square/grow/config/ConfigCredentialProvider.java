package com.p4square.grow.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

/**
 * AWSCredentials credentials backed by config.
 *
 * Falls back to DefaultAWSCredentialsProviderChain if the credentials are not in the config.
 */
public class ConfigCredentialProvider implements AWSCredentials {

    private AWSCredentials mCredentials;

    public ConfigCredentialProvider(final Config config) {
        String awsAccessKey = config.getString("awsAccessKey");
        if (awsAccessKey != null) {
            mCredentials = new AWSCredentials() {
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
            mCredentials = new DefaultAWSCredentialsProviderChain().getCredentials();
        }
    }

    @Override
    public String getAWSAccessKeyId() {
        return mCredentials.getAWSAccessKeyId();
    }

    @Override
    public String getAWSSecretKey() {
        return mCredentials.getAWSSecretKey();
    }
}
