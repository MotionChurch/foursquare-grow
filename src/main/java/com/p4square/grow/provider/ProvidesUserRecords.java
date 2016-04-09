/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.provider;

import com.p4square.grow.model.UserRecord;

/**
 * Indicates the ability to provide a UserRecord Provider.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public interface ProvidesUserRecords {
    /**
     * @return A Provider of Questions keyed by question id.
     */
    Provider<String, UserRecord> getUserRecordProvider();
}
