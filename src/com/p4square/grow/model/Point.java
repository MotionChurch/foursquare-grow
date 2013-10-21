/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

/**
 * Simple double based point class.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class Point {
    /**
     * Parse a comma separated x,y pair into a point.
     *
     * @return The point represented by the string.
     * @throws IllegalArgumentException if the input is malformed.
     */
    public static Point valueOf(String str) {
        final int comma = str.indexOf(',');
        if (comma == -1 || comma == 0 || comma == str.length() - 1) {
            throw new IllegalArgumentException("Malformed point string");
        }

        final String sX = str.substring(0, comma);
        final String sY = str.substring(comma + 1);

        return new Point(Double.valueOf(sX), Double.valueOf(sY));
    }

    private final double mX;
    private final double mY;

    /**
     * Create a new point with the given coordinates.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    public Point(double x, double y) {
        mX = x;
        mY = y;
    }

    /**
     * Compute the distance between this point and another.
     *
     * @param other The other point.
     * @return The distance between this point and other.
     */
    public double distance(Point other) {
        final double dx = mX - other.mX;
        final double dy = mY - other.mY;

        return Math.sqrt(dx*dx + dy*dy);
    }

    /**
     * @return The x coordinate.
     */
    public double getX() {
        return mX;
    }

    /**
     * @return The y coordinate.
     */
    public double getY() {
        return mY;
    }

    /**
     * @return The point represented as a comma separated pair.
     */
    @Override
    public String toString() {
        return String.format("%.2f,%.2f", mX, mY);
    }
}
