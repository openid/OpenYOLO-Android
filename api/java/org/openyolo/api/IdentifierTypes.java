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

package org.openyolo.api;

import android.net.Uri;

/**
 * The standard identifier types defined in the OpenYOLO specification. Any URI of form
 * {@code scheme://authority} can be used as an identifier type, but the values defined here
 * have a common meaning across all credential providers.
 */
public final class IdentifierTypes {

    /**
     * Designates identifiers which are composed of alphanumeric characters only. This is typical
     * of sites with "usernames" instead of email addresses as the primary identifier, e.g.
     * "bob" instead of "bob@example.com".
     */
    public static final Uri ALPHANUMERIC = Uri.parse("openyolo://alphanumeric-identifier");

    /**
     * Designates email address based identifiers.
     */
    public static final Uri EMAIL = Uri.parse("openyolo://email-identifier");

    /**
     * Designates phone number based identifiers.
     */
    public static final Uri PHONE = Uri.parse("openyolo://phone-identifier");

    private IdentifierTypes() {
        throw new IllegalStateException("Not intended to be constructed");
    }
}
