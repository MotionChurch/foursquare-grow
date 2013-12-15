/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend;

/**
 * Display the Group Leader training videos.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class GroupLeaderTrainingPageResource extends TrainingPageResource {
    private static final String[] CHAPTERS = { "leader" };

    @Override
    public void doInit() {
        super.doInit();

        mChapter = "leader";
    }

    @Override
    public String[] getChaptersInOrder() {
        return CHAPTERS;
    }
}
