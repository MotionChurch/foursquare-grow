/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Manage configuration for an application.
 *
 * Config reads one or more property files as the application config. Duplicate
 * properties loaded later override properties loaded earlier. Config has the
 * concept of a domain to distinguish settings for development and production.
 * The default domain is prod for production. Domain can be any String such as
 * dev for development or test for testing.
 *
 * The property files are processed like java.util.Properties except that the
 * keys are specified as DOMAIN.KEY. An asterisk (*) can be used in place of a
 * domain to indicate it should apply to all domains. If a domain specific entry
 * exists for the current domain, it will override any global config.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class Config {
    private static final Logger LOG = Logger.getLogger(Config.class);

    private String mDomain;
    private Properties mProperties;

    /**
     * Construct a new Config object. Domain defaults to prod.
     */
    public Config() {
        mDomain = "prod";
        mProperties = new Properties();

    }

    /**
     * Change the domain from the default string "prod".
     *
     * @param domain The new domain.
     */
    public void setDomain(String domain) {
        LOG.info("Setting Config domain to " + domain);
        mDomain = domain;
    }

    /**
     * Load properties from a file.
     * Any exceptions are logged and suppressed.
     */
    public void updateConfig(String propertyFilename) {
        final File propFile = new File(propertyFilename);

        LOG.info("Loading properties from " + propFile);

        try {
            InputStream in = new FileInputStream(propFile);
            updateConfig(in);

        } catch (IOException e) {
            LOG.error("Could not load properties file: " + e.getMessage(), e);
        }
    }

    /**
     * Load properties from an InputStream.
     * This method closes the InputStream when it completes.
     *
     * @param in The InputStream
     */
    public void updateConfig(InputStream in) throws IOException {
        LOG.info("Loading properties from InputStream");
        mProperties.load(in);
        in.close();
    }

    /**
     * Get a String from the config.
     *
     * @return The config value or null if it is not found.
     */
    public String getString(String key) {
        return getString(key, null);
    }

    /**
     * Get a String from the config.
     *
     * @return The config value or defaultValue if it can not be found.
     */
    public String getString(final String key, final String defaultValue) {
        String result;

        final String domainKey = mDomain + "." + key;
        result = mProperties.getProperty(domainKey);
        if (result != null) {
            LOG.debug("Reading config for key = { " + key + " }. Got result = { " + result + " }");
            return result;
        }

        final String globalKey = "*." + key;
        result = mProperties.getProperty(globalKey);
        if (result != null) {
            LOG.debug("Reading config for key = { " + key + " }. Got result = { " + result + " }");
            return result;
        }

        LOG.debug("Reading config for key = { " + key + " }. Got default value = { " + defaultValue + " }");
        return defaultValue;
    }

    /**
     * Get an integer from the config.
     *
     * @return The config value or Integer.MIN_VALUE if the key is not present or the
     *         config can not be parsed.
     */
    public int getInt(String key) {
        return getInt(key, Integer.MIN_VALUE);
    }

    /**
     * Get an integer from the config.
     *
     * @return The config value or defaultValue if the key is not present or the
     *         config can not be parsed.
     */
    public int getInt(String key, int defaultValue) {
        final String propertyValue = getString(key);

        if (propertyValue != null) {
            try {
                final int result = Integer.valueOf(propertyValue);
                return result;

            } catch (NumberFormatException e) {
                LOG.warn("Expected property to be an integer: "
                        + key + " = { " + propertyValue + " }");
            }
        }

        return defaultValue;
    }
}
