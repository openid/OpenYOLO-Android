/*
 * Copyright 2017 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openyolo.demoapp.passwordlogin.userdata;

import android.util.Base64;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Generates salts and hashes for password storage and verification.
 * *NOTE*: This implementation is *NOT SECURE ENOUGH* for a real implementation of a password
 * database, but is sufficient for a demo. For more information, see:
 * https://en.wikipedia.org/wiki/Cryptographic_hash_function#Password_verification
 */
public final class HashUtil {

    private static final int SALT_ENTROPY = 16;

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a salt from a cryptographically secure source of randomness.
     */
    public static String generateSalt() {
        byte[] saltBytes = new byte[SALT_ENTROPY];
        RANDOM.nextBytes(saltBytes);
        return Base64.encodeToString(saltBytes, Base64.NO_WRAP | Base64.URL_SAFE);
    }

    /**
     * Creates a salted hash of a password.
     */
    public static String hashPassword(String salt, String password) {
        return base64Hash(salt + password);
    }

    /**
     * Creates a Base64 encoded, SHA-256 hash of the provided string.
     */
    public static String base64Hash(String str) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Platform does not support SHA-256");
        }

        byte[] hashBytes = digest.digest(str.getBytes(Charset.forName("UTF-8")));
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP | Base64.URL_SAFE);
    }
}
