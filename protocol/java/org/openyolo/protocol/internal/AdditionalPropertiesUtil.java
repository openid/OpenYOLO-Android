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

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.openyolo.protocol.internal.CustomMatchers.notNullOrEmptyString;
import static org.valid4j.Validation.validate;

import android.support.annotation.Nullable;
import com.google.protobuf.ByteString;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for the conversion and manipulation of additional property maps.
 */
public final class AdditionalPropertiesUtil {

    /**
     * Ensures that the provided map contains no null or empty keys, and no null values.
     * Treats a null map as an empty map.
     */
    public static Map<String, ByteString> validateAdditionalProperties(
            @Nullable Map<String, byte[]> additionalProps) {
        if (additionalProps == null) {
            return new HashMap<>();
        }

        validate(
                additionalProps.keySet(),
                everyItem(notNullOrEmptyString()),
                IllegalArgumentException.class);
        validate(
                additionalProps.values(),
                everyItem(notNullValue()),
                IllegalArgumentException.class);

        return CollectionConverter.convertMapValues(
                additionalProps,
                ByteStringConverters.BYTE_ARRAY_TO_BYTE_STRING);
    }

    /**
     * Ensures that the provided protocol buffer sourced map contains no null or empty keys, and
     * no null values. Treats a null map as an empty map.
     */
    public static Map<String, ByteString> validateAdditionalPropertiesFromProto(
            @Nullable Map<String, ByteString> additionalProps) {
        if (additionalProps == null) {
            return new HashMap<>();
        }

        validate(
                additionalProps.keySet(),
                everyItem(notNullOrEmptyString()),
                IllegalArgumentException.class);
        validate(
                additionalProps.values(),
                everyItem(notNullValue()),
                IllegalArgumentException.class);

        return additionalProps;
    }

    private AdditionalPropertiesUtil() {
        throw new IllegalStateException("not intended to be constructed");
    }
}
