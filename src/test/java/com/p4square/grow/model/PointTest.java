/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the Point class.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class PointTest {
    private static final double DELTA = 1e-15;

    public static void main(String... args) {
        org.junit.runner.JUnitCore.main(PointTest.class.getName());
    }

    /**
     * Verify that the constructor works properly.
     */
    @Test
    public void testHappyCase() {
        Point p = new Point(1, 2);
        assertEquals(1, p.getX(), DELTA);
        assertEquals(2, p.getY(), DELTA);
    }

    /**
     * Verify distance is computed correctly.
     */
    @Test
    public void testDistance() {
        Point p1, p2;

        // Simple line
        p1 = new Point(2, 1);
        p2 = new Point(-2, 1);
        assertEquals(4, p1.distance(p2), DELTA);
        assertEquals(4, p2.distance(p1), DELTA);

        // Across origin
        p1 = new Point(5, 1);
        p2 = new Point(-3, -2);
        assertEquals(Math.sqrt(73), p1.distance(p2), DELTA);
        assertEquals(Math.sqrt(73), p2.distance(p1), DELTA);
    }

    /**
     * Verify toString returns the expected string.
     */
    @Test
    public void testToString() {
        Point p = new Point(-1.12345, 2.3);
        assertEquals("-1.12,2.30", p.toString());
    }

    /**
     * Verify that valueOf correctly parses a variety of strings.
     */
    @Test
    public void testValueOfHappyCase() {
        Point p;

        p = Point.valueOf("1,2");
        assertEquals(1, p.getX(), DELTA);
        assertEquals(2, p.getY(), DELTA);

        p = Point.valueOf("1.5,2.0");
        assertEquals(1.5, p.getX(), DELTA);
        assertEquals(2.0, p.getY(), DELTA);

        p = Point.valueOf("-1.5,2.0");
        assertEquals(-1.5, p.getX(), DELTA);
        assertEquals(2.0, p.getY(), DELTA);

        p = Point.valueOf("1.5,-2.0");
        assertEquals(1.5, p.getX(), DELTA);
        assertEquals(-2.0, p.getY(), DELTA);

        p = Point.valueOf("-1.5,-2.0");
        assertEquals(-1.5, p.getX(), DELTA);
        assertEquals(-2.0, p.getY(), DELTA);
    }

    /**
     * Verify that valueOf fails on null string.
     */
    @Test(expected = NullPointerException.class)
    public void testValueOfNull() {
        Point.valueOf(null);
    }

    /**
     * Verify that valueOf fails on empty string.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testValueOfEmptyString() {
        Point.valueOf("");
    }

    /**
     * Verify that valueOf fails on missing comma.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testValueOfMissingComma() {
        Point.valueOf("123");
    }

    /**
     * Verify that valueOf fails on missing x.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testValueOfMissingX() {
        Point.valueOf(",12");
    }

    /**
     * Verify that valueOf fails on missing y.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testValueOfMissingY() {
        Point.valueOf("12,");
    }
}
