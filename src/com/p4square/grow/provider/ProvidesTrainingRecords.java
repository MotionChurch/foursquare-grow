/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.provider;

import com.p4square.grow.model.TrainingRecord;

/**
 * Indicates the ability to provide a TrainingRecord Provider.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public interface ProvidesTrainingRecords {
    /**
     * @return A Provider of Questions keyed by question id.
     */
    Provider<String, TrainingRecord> getTrainingRecordProvider();
}
