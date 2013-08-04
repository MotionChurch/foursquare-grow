/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

/**
 * Simple double based point class.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
class Point {
    public static Point valueOf(String str) {
        final int comma = str.indexOf(',');
        if (comma == -1) {
            throw new IllegalArgumentException("Malformed point string");
        }

        final String sX = str.substring(0, comma);
        final String sY = str.substring(comma + 1);

        return new Point(Double.valueOf(sX), Double.valueOf(sY));
    }     

    private final double mX;
    private final double mY;

    public Point(double x, double y) {
        mX = x;
        mY = y;
    }

    public double distance(Point other) {
        final double dx = mX - other.mX;
        final double dy = mY - other.mY;

        return Math.sqrt(dx*dx + dy*dy);
    }

    public double getX() {
        return mX;
    }

    public double getY() {
        return mY;
    }

    @Override
    public String toString() {
        return String.format("%.2f,%.2f", mX, mY);
    }
}
