/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for SliderQuestion.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SliderQuestionTest {
    private static final double DELTA = 1e-4;

    public static void main(String... args) {
        org.junit.runner.JUnitCore.main(SliderQuestionTest.class.getName());
    }

    private Question mQuestion;

    @Before
    public void setUp() {
        mQuestion = new SliderQuestion();
    }

    /**
     * The ScoringEngines are tested extensively independently, so simply
     * verify that we get the expected results for our input.
     */
    @Test
    public void testScoreAnswer() {
        Score score = new Score();
        RecordedAnswer answer = new RecordedAnswer();

        answer.setAnswerId("0.66666");
        assertTrue(mQuestion.scoreAnswer(score, answer));
        assertEquals(3, score.sum, DELTA);
        assertEquals(1, score.count);

        answer.setAnswerId("1");
        assertTrue(mQuestion.scoreAnswer(score, answer));
        assertEquals(7, score.sum, DELTA);
        assertEquals(2, score.count);
    }

    /**
     * Verify the correct type string is returned.
     */
    @Test
    public void testType() {
        assertEquals("slider", mQuestion.getType().toString());
    }
}
