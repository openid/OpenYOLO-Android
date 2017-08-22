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

package org.openyolo.api.persistence.internal;

/**
 * Factory for creating settings.
 */
interface SettingsFactory {

    /**
     * Returns a new {@link BooleanSetting} based on the given key and default value.
     * @param key a unique identifier that acts as the primary key of the setting.
     * @param defaultValue the default value of the setting.
     */
    BooleanSetting makeBoolean(String key, boolean defaultValue);

    /**
     * A boolean setting.
     */
    interface BooleanSetting {

        /**
         * Returns the current value if one has been set, otherwise the default value.
         */
        boolean get();

        /**
         * Sets the current value of the setting to the given value.
         */
        void set(boolean value);
    }
}
