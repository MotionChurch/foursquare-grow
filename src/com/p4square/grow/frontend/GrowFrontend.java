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

    /**
     * Stand-alone main for testing.
     */
    public static void main(String[] args) {
        // Start the HTTP Server
        final Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8085);
        component.getClients().add(Protocol.HTTP);
        component.getDefaultHost().attach(new GrowFrontend());

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