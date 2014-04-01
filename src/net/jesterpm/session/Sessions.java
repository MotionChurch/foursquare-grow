/*
 * Copyright 2013 Jesse Morgan
 */

package net.jesterpm.session;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.restlet.Response;
import org.restlet.Request;
import org.restlet.data.CookieSetting;
import org.restlet.security.User;

/**
 * Singleton Session Manager.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class Sessions {
    private static final String COOKIE_NAME  = "S";
    private static final int DELETE  = 0;

    private static final Sessions THE = new Sessions();
    public static Sessions getInstance() {
        return THE;
    }

    private final Map<String, Session> mSessions;
    private final Timer mCleanupTimer;

    private Sessions() {
        mSessions = new ConcurrentHashMap<String, Session>();

        mCleanupTimer = new Timer("sessionCleaner", true);
        mCleanupTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Session s : mSessions.values()) {
                    if (s.isExpired()) {
                        mSessions.remove(s.getId());
                    }
                }
            }
        }, Session.LIFETIME, Session.LIFETIME);
    }

    /**
     * Get a session by ID.
     *
     * @param sessionid
     *                  The Session id
     * @return The Session if found and not expired, null otherwise.
     */
    public Session get(String sessionid) {
        Session s = mSessions.get(sessionid);

        if (s != null && !s.isExpired()) {
            s.touch();
            return s;
        }

        return null;
    }

    /**
     * Get the Session associated with the Request.
     *
     * @param request
     *                  The request to fetch a session for.
     * @return A session or null if no session is found.
     */
    public Session get(Request request) {
        final String cookie = request.getCookies().getFirstValue(COOKIE_NAME);

        if (cookie != null) {
            return get(cookie);
        }

        return null;
    }

    /**
     * Create a new Session for the given User object.
     *
     * @param user
     *              The User to associate with the Session.
     * @return The new Session object.
     */
    public Session create(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Can not create session for null user.");
        }

        Session s = new Session(user);
        mSessions.put(s.getId(), s);

        return s;
    }

    /**
     * Delete a Session.
     *
     * @param sessionid
     *              The id of the Session to remove.
     */
    public void delete(String sessionid) {
        mSessions.remove(sessionid);
    }

    /**
     * Create a new Session and add the Session cookie to the response.
     *
     * @param request
     *              The request to create the Session for.
     * @param response
     *              The response to add the session cookie to.
     * @return The new Session.
     */
    public Session create(Request request, Response response) {
        Session s = create(request.getClientInfo().getUser());

        CookieSetting cookie = new CookieSetting(COOKIE_NAME, s.getId());
        cookie.setPath("/");

        request.getCookies().add(cookie);
        response.getCookieSettings().add(cookie);

        return s;
    }

    /**
     * Remove a Session and delete the cookies.
     *
     * @param request
     *              The request with the session cookie to remove
     * @param response
     *              The response to remove the session cookie from.
     */
    public void delete(Request request, Response response) {
        final String sessionid = request.getCookies().getFirstValue(COOKIE_NAME);

        delete(sessionid);

        CookieSetting cookie = new CookieSetting(COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(DELETE);

        request.getCookies().add(cookie);
        response.getCookieSettings().add(cookie);
    }

}
