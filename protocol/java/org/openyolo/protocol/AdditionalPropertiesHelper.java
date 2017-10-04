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

import android.support.annotation.Nullable;
import java.nio.charset.Charset;

/**
 * Utility methods for reading and writing additional properties.
 */
public final class AdditionalPropertiesHelper {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * Encodes the provided string for use as an additional property, using UTF-8 encoding.
     * If the provided string is `null`, then `null will be returned.
     */
    @Nullable
    public static byte[] encodeStringValue(@Nullable String value) {
        if (value == null) {
            return null;
        }

        return value.getBytes(UTF_8);
    }

    /**
     * Decodes the provided byte array from an additional property value to a String. This
     * assumes that the byte array was originally produced by {@link #encodeStringValue(String)}
     * or equivalent code. If the provided byte array is `null`, then `null` will be returned.
     */
    @Nullable
    public static String decodeStringValue(@Nullable byte[] value) {
        if (value == null) {
            return null;
        }

        return new String(value, UTF_8);
    }

    private AdditionalPropertiesHelper() {
        throw new IllegalStateException("not intended to be constructed");
    }
}
