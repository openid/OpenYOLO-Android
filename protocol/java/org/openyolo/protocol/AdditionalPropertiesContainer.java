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

package org.openyolo.protocol;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Map;

/**
 * Implemented by all model types that can hold additional, non-standard properties.
 */
public interface AdditionalPropertiesContainer {

    /**
     * The set of additional, non-standard properties.
     */
    @NonNull
    Map<String, byte[]> getAdditionalProperties();

    /**
     * Returns the additional, non-standard property identified by the specified key. If this
     * additional property does not exist, then `null` is returned.
     */
    @Nullable
    byte[] getAdditionalProperty(String key);

    /**
     * Returns the additional, non-standard property identified by the specified key, where the
     * value is assumed to be a UTF-8 encoded string. If this additional property does not exist,
     * then `null` is returned.
     */
    @Nullable
    String getAdditionalPropertyAsString(String key);
}
