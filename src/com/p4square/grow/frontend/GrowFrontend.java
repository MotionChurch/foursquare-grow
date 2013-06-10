/*
 * Copyright 2013 Jesse Morgan <jesse@jesterpm.net>
 */

package com.p4square.grow.frontend;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

import org.apache.log4j.Logger;

import net.jesterpm.fmfacade.FMFacade;
import net.jesterpm.fmfacade.FreeMarkerPageResource;

import com.p4square.grow.config.Config;

/**
 * This is the Restlet Application implementing the Grow project front-end.
 * It's implemented as an extension of FMFacade that connects interactive pages
 * with various ServerResources. This class provides a main method to start a
 * Jetty instance for testing.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class GrowFrontend extends FMFacade {
    private static Logger cLog = Logger.getLogger(GrowFrontend.class);

    private Config mConfig;

    public GrowFrontend() {
        mConfig = new Config();
    }

    public Config getConfig() {
        return mConfig;
    }

    @Override
    public void start() throws Exception {
        super.start();

        final String configDomain =
            getContext().getParameters().getFirstValue("configDomain");
        if (configDomain != null) {
            mConfig.setDomain(configDomain);
        }

        mConfig.updateConfig(this.getClass().getResourceAsStream("/grow.properties"));

        final String configFilename =
            getContext().getParameters().getFirstValue("configFile");

        if (configFilename != null) {
            mConfig.updateConfig(configFilename);
        }
    }

    @Override
    protected Router createRouter() {
        Router router = new Router(getContext());

        final String loginPage = getConfig().getString("dynamicRoot", "") + "/login.html";

        final LoginAuthenticator defaultGuard =
            new LoginAuthenticator(getContext(), true, loginPage);
        defaultGuard.setNext(FreeMarkerPageResource.class);
        router.attachDefault(defaultGuard);
        router.attach("/login.html", LoginPageResource.class);

        final Router accountRouter = new Router(getContext());
        accountRouter.attach("/assessment/question/{questionId}", SurveyPageResource.class);
        accountRouter.attach("/assessment", SurveyPageResource.class);

        final LoginAuthenticator accountGuard =
            new LoginAuthenticator(getContext(), false, loginPage);
        accountGuard.setNext(accountRouter);
        router.attach("/account", accountGuard);

        return router;
    }

    /**
     * Stand-alone main for testing.
     */
    public static void main(String[] args) {
        // Start the HTTP Server
        final Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8085);
        component.getClients().add(Protocol.HTTP);

        // Setup App
        GrowFrontend app = new GrowFrontend();

        // Load an optional config file from the first argument.
        app.getConfig().setDomain("dev");
        if (args.length == 1) {
            app.getConfig().updateConfig(args[0]);
        }

        component.getDefaultHost().attach(app);

        // Setup shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    component.stop();
                } catch (Exception e) {
                    cLog.error("Exception during cleanup", e);
                }
            }
        });

        cLog.info("Starting server...");

        try {
            component.start();
        } catch (Exception e) {
            cLog.fatal("Could not start: " + e.getMessage(), e);
        }
    }
}
