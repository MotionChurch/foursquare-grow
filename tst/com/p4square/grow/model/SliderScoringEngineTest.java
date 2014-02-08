/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import java.util.Map;

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
     * Test the scoreAnswer() method with four answers.
     */
    @Test
    public void testScoreAnswerFourAnswers() {
        // Create the four answers.
        createAnswers(4);

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

        // Test 0.33. Should be 2.
        answer.setAnswerId("0.33333");
        assertTrue(mEngine.scoreAnswer(score, mQuestion, answer));
        assertEquals(3, score.count);
        assertEquals(7, score.sum, DELTA);

        // Test 0.9, should be 4.
        answer.setAnswerId("0.9");
        assertTrue(mEngine.scoreAnswer(score, mQuestion, answer));
        assertEquals(4, score.count);
        assertEquals(11, score.sum, DELTA);
    }

    /**
     * Test the scoreAnswer() method with six answers.
     */
    @Test
    public void testScoreAnswerSixAnswers() {
        // Create the four answers.
        createAnswers(6);

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

        // Test 0.33. Should score as 1.33
        answer.setAnswerId("0.33333");
        assertTrue(mEngine.scoreAnswer(score, mQuestion, answer));
        assertEquals(3, score.count);
        assertEquals(6.3333, score.sum, DELTA);

        // Test 0.55. Should score as 2.66
        answer.setAnswerId("0.55");
        assertTrue(mEngine.scoreAnswer(score, mQuestion, answer));
        assertEquals(4, score.count);
        assertEquals(9, score.sum, DELTA);

        // Test 0.9. Should score as 4.
        answer.setAnswerId("0.90");
        assertTrue(mEngine.scoreAnswer(score, mQuestion, answer));
        assertEquals(5, score.count);
        assertEquals(13, score.sum, DELTA);
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
        createAnswers(4);
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
        createAnswers(4);
        Score score = new Score();
        RecordedAnswer answer = new RecordedAnswer();
        answer.setAnswerId("1.1");
        mEngine.scoreAnswer(score, mQuestion, answer);
    }

    /**
     * Helper method to create a number of questions on the slider.
     * 
     * @param count Number of answers on the questions.
     */
    private void createAnswers(int count) {
        Map<String, Answer> answers = mQuestion.getAnswers();
        answers.clear();
        for (int i = 0; i < count; i++) {
            answers.put(String.valueOf(i), new Answer());
        }
    }
}
