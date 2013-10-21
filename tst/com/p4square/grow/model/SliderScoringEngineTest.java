/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test the SliderScoringEngine.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class SliderScoringEngineTest {
    private static final double DELTA = 1e-4;

    public static void main(String... args) {
        org.junit.runner.JUnitCore.main(SliderScoringEngineTest.class.getName());
    }

    private Question mQuestion;
    private ScoringEngine mEngine;

    @Before
    public void setup() {
        // Setup the Question
        mQuestion = new SliderQuestion();
        mEngine = new SliderScoringEngine();
    }

    /**
     * Test the scoreAnswer() method.
     */
    @Test
    public void testScoreAnswer() {
        Score score = new Score();
        RecordedAnswer answer = new RecordedAnswer();

        // Test 0
        answer.setAnswerId("0");
        assertTrue(mEngine.scoreAnswer(score, mQuestion, answer));
        assertEquals(1, score.count);
        assertEquals(1, score.sum, DELTA);

        // Test 1
        answer.setAnswerId("1");
        assertTrue(mEngine.scoreAnswer(score, mQuestion, answer));
        assertEquals(2, score.count);
        assertEquals(5, score.sum, DELTA);

        // Test fraction (0.33)
        answer.setAnswerId("0.33333");
        assertTrue(mEngine.scoreAnswer(score, mQuestion, answer));
        assertEquals(3, score.count);
        assertEquals(7, score.sum, DELTA);
    }

    /**
     * Verify exception is thrown for non-numeric answer.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNonNumericAnswer() {
        Score score = new Score();
        RecordedAnswer answer = new RecordedAnswer();
        answer.setAnswerId("unknown");
        mEngine.scoreAnswer(score, mQuestion, answer);
    }

    /**
     * Verify exception is thrown for negative answer.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeAnswer() {
        Score score = new Score();
        RecordedAnswer answer = new RecordedAnswer();
        answer.setAnswerId("-1");
        mEngine.scoreAnswer(score, mQuestion, answer);
    }

    /**
     * Verify exception is thrown for out of bounds answer.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAnswerOutOfBounds() {
        Score score = new Score();
        RecordedAnswer answer = new RecordedAnswer();
        answer.setAnswerId("1.1");
        mEngine.scoreAnswer(score, mQuestion, answer);
    }
}
