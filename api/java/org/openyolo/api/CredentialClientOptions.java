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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.valid4j.Validation.validate;

import android.content.Context;
import android.support.annotation.NonNull;

import org.openyolo.api.persistence.AppSettings;


/**
 * Context object that may be used to configure a {@link CredentialClient} instance. It is
 * recommended to use the default options via {@link CredentialClient#getInstance(Context)}.
 */
public class CredentialClientOptions {
    private final AppSettings mAppSettings;

    private CredentialClientOptions(Builder builder) {
        mAppSettings = builder.mAppSettings;
    }

    @NonNull
    AppSettings getDeviceState() {
        return mAppSettings;
    }

    /**
     * Builder for {@link CredentialClientOptions}.
     */
    public static final class Builder {
        private AppSettings mAppSettings;

        /**
         * Default builder for {@link CredentialClientOptions}.
         */
        public Builder(@NonNull AppSettings deviceState) {
            setDeviceState(deviceState);
        }

        /**
         * Specifies the {@link AppSettings} implementation that will be used by the
         * {@link CredentialClient}.
         */
        public Builder setDeviceState(@NonNull AppSettings appSettings) {
            validate(appSettings, notNullValue(), NullPointerException.class);

            this.mAppSettings = appSettings;
            return this;
        }

        /**
         * Returns a new {@link CredentialClientOptions} instance based on the configuration of this
         * builder.
         */
        public CredentialClientOptions build() {
            return new CredentialClientOptions(this);
        }
    }
}
