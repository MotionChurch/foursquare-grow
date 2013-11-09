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
        assertEquals(4, Score.numericScore("teacher"));
        assertEquals(3, Score.numericScore("disciple"));
        assertEquals(2, Score.numericScore("believer"));
        assertEquals(1, Score.numericScore("seeker"));
    }

    /**
     * Verify that toString() returns the correct mappings.
     */
    @Test
    public void testToString() {
        mScore.count = 1;

        // Seeker is defined as score < 2
        mScore.sum = 0;
        assertEquals("seeker", mScore.toString());
        mScore.sum = 0.5;
        assertEquals("seeker", mScore.toString());
        mScore.sum = 1;
        assertEquals("seeker", mScore.toString());
        mScore.sum = 1.5;
        assertEquals("seeker", mScore.toString());
        mScore.sum = 1.99;
        assertEquals("seeker", mScore.toString());

        // Believer is defined as 2 <= score < 3
        mScore.sum = 2;
        assertEquals("believer", mScore.toString());
        mScore.sum = 2.5;
        assertEquals("believer", mScore.toString());
        mScore.sum = 2.99;
        assertEquals("believer", mScore.toString());

        // Disciple is defined as 3 <= score < 4
        mScore.sum = 3;
        assertEquals("disciple", mScore.toString());
        mScore.sum = 3.5;
        assertEquals("disciple", mScore.toString());
        mScore.sum = 3.99;
        assertEquals("disciple", mScore.toString());

        // Teacher is defined as 4 <= score
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
