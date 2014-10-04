/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.restlet.metrics;

import com.codahale.metrics.MetricRegistry;

import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;

/**
 * 
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class MetricsResource extends ServerResource {

    private MetricRegistry mMetricRegistry;

    @Override
    public void doInit() {
        mMetricRegistry = ((MetricsApplication) getApplication()).getMetricRegistry();
    }

    @Override
    protected Representation get() {
        JacksonRepresentation<MetricRegistry> rep = new JacksonRepresentation<>(mMetricRegistry);
        rep.setObjectMapper(MetricsApplication.MAPPER);
        return rep;
    }
}
