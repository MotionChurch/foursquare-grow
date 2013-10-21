/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test the QuadScoringEngine.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class QuadScoringEngineTest {
    private static final double DELTA = 1e-4;

    public static void main(String... args) {
        org.junit.runner.JUnitCore.main(QuadScoringEngineTest.class.getName());
    }

    private Question mQuestion;
    private ScoringEngine mEngine;

    @Before
    public void setup() {
        // Setup the Question
        mQuestion = new QuadQuestion();
        Map<String, Answer> answers = mQuestion.getAnswers();

        // Create four answers at (-1,-1), (1, -1), (-1, 1), (1, 1)
        for (int i = 1; i <= 4; i++) {
            int x = i % 2 == 0 ? 1 : -1;
            int y = i > 2 ? 1 : -1;

            Answer a = new Answer();
            a.setScore(i);
            answers.put(x + ".00," + y + ".00", a);
        }

        mEngine = new QuadScoringEngine();
    }

    /**
     * Test a point inside each quadrant.
     */
    @Test
    public void testEachQuadrant() {
        Score score;
        RecordedAnswer answer = new RecordedAnswer();

        // 0.5,0.5 == 4
        score = new Score();
        answer.setAnswerId("0.5,0.5");
        assertTrue(mEngine.scoreAnswer(score, mQuestion, answer));
        assertEquals(4, score.getSum(), DELTA);
        assertEquals(1, score.getCount());

        // 0.5,-0.5 == 2
        score = new Score();
        answer.setAnswerId("0.5,-0.5");
        assertTrue(mEngine.scoreAnswer(score, mQuestion, answer));
        assertEquals(2, score.getSum(), DELTA);
        assertEquals(1, score.getCount());

        // -0.5,0.5 == 3
        score = new Score();
        answer.setAnswerId("-0.5,0.5");
        assertTrue(mEngine.scoreAnswer(score, mQuestion, answer));
        assertEquals(3, score.getSum(), DELTA);
        assertEquals(1, score.getCount());

        // -0.5,-0.5 == 0.5
        score = new Score();
        answer.setAnswerId("-0.5,-0.5");
        assertTrue(mEngine.scoreAnswer(score, mQuestion, answer));
        assertEquals(1, score.getSum(), DELTA);
        assertEquals(1, score.getCount());
    }

}
