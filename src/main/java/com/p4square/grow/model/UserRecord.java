/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.model;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import org.restlet.security.User;

/**
 * A simple user representation without any secrets.
 */
public class UserRecord {
    private String mId;
    private String mFirstName;
    private String mLastName;
    private String mEmail;
    private String mLanding;
    private boolean mNewBeliever;

    // Backend Access
    private String mBackendPasswordHash;

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
    public void setId(final String value) {
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
    public void setEmail(final String value) {
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
    public void setFirstName(final String value) {
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
    public void setLastName(final String value) {
        mLastName = value;
    }

    /**
     * @return The user's landing page.
     */
    public String getLanding() {
        return mLanding;
    }

    /**
     * Set the user's landing page.
     * @param value The new landing page.
     */
    public void setLanding(final String value) {
        mLanding = value;
    }

    /**
     * @return true if the user came from the New Believer's landing.
     */
    public boolean getNewBeliever() {
        return mNewBeliever;
    }

    /**
     * Set the user's new believer flag.
     * @param value The new flag.
     */
    public void setNewBeliever(final boolean value) {
        mNewBeliever = value;
    }

    /**
     * @return The user's backend password hash, null if he doesn't have
     * access.
     */
    public String getBackendPasswordHash() {
        return mBackendPasswordHash;
    }

    /**
     * Set the user's backend password hash.
     * @param value The new backend password hash or null to remove
     * access.
     */
    public void setBackendPasswordHash(final String value) {
        mBackendPasswordHash = value;
    }

    /**
     * Set the user's backend password to the clear-text value given.
     * @param value The new backend password.
     */
    public void setBackendPassword(final String value) {
        try {
            mBackendPasswordHash = hashPassword(value);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Hash the given secret.
     */
    public static String hashPassword(final String secret) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");

        // Convert the char[] to byte[]
        // FIXME This approach is incorrectly truncating multibyte
        // characters.
        byte[] b = new byte[secret.length()];
        for (int i = 0; i < secret.length(); i++) {
            b[i] = (byte) secret.charAt(i);
        }

        md.update(b);

        byte[] hash = md.digest();
        return new String(Hex.encodeHex(hash));
    }
}
