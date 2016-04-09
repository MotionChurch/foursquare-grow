/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.backend;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import org.restlet.security.SecretVerifier;

import com.p4square.grow.model.UserRecord;
import com.p4square.grow.provider.Provider;

/**
 * Verify the given credentials against the users with backend access.
 */
public class BackendVerifier extends SecretVerifier {

    private final Provider<String, UserRecord> mUserProvider;

    public BackendVerifier(Provider<String, UserRecord> userProvider) {
        mUserProvider = userProvider;
    }

    @Override
    public int verify(String identifier, char[] secret) {
        if (identifier == null) {
            throw new IllegalArgumentException("Null identifier");
        }

        if (secret == null) {
            throw new IllegalArgumentException("Null secret");
        }

        // Does the user exist?
        UserRecord user;
        try {
            user = mUserProvider.get(identifier);
            if (user == null) {
                return RESULT_UNKNOWN;
            }

        } catch (IOException e) {
            return RESULT_UNKNOWN;
        }

        // Does the user have a backend password?
        String storedHash = user.getBackendPasswordHash();
        if (storedHash == null) {
            // This user doesn't have access
            return RESULT_INVALID;
        }

        // Validate the password.
        try {
            String hashedInput = hashPassword(secret);
            if (hashedInput.equals(storedHash)) {
                return RESULT_VALID;
            }

        } catch (NoSuchAlgorithmException e) {
            return RESULT_UNSUPPORTED;
        }

        // If all else fails, fail.
        return RESULT_INVALID;
    }

    /**
     * Hash the given secret.
     */
    public static String hashPassword(char[] secret) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");

        // Convert the char[] to byte[]
        // FIXME This approach is incorrectly truncating multibyte
        // characters.
        byte[] b = new byte[secret.length];
        for (int i = 0; i < secret.length; i++) {
            b[i] = (byte) secret[i];
        }

        md.update(b);

        byte[] hash = md.digest();
        return new String(Hex.encodeHex(hash));
    }
}
