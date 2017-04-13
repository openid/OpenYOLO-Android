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

package org.openyolo.protocol.internal;

import org.openyolo.protocol.AuthenticationDomain;

/**
 * Implementations of {@link ValueConverter} related to {@link AuthenticationDomain}.
 */
public final class AuthenticationDomainConverters {

    /**
     * Creates {@link AuthenticationDomain} instances from strings.
     */
    public static final ValueConverter<String, AuthenticationDomain> STRING_TO_DOMAIN =
            new StringToAuthenticationDomainConverter();

    /**
     * Extracts the string representation from {@link AuthenticationDomain} instances.
     */
    public static final ValueConverter<AuthenticationDomain, String> DOMAIN_TO_STRING =
            new AuthenticationDomainToStringConverter();

    private AuthenticationDomainConverters() {
        throw new IllegalStateException("not intended to be constructed");
    }

    private static final class StringToAuthenticationDomainConverter
            implements ValueConverter<String, AuthenticationDomain> {
        @Override
        public AuthenticationDomain convert(String value) {
            return new AuthenticationDomain(value);
        }
    }

    private static final class AuthenticationDomainToStringConverter
            implements ValueConverter<AuthenticationDomain, String> {

        @Override
        public String convert(AuthenticationDomain value) {
            return value.toString();
        }
    }
}
