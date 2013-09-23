/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow;

import org.restlet.Component;
import org.restlet.data.Protocol;

import com.p4square.grow.backend.GrowBackend;
import com.p4square.grow.config.Config;
import com.p4square.grow.frontend.GrowFrontend;

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class GrowProcessComponent extends Component {
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
}
