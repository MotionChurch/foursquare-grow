/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.provider;

import java.io.IOException;

import com.p4square.grow.model.Question;

/**
 * QuestionProvider wraps an existing Provider to get and put Questions.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public abstract class QuestionProvider<K> implements Provider<String, Question> {

    private Provider<K, Question> mProvider;

    public QuestionProvider(Provider<K, Question> provider) {
        mProvider = provider;
    }

    @Override
    public Question get(String key) throws IOException {
        return mProvider.get(makeKey(key));
    }

    @Override
    public void put(String key, Question obj) throws IOException {
        mProvider.put(makeKey(key), obj);
    }

    /**
     * Make a Key for questionId.
     *
     * @param questionId The question id.
     * @return a key for questionId.
     */
    protected abstract K makeKey(String questionId);
}
