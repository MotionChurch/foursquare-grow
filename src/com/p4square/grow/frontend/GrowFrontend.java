/*
 * Copyright 2013 Jesse Morgan <jesse@jesterpm.net>
 */

package com.p4square.grow.frontend;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.UUID;

import freemarker.template.Template;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.security.Authenticator;

import org.apache.log4j.Logger;

import com.p4square.fmfacade.FMFacade;
import com.p4square.fmfacade.FreeMarkerPageResource;

import com.p4square.grow.config.Config;

import com.p4square.f1oauth.F1OAuthHelper;
import com.p4square.f1oauth.SecondPartyVerifier;

import com.p4square.session.SessionCheckingAuthenticator;
import com.p4square.session.SessionCreatingAuthenticator;

/**
 * This is the Restlet Application implementing the Grow project front-end.
 * It's implemented as an extension of FMFacade that connects interactive pages
 * with various ServerResources. This class provides a main method to start a
 * Jetty instance for testing.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class GrowFrontend extends FMFacade {
    private static Logger LOG = Logger.getLogger(GrowFrontend.class);

    private Config mConfig;

    private F1OAuthHelper mHelper;

    public GrowFrontend() {
        this(new Config());
    }

    public GrowFrontend(Config config) {
        mConfig = config;
    }

    public Config getConfig() {
        return mConfig;
    }

    @Override
    public synchronized void start() throws Exception {
        Template errorTemplate = getTemplate("templates/error.ftl");
        if (errorTemplate != null) {
            ErrorPage.setTemplate(errorTemplate, FreeMarkerPageResource.baseRootObject(getContext()));
        }

        super.start();
    }

    synchronized F1OAuthHelper getHelper() {
        if (mHelper == null) {
            mHelper = new F1OAuthHelper(getContext(), mConfig.getString("f1ConsumerKey", ""),
                    mConfig.getString("f1ConsumerSecret", ""),
                    mConfig.getString("f1BaseUrl", "staging.fellowshiponeapi.com"),
                    mConfig.getString("f1ChurchCode", "pfseawa"),
                    F1OAuthHelper.UserType.WEBLINK);
        }

        return mHelper;
    }

    @Override
    protected Router createRouter() {
        Router router = new Router(getContext());

        final Authenticator defaultGuard = new SessionCheckingAuthenticator(getContext(), true);
        defaultGuard.setNext(FreeMarkerPageResource.class);
        router.attachDefault(defaultGuard);
        router.attach("/", new Redirector(getContext(), "index.html", Redirector.MODE_CLIENT_PERMANENT));
        router.attach("/login.html", LoginPageResource.class);
        router.attach("/newaccount.html", NewAccountResource.class);
        router.attach("/newbeliever", NewBelieverResource.class);

        final Router accountRouter = new Router(getContext());
        accountRouter.attach("/authenticate", AuthenticatedResource.class);
        accountRouter.attach("/logout", LogoutResource.class);

        accountRouter.attach("", AccountRedirectResource.class);
        accountRouter.attach("/assessment/question/{questionId}", SurveyPageResource.class);
        accountRouter.attach("/assessment/results", AssessmentResultsPage.class);
        accountRouter.attach("/assessment/reset", AssessmentResetPage.class);
        accountRouter.attach("/assessment", SurveyPageResource.class);
        accountRouter.attach("/training/{chapter}/completed", ChapterCompletePage.class);
        accountRouter.attach("/training/{chapter}/videos/{videoId}.json", VideosResource.class);
        accountRouter.attach("/training/{chapter}", TrainingPageResource.class);
        accountRouter.attach("/training", TrainingPageResource.class);
        accountRouter.attach("/feed/{topic}", FeedResource.class);
        accountRouter.attach("/feed/{topic}/{thread}", FeedResource.class);

        final Authenticator accountGuard = createAuthenticatorChain(accountRouter);
        router.attach("/account", accountGuard);

        return router;
    }

    private Authenticator createAuthenticatorChain(Restlet last) {
        final Context context = getContext();
        final String loginPage = getConfig().getString("dynamicRoot", "") + "/login.html";
        final String loginPost = getConfig().getString("dynamicRoot", "") + "/account/authenticate";
        final String defaultPage = getConfig().getString("dynamicRoot", "") + "/account";

        // This is used to check for an existing session
        SessionCheckingAuthenticator sessionChk = new SessionCheckingAuthenticator(context, true);

        // This is used to authenticate the user
        SecondPartyVerifier f1Verifier = new SecondPartyVerifier(context, getHelper());
        LoginFormAuthenticator loginAuth = new LoginFormAuthenticator(context, false, f1Verifier);
        loginAuth.setLoginFormUrl(loginPage);
        loginAuth.setLoginPostUrl(loginPost);
        loginAuth.setDefaultPage(defaultPage);

        // This is used to create a new session for a newly authenticated user.
        SessionCreatingAuthenticator sessionCreate = new SessionCreatingAuthenticator(context);

        sessionChk.setNext(loginAuth);
        loginAuth.setNext(sessionCreate);

        sessionCreate.setNext(last);

        return sessionChk;
    }

    /**
     * Stand-alone main for testing.
     */
    public static void main(String[] args) {
        // Start the HTTP Server
        final Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8085);
        component.getClients().add(Protocol.HTTP);
        component.getClients().add(Protocol.HTTPS);
        component.getClients().add(Protocol.FILE);
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
