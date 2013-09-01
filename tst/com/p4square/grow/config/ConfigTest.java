/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.config;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class ConfigTest {
    public static void main(String... args) {
        org.junit.runner.JUnitCore.main(ConfigTest.class.getName());
    }

    @Test
    public void basicTest() throws Exception {
        // Load a config file
        Config domain1 = new Config();
        domain1.setDomain("domain1");
        Config domain2 = new Config();
        domain2.setDomain("domain2");

        domain1.updateConfig(getClass().getResourceAsStream("ConfigTest.properties"));
        domain2.updateConfig(getClass().getResourceAsStream("ConfigTest.properties"));

        // Non-existent key returns default
        assertEquals("default", domain1.getString("doesNotExist", "default"));
        assertSame(null, domain1.getString("doesNotExist"));

        // Domain keys return different values for different domains
        assertEquals("domain1Value", domain1.getString("domainSpecific"));
        assertEquals("domain2Value", domain2.getString("domainSpecific"));

        // Domain key takes priority over *. key
        assertEquals("domain1Value", domain1.getString("onlyInDomain1"));
        assertEquals("wildValue", domain2.getString("onlyInDomain1"));

        // Wildcard domain returns value
        assertEquals("wildValue", domain1.getString("wildcardOnly"));

        // Empty value gives empty string
        assertEquals("", domain1.getString("emptyValue"));

        // Number is returned
        assertEquals(5, domain1.getInt("number"));

        // Non number test
        assertEquals(Integer.MIN_VALUE, domain1.getInt("notANumber"));
    }
}
