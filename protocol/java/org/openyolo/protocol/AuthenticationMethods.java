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

package org.openyolo.protocol;

import android.net.Uri;
import org.openyolo.protocol.Credential.Builder;

/**
 * A set of well-known authentication method URIs for use with
 * {@link Builder#setAuthenticationMethod(String)}.
 */
public final class AuthenticationMethods {

    /**
     * Email address based authentication, optionally with a password. This authentication type
     * is typically for accounts which have an email address as the primary (user facing)
     * identifier. Authentication typically occurs using the email address and a password, but
     * is also possible using the email address as a recovery method.
     */
    public static final Uri EMAIL = Uri.parse("openyolo://email");

    /**
     * Phone number based authentication, optionally with a password. This authentication type
     * is typically for accounts which have a phone number as the primary (user facing)
     * identifier. Authentication typically occurs by sending a code to the phone number, and
     * possible also requiring the entry of a password or pin.
     *
     * <p>When used to request a hint, OpenYOLO providers _MUST_ return the selected phone number
     * in E.164 format.
     */
    public static final Uri PHONE = Uri.parse("openyolo://phone");

    /**
     * User name and password based authentication. This authentication type requires the entry
     * of a unicode identifier string and a password.
     */
    public static final Uri USER_NAME = Uri.parse("openyolo://username");

    /**
     * Google Sign-in authentication.
     *
     * @see <a href="https://developers.google.com/identity/sign-in/android/">Google Sign-in for
     *     Android</a>.
     */
    public static final Uri GOOGLE = Uri.parse("https://accounts.google.com");

    /**
     * Facebook Login authentication.
     *
     * @see <a href="https://developers.facebook.com/docs/facebook-login/android">Facebook Login
     *     for Android</a>.
     */
    public static final Uri FACEBOOK = Uri.parse("https://www.facebook.com");

    private AuthenticationMethods() {
        throw new IllegalStateException("not intended to be constructed");
    }
}
