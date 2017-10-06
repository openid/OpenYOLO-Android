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
 * The contract that builders must adhere to if they build model types that include
 * additional properties.
 */
public interface AdditionalPropertiesBuilder<
        ModelT extends AdditionalPropertiesContainer,
        BuilderT extends AdditionalPropertiesBuilder<ModelT, BuilderT>> {

    /**
     * Specifies any additional, non-standard properties associated with the credential.
     */
    @NonNull
    BuilderT setAdditionalProperties(@Nullable Map<String, byte[]> additionalProps);

    /**
     * Specifies an additional, non-standard property to include in the credential.
     */
    @NonNull
    BuilderT setAdditionalProperty(@NonNull String key, @Nullable byte[] value);

    /**
     * Specifies an additional, non-standard property with a string value to include in the
     * credential.
     */
    @NonNull
    BuilderT setAdditionalPropertyAsString(@NonNull String key, @Nullable String value);

    /**
     * Creates an instance of the model type.
     */
    @NonNull
    ModelT build();
}
