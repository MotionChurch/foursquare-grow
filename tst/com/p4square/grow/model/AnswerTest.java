/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the Answer class.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class AnswerTest {
    private static final double DELTA = 1e-15;

    public static void main(String... args) {
        org.junit.runner.JUnitCore.main(AnswerTest.class.getName());
    }

    /**
     * Verify that the correct default values are returned.
     */
    @Test
    public void testDefaults() {
        Answer a = new Answer();

        // Type should default to AVERAGE
        assertEquals(Answer.ScoreType.AVERAGE, a.getType());

        // NextQuestion should default to null
        assertNull(a.getNextQuestion());
    }

    /**
     * Verify that getters and setters function correctly.
     */
    @Test
    public void testGetAndSet() {
        Answer a = new Answer();

        a.setText("Answer Text");
        assertEquals("Answer Text", a.getText());

        a.setType(Answer.ScoreType.TRUMP);
        assertEquals(Answer.ScoreType.TRUMP, a.getType());

        a.setScore(10);
        assertEquals(10, a.getScore(), DELTA);

        a.setNextQuestion("nextQuestion");
        assertEquals("nextQuestion", a.getNextQuestion());
    }

    /**
     * Verify that when the ScoreType is NONE, the score is 0.
     */
    @Test
    public void testScoreTypeNone() {
        Answer a = new Answer();

        a.setScore(10);
        assertEquals(10, a.getScore(), DELTA);

        a.setType(Answer.ScoreType.NONE);
        assertEquals(0, a.getScore(), DELTA);
    }

    /**
     * Test score() with type TRUMP.
     */
    @Test
    public void testScoreTrump() {
        Score score = new Score();
        score.sum = 10;
        score.count = 2;

        Answer a = new Answer();
        a.setType(Answer.ScoreType.TRUMP);
        a.setScore(5);

        assertFalse(a.score(score));

        assertEquals(5, score.getSum(), DELTA);
        assertEquals(1, score.getCount());
    }

    /**
     * Test score() with type NONE.
     */
    @Test
    public void testScoreNone() {
        Score score = new Score();
        score.sum = 10;
        score.count = 2;

        Answer a = new Answer();
        a.setScore(5);
        a.setType(Answer.ScoreType.NONE);

        assertTrue(a.score(score));

        assertEquals(10, score.getSum(), DELTA);
        assertEquals(2, score.getCount());
    }

    /**
     * Test score() with type AVERAGE.
     */
    @Test
    public void testScoreAverage() {
        Score score = new Score();
        score.sum = 10;
        score.count = 2;

        Answer a = new Answer();
        a.setScore(5);
        a.setType(Answer.ScoreType.AVERAGE);

        assertTrue(a.score(score));

        assertEquals(15, score.getSum(), DELTA);
        assertEquals(3, score.getCount());
    }

    /**
     * Verify that ScoreType.toString() returns the proper strings.
     */
    @Test
    public void testScoreTypeToString() {
        assertEquals("none", Answer.ScoreType.NONE.toString());
        assertEquals("average", Answer.ScoreType.AVERAGE.toString());
        assertEquals("trump", Answer.ScoreType.TRUMP.toString());
    }
}
