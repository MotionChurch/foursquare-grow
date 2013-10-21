/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test the SimpleScoringEngine.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SimpleScoringEngineTest {
    private static final double DELTA = 1e-15;

    public static void main(String... args) {
        org.junit.runner.JUnitCore.main(SimpleScoringEngineTest.class.getName());
    }

    private Question mQuestion;
    private ScoringEngine mEngine;

    @Before
    public void setup() {
        // Setup the Question
        mQuestion = new TextQuestion();

        for (int i = 0; i <= 4; i++) {
            Answer a = new Answer();
            a.setScore(i);
            mQuestion.getAnswers().put("a" + i, a);
        }

        mEngine = new SimpleScoringEngine();
    }

    /**
     * Test that each individual answer is scored correctly.
     */
    @Test
    public void testAllAnswers() {
        for (int i = 1; i <= 4; i++) {
            Score score = new Score();
            RecordedAnswer answer = new RecordedAnswer();
            answer.setAnswerId("a" + i);

            assertTrue(mEngine.scoreAnswer(score, mQuestion, answer));

            assertEquals(1, score.count);
            assertEquals(i, score.sum, DELTA);
        }
    }

    /**
     * Test that each answer score forms an increasing sum.
     */
    @Test
    public void testAllAnswersIncremental() {
        Score score = new Score();

        for (int i = 1; i <= 4; i++) {
            RecordedAnswer answer = new RecordedAnswer();
            answer.setAnswerId("a" + i);

            assertTrue(mEngine.scoreAnswer(score, mQuestion, answer));
        }

        assertEquals(4, score.count);
        assertEquals(10, score.sum, DELTA);
    }

    /**
     * Verify exception is thrown for undefined answer.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUnknownAnswer() {
        Score score = new Score();
        RecordedAnswer answer = new RecordedAnswer();
        answer.setAnswerId("unknown");
        mEngine.scoreAnswer(score, mQuestion, answer);
    }
}
