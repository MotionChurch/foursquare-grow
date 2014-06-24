/*
 * Copyright 2013 Jesse Morgan <jesse@jesterpm.net>
 */

package net.jesterpm.fmfacade;

import java.io.IOException;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

import org.apache.log4j.Logger;

import com.p4square.grow.config.Config;

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class FMFacade extends Application {
    private static final Logger cLog = Logger.getLogger(FMFacade.class);
    private final Configuration mFMConfig;

    public FMFacade() {
        mFMConfig = new Configuration();
        mFMConfig.setClassForTemplateLoading(getClass(), "/templates");
        mFMConfig.setObjectWrapper(new DefaultObjectWrapper());
    }

    /**
     * @return a Config object.
     */
    public Config getConfig() {
        return null;
    }

    @Override
    public synchronized Restlet createInboundRoot() {
        return createRouter();
    }

    /**
     * Retrieve a template.
     *
     * @param name The template name.
     * @return A FreeMarker template or null on error.
     */
    public Template getTemplate(String name) {
        try {
            return mFMConfig.getTemplate(name);

        } catch (IOException e) {
            cLog.error("Could not load template \"" + name + "\"", e);
            return null;
        }
    }

    /**
     * Create the router to be used by this application. This can be overriden
     * by sub-classes to add additional routes.
     *
     * @return The router.
     */
    protected Router createRouter() {
        Router router = new Router(getContext());
        router.attachDefault(FreeMarkerPageResource.class);

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
        component.getDefaultHost().attach(new FMFacade());

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
