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

package org.openyolo.demoprovider.barbican;

import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.Patterns;
import java.util.Collection;
import java.util.regex.Pattern;
import org.openyolo.api.IdentifierTypes;
import org.openyolo.proto.Credential;

/**
 * Heuristically identifies the "type" of a password credential based on the ID value.
 */
public final class CredentialClassifier {

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("[a-zA-Z0-9]+");

    /**
     * Determines whether the credential identifier is an email address.
     */
    public static boolean isEmailCredential(Credential credential) {
        return isEmailIdentifier(credential.id);
    }

    /**
     * Determines whether the provided identifier is an email address.
     */
    public static boolean isEmailIdentifier(String identifier) {
        return Patterns.EMAIL_ADDRESS.matcher(identifier).matches();
    }

    /**
     * Determines whether the credential identifier is a phone number.
     */
    public static boolean isPhoneCredential(Credential credential) {
        return isPhoneIdentifier(credential.id);
    }

    /**
     * Determines whether the provided identifier is a phone number.
     */
    public static boolean isPhoneIdentifier(String identifier) {
        return Patterns.PHONE.matcher(identifier).matches();
    }

    /**
     * Determines whether the provided identifier contains alphanumeric characters only.
     */
    public static boolean isAlphanumericIdentifier(String identifier) {
        return ALPHANUMERIC_PATTERN.matcher(identifier).matches();
    }

    /**
     * Determines whether the provided identifier matches any of the provided types.
     *
     * @see {@link IdentifierTypes}
     */
    public static boolean identifierMatchesOneOf(
            @NonNull String identifier,
            @NonNull Collection<Uri> identifierTypes) {
        for (Uri identifierType : identifierTypes) {
            if (IdentifierTypes.EMAIL.equals(identifierType) && isEmailIdentifier(identifier)) {
                return true;
            }

            if (IdentifierTypes.PHONE.equals(identifierType) && isPhoneIdentifier(identifier)) {
                return true;
            }

            if (IdentifierTypes.ALPHANUMERIC.equals(identifierType)
                    && isAlphanumericIdentifier(identifier)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a drawable resource which can be used as a default profile picture for a
     * credential.
     */
    @DrawableRes
    public static int getDefaultIconForCredential(Credential credential) {
        if (isEmailCredential(credential)) {
            return R.drawable.email;
        } else if (isPhoneCredential(credential)) {
            return R.drawable.phone;
        }

        return R.drawable.person;
    }
}
