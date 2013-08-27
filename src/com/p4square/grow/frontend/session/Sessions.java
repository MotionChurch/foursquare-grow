/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend.session;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import org.restlet.Response;
import org.restlet.Request;
import org.restlet.security.User;

/**
 * Singleton Session Manager.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class Sessions {
    private static final String COOKIE_NAME  = "S";

    private static final Sessions THE = new Sessions();
    public static Sessions getInstance() {
        return THE;
    }

    private final Map<String, Session> mSessions;

    private Sessions() {
        mSessions = new ConcurrentHashMap<String, Session>();
    }

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
     * @return A session or null if no session is found.
     */
    public Session get(Request request) {
        final String cookie = request.getCookies().getFirstValue(COOKIE_NAME);

        if (cookie != null) {
            return get(cookie);
        }

        return null;
    }

    public Session create(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Can not create session for null user.");
        }

        Session s = new Session(user);
        mSessions.put(s.getId(), s);

        return s;
    }

    /**
     * Create a new Session and add the Session cookie to the response.
     */
    public Session create(Request request, Response response) {
        Session s = create(request.getClientInfo().getUser());

        request.getCookies().add(COOKIE_NAME, s.getId());
        response.getCookieSettings().add(COOKIE_NAME, s.getId());

        return s;
    }
}
