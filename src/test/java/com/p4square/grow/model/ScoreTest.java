/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for the Score class.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class ScoreTest {
    private static final double DELTA = 1e-4;

    public static void main(String... args) {
        org.junit.runner.JUnitCore.main(ScoreTest.class.getName());
    }

    private Score mScore;

    @Before
    public void setUp() {
        mScore = new Score();
    }

    /**
     * Verify getters and setters function.
     */
    @Test
    public void testGetAndSet() {
        // getSum()
        mScore.sum = 1.1;
        assertEquals(1.1, mScore.getSum(), DELTA);

        // getCount()
        mScore.count = 5;
        assertEquals(5, mScore.getCount());
    }

    /**
     * Verify that the average is computed by getScore().
     */
    @Test
    public void testGetScore() {
        mScore.sum = 7;
        mScore.count = 2;
        assertEquals(3.5, mScore.getScore(), DELTA);
    }

    /**
     * Verify that numericScore() returns the correct mappings.
     */
    @Test
    public void testNumericScore() {
        assertEquals(3.5, Score.numericScore("teacher"), DELTA);
        assertEquals(2.5, Score.numericScore("disciple"), DELTA);
        assertEquals(1.5, Score.numericScore("believer"), DELTA);
        assertEquals(0, Score.numericScore("seeker"), DELTA);
    }

    /**
     * Verify that numericScore() throws if a non-score is passed in.
     */
    @Test(expected =  IllegalArgumentException.class)
    public void testInvalidScoreString() {
        // Introduction is not a valid score.
        Score.numericScore("introduction");
    }

    /**
     * Verify that toString() returns the correct mappings.
     */
    @Test
    public void testToString() {
        mScore.count = 1;

        // Seeker is defined as score < 1.5
        mScore.sum = 0;
        assertEquals("seeker", mScore.toString());
        mScore.sum = 0.5;
        assertEquals("seeker", mScore.toString());
        mScore.sum = 1;
        assertEquals("seeker", mScore.toString());
        mScore.sum = 1.49;
        assertEquals("seeker", mScore.toString());

        // Believer is defined as 1.5 <= score < 2.5
        mScore.sum = 1.5;
        assertEquals("believer", mScore.toString());
        mScore.sum = 2;
        assertEquals("believer", mScore.toString());
        mScore.sum = 2.49;
        assertEquals("believer", mScore.toString());

        // Disciple is defined as 2.5 <= score < 3.5
        mScore.sum = 2.5;
        assertEquals("disciple", mScore.toString());
        mScore.sum = 3;
        assertEquals("disciple", mScore.toString());
        mScore.sum = 3.49;
        assertEquals("disciple", mScore.toString());

        // Teacher is defined as 3.5 <= score
        mScore.sum = 3.5;
        assertEquals("teacher", mScore.toString());
        mScore.sum = 4;
        assertEquals("teacher", mScore.toString());
        mScore.sum = 4.5;
        assertEquals("teacher", mScore.toString());
        mScore.sum = 4.99;
        assertEquals("teacher", mScore.toString());
        mScore.sum = 5;
        assertEquals("teacher", mScore.toString());
    }
}
