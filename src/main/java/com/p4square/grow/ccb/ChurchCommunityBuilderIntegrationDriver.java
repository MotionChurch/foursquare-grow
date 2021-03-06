package com.p4square.grow.ccb;

import com.codahale.metrics.MetricRegistry;
import com.p4square.ccbapi.CCBAPI;
import com.p4square.ccbapi.CCBAPIClient;
import com.p4square.grow.config.Config;
import com.p4square.grow.frontend.IntegrationDriver;
import com.p4square.grow.frontend.ProgressReporter;
import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.security.Verifier;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The ChurchCommunityBuilderIntegrationDriver is used to integrate Grow with Church Community Builder.
 */
public class ChurchCommunityBuilderIntegrationDriver implements IntegrationDriver {

    private static final Logger LOG = Logger.getLogger(ChurchCommunityBuilderIntegrationDriver.class);

    private final Context mContext;
    private final MetricRegistry mMetricRegistry;
    private final Config mConfig;

    private final CCBAPI mAPI;

    private final CCBProgressReporter mProgressReporter;

    public ChurchCommunityBuilderIntegrationDriver(final Context context) {
        mContext = context;
        mConfig = (Config) context.getAttributes().get("com.p4square.grow.config");
        mMetricRegistry = (MetricRegistry) context.getAttributes().get("com.p4square.grow.metrics");

        try {
            CCBAPI api = new CCBAPIClient(new URI(mConfig.getString("CCBAPIURL", "")),
                                          mConfig.getString("CCBAPIUser", ""),
                                          mConfig.getString("CCBAPIPassword", ""));

            if (mMetricRegistry != null) {
                api = new MonitoredCCBAPI(api, mMetricRegistry);
            }

            mAPI = api;

            final CustomFieldCache cache = new CustomFieldCache(mAPI);
            mProgressReporter = new CCBProgressReporter(mAPI, cache);

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return A CCB API client.
     */
    public CCBAPI getAPI() {
        return mAPI;
    }

    @Override
    public Verifier newUserAuthenticationVerifier() {
        return new CCBUserVerifier(mAPI);
    }

    @Override
    public ProgressReporter getProgressReporter() {
        return mProgressReporter;
    }

    @Override
    public boolean doHealthCheck() {
        try {
            // Try something benign, like getting the custom field labels,
            // to verify that we can talk to CCB.
            mAPI.getCustomFieldLabels();
            return true;

        } catch (IOException e) {
            LOG.warn("CCB Health Check Failed: " + e.getMessage(), e);
            return false;
        }
    }
}
