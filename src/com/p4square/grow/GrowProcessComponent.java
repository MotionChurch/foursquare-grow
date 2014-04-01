/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import org.restlet.Application;
import org.restlet.Client;
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

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class GrowProcessComponent extends Component {
    private static Logger LOG = Logger.getLogger(GrowProcessComponent.class);

    private static final String BACKEND_REALM = "Grow Backend";

    private final Config mConfig;

    /**
     * Create a new Grow Process website component combining a frontend and backend.
     */
    public GrowProcessComponent() throws Exception {
        // Clients
        getClients().add(Protocol.FILE);
        getClients().add(Protocol.HTTP);
        getClients().add(Protocol.HTTPS);

        // Prepare mConfig
        mConfig = new Config();

        // Frontend
        GrowFrontend frontend = new GrowFrontend(mConfig);
        getDefaultHost().attach(frontend);

        // Backend
        GrowBackend backend = new GrowBackend(mConfig);
        getInternalRouter().attach("/backend", backend);

        // Authenticated access to the backend
        BackendVerifier verifier = new BackendVerifier(backend.getUserRecordProvider());
        ChallengeAuthenticator auth = new ChallengeAuthenticator(getContext().createChildContext(),
                false, ChallengeScheme.HTTP_BASIC, BACKEND_REALM, verifier);
        auth.setNext(backend);
        getDefaultHost().attach("/backend", auth);
    }

    @Override
    public void start() throws Exception {
        // Load mConfigs
        mConfig.updateConfig(this.getClass().getResourceAsStream("/grow.properties"));

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
        // Start the HTTP Server
        final GrowProcessComponent component = new GrowProcessComponent();
        component.getServers().add(Protocol.HTTP, 8085);
        //component.getClients().add(new Client(null, Arrays.asList(Protocol.HTTPS), "org.restlet.ext.httpclient.HttpClientHelper"));

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

        // Load an optional config file from the first argument.
        component.mConfig.setDomain("dev");
        if (args.length == 1) {
            component.mConfig.updateConfig(args[0]);
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
