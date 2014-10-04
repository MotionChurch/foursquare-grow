/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.restlet.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.TemplateRoute;
import org.restlet.routing.Router;

/**
 * 
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class MetricRouter extends Router {

    private final MetricRegistry mMetricRegistry;

    public MetricRouter(Context context, MetricRegistry metrics) {
        super(context);
        mMetricRegistry = metrics;
    }

    @Override
    protected void doHandle(Restlet next, Request request, Response response) {
        String baseName;
        if (next instanceof TemplateRoute) {
            TemplateRoute temp = (TemplateRoute) next;
            baseName = MetricRegistry.name("MetricRouter", temp.getTemplate().getPattern());
        } else {
            baseName = MetricRegistry.name("MetricRouter", "unknown");
        }

        final Timer.Context aggTimer = mMetricRegistry.timer("MetricRouter.time").time();
        final Timer.Context timer = mMetricRegistry.timer(baseName + ".time").time();

        try {
            super.doHandle(next, request, response);
        } finally {
            timer.stop();
            aggTimer.stop();

            // Record status code
            boolean success = !response.getStatus().isError();
            if (success) {
                mMetricRegistry.counter("MetricRouter.success").inc();
                mMetricRegistry.counter(baseName + ".response.success").inc();
            } else {
                mMetricRegistry.counter("MetricRouter.failure").inc();
                mMetricRegistry.counter(baseName + ".response.failure").inc();
            }
        }
    }
}
