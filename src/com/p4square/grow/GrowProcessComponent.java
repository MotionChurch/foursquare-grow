/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;

import org.apache.log4j.Logger;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.security.ChallengeAuthenticator;

import com.p4square.grow.backend.BackendVerifier;
import com.p4square.grow.backend.GrowBackend;
import com.p4square.grow.config.Config;
import com.p4square.grow.frontend.GrowFrontend;
import com.p4square.restlet.metrics.MetricsApplication;

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class GrowProcessComponent extends Component {
    private static Logger LOG = Logger.getLogger(GrowProcessComponent.class);

    private static final String BACKEND_REALM = "Grow Backend";

    private final Config mConfig;
    private final MetricRegistry mMetricRegistry;

    /**
     * Create a new Grow Process website component combining a frontend and backend.
     */
    public GrowProcessComponent() throws Exception {
        this(new Config());
    }

    public GrowProcessComponent(Config config) throws Exception {
        // Clients
        getClients().add(Protocol.FILE);
        getClients().add(Protocol.HTTP);
        getClients().add(Protocol.HTTPS);

        // Prepare mConfig
        mConfig = config;
        mConfig.updateConfig(this.getClass().getResourceAsStream("/grow.properties"));

        // Prepare Metrics
        mMetricRegistry = new MetricRegistry();

        // Frontend
        GrowFrontend frontend = new GrowFrontend(mConfig, mMetricRegistry);
        getDefaultHost().attach(frontend);

        // Backend
        GrowBackend backend = new GrowBackend(mConfig, mMetricRegistry);
        getInternalRouter().attach("/backend", backend);

        // Authenticated access to the backend
        BackendVerifier verifier = new BackendVerifier(backend.getUserRecordProvider());
        ChallengeAuthenticator auth = new ChallengeAuthenticator(getContext().createChildContext(),
                false, ChallengeScheme.HTTP_BASIC, BACKEND_REALM, verifier);
        auth.setNext(backend);
        getDefaultHost().attach("/backend", auth);

        // Authenticated access to metrics
        ChallengeAuthenticator metricAuth = new ChallengeAuthenticator(
                getContext().createChildContext(), false,
                ChallengeScheme.HTTP_BASIC, BACKEND_REALM, verifier);
        metricAuth.setNext(new MetricsApplication(mMetricRegistry));
        getDefaultHost().attach("/metrics", metricAuth);
    }


    @Override
    public void start() throws Exception {
        String configDomain = getContext().getParameters().getFirstValue("com.p4square.grow.configDomain");
        if (configDomain != null) {
            mConfig.setDomain(configDomain);
        }

        String configFilename = getContext().getParameters().getFirstValue("com.p4square.grow.configFile");
        if (configFilename != null) {
            mConfig.updateConfig(configFilename);
        }

        super.start();
    }

    /**
     * Stand-alone main for testing.
     */
    public static void main(String[] args) throws Exception {
        // Load an optional config file from the first argument.
        Config config = new Config();
        config.setDomain("dev");
        if (args.length >= 1) {
            config.updateConfig(args[0]);
        }

        // Override domain
        if (args.length == 2) {
            config.setDomain(args[1]);
        }

        // Start the HTTP Server
        final GrowProcessComponent component = new GrowProcessComponent(config);
        component.getServers().add(Protocol.HTTP, 8085);

        // Static content
        try {
            component.getDefaultHost().attach("/images/", new FileServingApp("./build/root/images/"));
            component.getDefaultHost().attach("/scripts", new FileServingApp("./build/root/scripts"));
            component.getDefaultHost().attach("/style.css", new FileServingApp("./build/root/style.css"));
            component.getDefaultHost().attach("/favicon.ico", new FileServingApp("./build/root/favicon.ico"));
            component.getDefaultHost().attach("/notfound.html", new FileServingApp("./build/root/notfound.html"));
            component.getDefaultHost().attach("/error.html", new FileServingApp("./build/root/error.html"));
        } catch (IOException e) {
            LOG.error("Could not create directory for static resources: "
                    + e.getMessage(), e);
        }

        // Setup shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    component.stop();
                } catch (Exception e) {
                    LOG.error("Exception during cleanup", e);
                }
            }
        });

        LOG.info("Starting server...");

        try {
            component.start();
        } catch (Exception e) {
            LOG.fatal("Could not start: " + e.getMessage(), e);
        }
    }

    private static class FileServingApp extends Application {
        private final String mPath;

        public FileServingApp(String path) throws IOException {
            mPath = new File(path).getAbsolutePath();
        }

        @Override
        public Restlet createInboundRoot() {
            return new Directory(getContext(), "file://" + mPath);
        }
    }
}
