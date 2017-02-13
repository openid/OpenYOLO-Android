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

package org.openyolo.api;

import android.net.Uri;

/**
 * A set of well-known authentication method URIs for use with
 * {@link org.openyolo.api.Credential.Builder#setAuthenticationMethod(String)}.
 */
public final class AuthenticationMethods {

    /**
     * Identifier and password based authentication.
     */
    public static final Uri ID_AND_PASSWORD = Uri.parse("openyolo://id-and-password");

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

    /**
     * LinkedIn OAuth2 authentication.
     *
     * @see <a href="https://developer.linkedin.com/docs/oauth2">"Authenticating with OAuth2"</a>
     * @see <a href="https://developer.linkedin.com/docs/android-sdk-auth">Authenticating with
     *     the Mobile SDK for Android</a>
     */
    public static final Uri LINKEDIN = Uri.parse("https://www.linkedin.com");

    /**
     * Microsoft OpenID Connect authentication.
     */
    public static final Uri MICROSOFT = Uri.parse("https://login.live.com");

    /**
     * Paypal OpenID Connect authentication.
     *
     * @see <a href="https://developer.paypal.com/docs/integration/direct/identity/log-in-with-paypal/">
     *     Integrate login with Paypal</a>
     */
    public static final Uri PAYPAL = Uri.parse("https://www.paypal.com");

    /**
     * Yahoo OAuth2 authentication.
     *
     * @see <a href="https://developer.yahoo.com/oauth2/guide/">Yahoo OAuth 2.0 Guide</a>
     */
    public static final Uri YAHOO = Uri.parse("https://login.yahoo.com");
}
