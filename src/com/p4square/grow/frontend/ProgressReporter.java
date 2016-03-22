package com.p4square.grow.frontend;

import org.restlet.security.User;

import java.util.Date;

/**
 * A ProgressReporter is used to record a User's progress in a Church Management System.
 */
public interface ProgressReporter {

    /**
     * Report that the User completed the assessment.
     *
     * @param user The user who completed the assessment.
     * @param level The assessment level.
     * @param date The completion date.
     * @param results Result information (e.g. json of the results).
     */
    void reportAssessmentComplete(User user, String level, Date date, String results);

    /**
     *
     * @param user The user who completed the chapter.
     * @param chapter The chatper completed.
     * @param date Teh completion date.
     */
    void reportChapterComplete(User user, String chapter, Date date);
}
