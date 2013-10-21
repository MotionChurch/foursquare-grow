/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * Model of an assessment question.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @Type(value = TextQuestion.class, name = "text"),
    @Type(value = ImageQuestion.class, name = "image"),
    @Type(value = SliderQuestion.class, name = "slider"),
    @Type(value = QuadQuestion.class, name = "quad"),
    @Type(value = CircleQuestion.class, name = "circle"),
})
public abstract class Question {
    /**
     * QuestionType indicates the type of Question.
     *
     * @author Jesse Morgan <jesse@jesterpm.net>
     */
    public enum QuestionType {
        TEXT,
        IMAGE,
        SLIDER,
        QUAD,
        CIRCLE;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private String mQuestionId;
    private QuestionType mType;
    private String mQuestionText;
    private Map<String, Answer> mAnswers;

    private String mPreviousQuestionId;
    private String mNextQuestionId;

    public Question() {
        mAnswers = new HashMap<String, Answer>();
    }

    /**
     * @return the id String for this question.
     */
    public String getId() {
        return mQuestionId;
    }

    /**
     * Set the id String for this question.
     * @param id New id
     */
    public void setId(String id) {
        mQuestionId = id;
    }

    /**
     * @return The Question text.
     */
    public String getQuestion() {
        return mQuestionText;
    }

    /**
     * Set the question text.
     * @param value The new question text.
     */
    public void setQuestion(String value) {
        mQuestionText = value;
    }

    /**
     * @return The id String of the previous question or null if no previous question exists.
     */
    public String getPreviousQuestion() {
        return mPreviousQuestionId;
    }

    /**
     * Set the id string of the previous question.
     * @param id Previous question id or null if there is no previous question.
     */
    public void setPreviousQuestion(String id) {
        mPreviousQuestionId = id;
    }

    /**
     * @return The id String of the next question or null if no next question exists.
     */
    public String getNextQuestion() {
        return mNextQuestionId;
    }

    /**
     * Set the id string of the next question.
     * @param id next question id or null if there is no next question.
     */
    public void setNextQuestion(String id) {
        mNextQuestionId = id;
    }

    /**
     * @return a map of Answer id Strings to Answer objects.
     */
    public Map<String, Answer> getAnswers() {
        return mAnswers;
    }

    /**
     * Determine the id of the next question based on the answer to this
     * question.
     *
     * @param answerid
     *              The id of the selected answer.
     * @return a question id or null if this is the last question.
     */
    public String getNextQuestion(String answerid) {
        String nextQuestion = null;

        Answer a = mAnswers.get(answerid);
        if (a != null) {
            nextQuestion = a.getNextQuestion();
        }

        if (nextQuestion == null) {
            nextQuestion = mNextQuestionId;
        }

        return nextQuestion;
    }

    /**
     * Update the score based on the answer to this question.
     *
     * @param score The running score to update.
     * @param answer The answer give to this question.
     * @return true if scoring should continue, false if this answer trumps everything else.
     */
    public abstract boolean scoreAnswer(Score score, RecordedAnswer answer);

    /**
     * @return the QuestionType of this question.
     */
    public abstract QuestionType getType();

}
