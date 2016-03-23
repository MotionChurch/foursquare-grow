package com.p4square.grow.ccb;

import com.p4square.ccbapi.CCBAPI;
import com.p4square.grow.frontend.ProgressReporter;
import org.restlet.security.User;

import java.util.Date;

/**
 * A ProgressReporter which records progress in CCB.
 *
 * Except not really, because it's not implemented yet.
 * This is just a placeholder until ccb-api-client-java has support for updating an individual.
 */
public class CCBProgressReporter implements ProgressReporter {

    private final CCBAPI mAPI;

    public CCBProgressReporter(final CCBAPI api) {
        mAPI = api;
    }

    @Override
    public void reportAssessmentComplete(User user, String level, Date date, String results) {
        // TODO
    }

    @Override
    public void reportChapterComplete(User user, String chapter, Date date) {
        // TODO
    }
}
