/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.restlet.metrics;

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.resource.Finder;

/**
 * 
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class MetricsApplication extends Application {
    static final ObjectMapper MAPPER;
    static {
       MAPPER = new ObjectMapper();
       MAPPER.registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS, true));
    }

    private final MetricRegistry mMetricRegistry;

    public MetricsApplication(MetricRegistry metrics) {
        mMetricRegistry = metrics;
    }

    public MetricRegistry getMetricRegistry() {
        return mMetricRegistry;
    }

    @Override
    public Restlet createInboundRoot() {
        return new Finder(getContext(), MetricsResource.class);
    }
}
