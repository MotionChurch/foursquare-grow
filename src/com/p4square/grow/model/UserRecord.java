/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.model;

import org.restlet.security.User;

/**
 * A simple user representation without any secrets.
 */
public class UserRecord {
    private String mId;
    private String mFirstName;
    private String mLastName;
    private String mEmail;

    /**
     * Create an empty UserRecord.
     */
    public UserRecord() {
    }

    /**
     * Create a new UserRecord with the information from a User.
     */
    public UserRecord(final User user) {
        mId = user.getIdentifier();
        mFirstName = user.getFirstName();
        mLastName = user.getLastName();
        mEmail = user.getEmail();
    }

    /**
     * @return The user's identifier.
     */
    public String getId() {
        return mId;
    }

    /**
     * Set the user's identifier.
     * @param value The new id.
     */
    public void setId (final String value) {
        mId = value;
    }

    /**
     * @return The user's email.
     */
    public String getEmail() {
        return mEmail;
    }

    /**
     * Set the user's email.
     * @param value The new email.
     */
    public void setEmail (final String value) {
        mEmail = value;
    }

    /**
     * @return The user's first name.
     */
    public String getFirstName() {
        return mFirstName;
    }

    /**
     * Set the user's first name.
     * @param value The new first name.
     */
    public void setFirstName (final String value) {
        mFirstName = value;
    }

    /**
     * @return The user's last name.
     */
    public String getLastName() {
        return mLastName;
    }

    /**
     * Set the user's last name.
     * @param value The new last name.
     */
    public void setLastName (final String value) {
        mLastName = value;
    }
}
