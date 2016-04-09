/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for ImageQuestion.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class ImageQuestionTest {
    private static final double DELTA = 1e-4;

    public static void main(String... args) {
        org.junit.runner.JUnitCore.main(ImageQuestionTest.class.getName());
    }

    private Question mQuestion;

    @Before
    public void setUp() {
        mQuestion = new ImageQuestion();

        Answer a1 = new Answer();
        a1.setScore(2);

        Answer a2 = new Answer();
        a2.setScore(4);

        mQuestion.getAnswers().put("a1", a1);
        mQuestion.getAnswers().put("a2", a2);
    }

    /**
     * The ScoringEngines are tested extensively independently, so simply
     * verify that we get the expected results for our input.
     */
    @Test
    public void testScoreAnswer() {
        Score score = new Score();
        RecordedAnswer answer = new RecordedAnswer();

        answer.setAnswerId("a1");
        assertTrue(mQuestion.scoreAnswer(score, answer));
        assertEquals(2, score.sum, DELTA);
        assertEquals(1, score.count);

        answer.setAnswerId("a2");
        assertTrue(mQuestion.scoreAnswer(score, answer));
        assertEquals(6, score.sum, DELTA);
        assertEquals(2, score.count);

        try {
            answer.setAnswerId("unknown");
            assertTrue(mQuestion.scoreAnswer(score, answer));
            fail("Should have thrown exception.");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Verify the correct type string is returned.
     */
    @Test
    public void testType() {
        assertEquals("image", mQuestion.getType().toString());
    }
}
