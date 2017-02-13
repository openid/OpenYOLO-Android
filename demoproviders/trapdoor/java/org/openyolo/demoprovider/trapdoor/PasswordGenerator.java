/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
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

package org.openyolo.demoprovider.trapdoor;

import java.math.BigInteger;
import java.nio.charset.Charset;
import org.spongycastle.crypto.digests.Blake2bDigest;

/**
 * Generates passwords to handle credential retrieve requests.
 */
public class PasswordGenerator {

    private static final int HASH_SIZE = 8;

    private static final int PASSWORD_SIZE = 12;

    private static final String BASE56_ENCODING_CHARS =
            "8Zy5giQPhGbUS4FBJjDfuVwE9t7LkHX2sAKv6NczRdpnTWmexqa3rCYM";

    /**
     * Generates a password for a given user name, pin and unique application identifier.
     */
    public static String generatePassword(String userName, String pin, String appId) {
        String plainText = userName + "-" + pin + "-" + appId;
        byte[] plainTextBytes = plainText.getBytes(Charset.forName("UTF-8"));
        return toBase56(digest(plainTextBytes));
    }

    private static byte[] digest(byte[] input) {
        Blake2bDigest digest = new Blake2bDigest(
                null, /* no key */
                HASH_SIZE,
                null, /* no salt */
                null); /* no personalization of the hashing function */

        digest.update(input, 0, input.length);
        byte[] result = new byte[HASH_SIZE];
        digest.doFinal(result, 0);

        return result;
    }

    private static String toBase56(byte[] value) {
        StringBuilder builder = new StringBuilder();
        BigInteger quotient = new BigInteger(value);

        BigInteger base = new BigInteger("56");

        int size = 0;
        while (quotient.compareTo(BigInteger.ZERO) > 0 || size < PASSWORD_SIZE) {
            int remainder = quotient.mod(base).intValue();
            builder.append(BASE56_ENCODING_CHARS.charAt(remainder));
            quotient = quotient.divide(base);
            size++;
        }

        return builder.toString();
    }
}
