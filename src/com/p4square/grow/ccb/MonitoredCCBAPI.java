package com.p4square.grow.ccb;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.p4square.ccbapi.CCBAPI;
import com.p4square.ccbapi.model.GetCustomFieldLabelsResponse;
import com.p4square.ccbapi.model.GetIndividualProfilesRequest;
import com.p4square.ccbapi.model.GetIndividualProfilesResponse;

import java.io.IOException;

/**
 * MonitoredCCBAPI is a CCBAPI decorator which records metrics for each API call.
 */
public class MonitoredCCBAPI implements CCBAPI {

    private final CCBAPI mAPI;
    private final MetricRegistry mMetricRegistry;

    public MonitoredCCBAPI(final CCBAPI api, final MetricRegistry metricRegistry) {
        if (api == null) {
            throw new IllegalArgumentException("api must not be null.");
        }
        mAPI = api;

        if (metricRegistry == null) {
            throw new IllegalArgumentException("metricRegistry must not be null.");
        }
        mMetricRegistry = metricRegistry;
    }

    @Override
    public GetCustomFieldLabelsResponse getCustomFieldLabels() throws IOException {
        final Timer.Context timer = mMetricRegistry.timer("CCBAPI.getCustomFieldLabels.time").time();
        boolean success = false;
        try {
            final GetCustomFieldLabelsResponse resp = mAPI.getCustomFieldLabels();
            success = true;
            return resp;
        } finally {
            timer.stop();
            mMetricRegistry.counter("CCBAPI.getCustomFieldLabels.success").inc(success ? 1 : 0);
            mMetricRegistry.counter("CCBAPI.getCustomFieldLabels.failure").inc(!success ? 1 : 0);
        }
    }

    @Override
    public GetIndividualProfilesResponse getIndividualProfiles(GetIndividualProfilesRequest request)
            throws IOException {
        final Timer.Context timer = mMetricRegistry.timer("CCBAPI.getIndividualProfiles").time();
        boolean success = false;
        try {
            final GetIndividualProfilesResponse resp = mAPI.getIndividualProfiles(request);
            mMetricRegistry.counter("CCBAPI.getCustomFieldLabels.count").inc(resp.getIndividuals().size());
            success = true;
            return resp;
        } finally {
            timer.stop();
            mMetricRegistry.counter("CCBAPI.getCustomFieldLabels.success").inc(success ? 1 : 0);
            mMetricRegistry.counter("CCBAPI.getCustomFieldLabels.failure").inc(!success ? 1 : 0);
        }
    }

    @Override
    public void close() throws IOException {
        mAPI.close();
    }
}
