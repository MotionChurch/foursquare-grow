/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the Question class.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class QuestionTest {
    public static void main(String... args) {
        org.junit.runner.JUnitCore.main(QuestionTest.class.getName());
    }

    /**
     * Verify that all the getters and setters function.
     */
    @Test
    public void testGetAndSet() {
        TextQuestion q = new TextQuestion();

        q.setId("123");
        assertEquals("123", q.getId());

        q.setQuestion("Hello World");
        assertEquals("Hello World", q.getQuestion());

        q.setPreviousQuestion("122");
        assertEquals("122", q.getPreviousQuestion());

        q.setNextQuestion("124");
        assertEquals("124", q.getNextQuestion());
    }

    /**
     * Verify the correct next question is returned.
     */
    @Test
    public void testGetNextQuestion() {
        // Setup the Question
        TextQuestion q = new TextQuestion();
        q.setNextQuestion("defaultNext");

        Answer answerWithNext = new Answer();
        answerWithNext.setNextQuestion("answerNext");

        q.getAnswers().put("withNext", answerWithNext);
        q.getAnswers().put("withoutNext", new Answer());

        // Answer without a nextQuestion should return default.
        assertEquals("defaultNext", q.getNextQuestion("withoutNext"));

        // Answer with a nextQuestion should return it's next question.
        assertEquals("answerNext", q.getNextQuestion("withNext"));

        // Unknown answer should also return the default
        assertEquals("defaultNext", q.getNextQuestion("unknownAnswer"));
    }

    /**
     * Validate the toString() results for the enum.
     *
     * This may seem like an odd test, but it is very important for these to be
     * lowercase to match the values in the JSON files.
     */
    @Test
    public void testToString() {
        assertEquals("text", Question.QuestionType.TEXT.toString());
        assertEquals("image", Question.QuestionType.IMAGE.toString());
        assertEquals("slider", Question.QuestionType.SLIDER.toString());
        assertEquals("quad", Question.QuestionType.QUAD.toString());
        assertEquals("circle", Question.QuestionType.CIRCLE.toString());
    }
}
