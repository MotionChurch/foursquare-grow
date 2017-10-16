package com.p4square.f1oauth;

import com.p4square.grow.frontend.ProgressReporter;
import com.p4square.grow.model.Chapters;
import org.apache.log4j.Logger;
import org.restlet.security.User;

import java.util.Date;

/**
 * A ProgressReporter implementation to record progress in F1.
 */
public class F1ProgressReporter implements ProgressReporter {

    private static final Logger LOG = Logger.getLogger(F1ProgressReporter.class);

    private F1Access mF1Access;

    public F1ProgressReporter(final F1Access f1access) {
        mF1Access = f1access;
    }

    @Override
    public void reportAssessmentComplete(final User user, final String level, final Date date, final String results) {
        String attributeName = "Assessment Complete - " + level;
        Attribute attribute = new Attribute(attributeName);
        attribute.setStartDate(date);
        attribute.setComment(results);
        addAttribute(user, attribute);
    }

    @Override
    public void reportChapterComplete(final User user, final Chapters chapter, final Date date) {
        final String attributeName = "Training Complete - " + chapter.toString().toLowerCase();
        final Attribute attribute = new Attribute(attributeName);
        attribute.setStartDate(date);
        addAttribute(user, attribute);
    }

    private void addAttribute(final User user, final Attribute attribute) {
        if (!(user instanceof F1User)) {
            throw new IllegalArgumentException("User must be an F1User, but got " + user.getClass().getName());
        }

        try {
            final F1User f1User = (F1User) user;
            final F1API f1 = mF1Access.getAuthenticatedApi(f1User);

            if (!f1.addAttribute(user.getIdentifier(), attribute)) {
                LOG.error("addAttribute failed for " + user.getIdentifier() + " with attribute "
                          + attribute.getAttributeName());
            }
        } catch (Exception e) {
            LOG.error("addAttribute failed for " + user.getIdentifier() + " with attribute "
                      + attribute.getAttributeName(), e);
        }
    }
}
