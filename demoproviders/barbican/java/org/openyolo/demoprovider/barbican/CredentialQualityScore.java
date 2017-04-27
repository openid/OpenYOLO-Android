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

package org.openyolo.demoprovider.barbican;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import java.util.Comparator;
import org.openyolo.demoprovider.barbican.Protobufs.AccountHint;
import org.openyolo.protocol.AuthenticationMethod;
import org.openyolo.protocol.AuthenticationMethods;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.Protobufs;

/**
 * Derives quality scores for credentials and hints that can help to order lists of them based on
 * how useful they are to an application.
 */
public class CredentialQualityScore {

    /**
     * A comparator which orders credentials in descending order of quality score, i.e. those with
     * the highest quality score first.
     */
    public static final Comparator<Credential> QUALITY_SORT = new CredentialQualityComparator();

    /**
     * A comparator which orders credential protocol buffers in descending order of quality score,
     * i.e. those with the highest quality score first.
     */
    public static final Comparator<Protobufs.Credential> PROTO_QUALITY_SORT =
            new CredentialProtoQualityComparator();

    /**
     * Generates the score for the user data in an account hint.
     */
    public static int getScore(AccountHint hint) {
        return getQualityScore(
                hint.getIdentifier(),
                new AuthenticationMethod(hint.getAuthMethod()),
                hint.getName(),
                hint.getPictureUri());
    }

    /**
     * Generates the score for the user data in a credential protocol buffer.
     */
    public static int getScore(Protobufs.Credential credential) {
        return getQualityScore(
                credential.getId(),
                AuthenticationMethod.fromProtobuf(credential.getAuthMethod()),
                credential.getDisplayName(),
                credential.getDisplayPictureUri());
    }

    /**
     * Generates the score for the user data in a credential.
     */
    public static int getScore(Credential credential) {
        return getQualityScore(
                credential.getIdentifier(),
                credential.getAuthenticationMethod(),
                credential.getDisplayName(),
                credential.getDisplayPicture() != null
                        ? credential.getDisplayPicture().toString()
                        : null);
    }

    private static int getQualityScore(
            @Nullable String id,
            @Nullable AuthenticationMethod authMethod,
            @Nullable String name,
            @Nullable String pictureUri) {

        int score = 1;

        // email identifiers are recognisable and useful for ID token acquisition
        if (AuthenticationMethods.EMAIL.equals(authMethod)) {
            score <<= 1;
        }

        // prefer credentials that have a human readable name
        if (!TextUtils.isEmpty(name)) {
            score <<= 1;
        }

        // prefer credentials that have a profile picture
        if (pictureUri != null) {
            score <<= 1;
        }

        return score;
    }

    private static final class CredentialQualityComparator implements Comparator<Credential> {

        @Override
        public int compare(Credential c1, Credential c2) {
            int c1Score = getScore(c1);
            int c2Score = getScore(c2);

            // higher scores (more information) come first
            if (c1Score > c2Score) {
                return -1;
            } else if (c1Score == c2Score) {
                return 0;
            }

            return 1;
        }
    }

    private static final class CredentialProtoQualityComparator
            implements Comparator<Protobufs.Credential> {

        @Override
        public int compare(Protobufs.Credential c1, Protobufs.Credential c2) {
            int c1Score = getScore(c1);
            int c2Score = getScore(c2);

            // higher scores (more information) come first
            if (c1Score > c2Score) {
                return -1;
            } else if (c1Score == c2Score) {
                return 0;
            }

            return 1;
        }
    }
}
