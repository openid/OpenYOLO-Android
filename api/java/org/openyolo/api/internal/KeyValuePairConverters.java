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

package org.openyolo.api.internal;

import android.util.Pair;
import java.util.Collection;
import java.util.Map;
import okio.ByteString;
import org.openyolo.proto.KeyValuePair;

/**
 * Utility methods for converting between the {@link KeyValuePair} type used in OpenYOLO protocol
 * buffers and {@link Pair} as a more generic equivalent.
 */
public final class KeyValuePairConverters {

    private KeyValuePairConverters() {
        throw new IllegalStateException("not intended to be constructed");
    }

    /**
     * Converts {@link KeyValuePair} instances to {@link Pair} instances, typically for use by
     * {@link CollectionConverter#toMap(Collection, ValueConverter)}.
     */
    public static final ValueConverter<KeyValuePair, Pair<String, byte[]>> CONVERTER_KVP_TO_PAIR =
            new ValueConverter<KeyValuePair, Pair<String, byte[]>>() {
                @Override
                public Pair<String, byte[]> convert(KeyValuePair value) {
                    return new Pair<>(value.name, value.value.toByteArray());
                }
            };

    /**
     * Converts {@link java.util.Map.Entry} instances to {@link KeyValuePair} instances.
     */
    public static final ValueConverter<Map.Entry<String, byte[]>, KeyValuePair>
            CONVERTER_ENTRY_TO_KVP =
            new ValueConverter<Map.Entry<String, byte[]>, KeyValuePair>() {
                @Override
                public KeyValuePair convert(Map.Entry<String, byte[]> value) {
                    return new KeyValuePair(value.getKey(), ByteString.of(value.getValue()));
                }
            };
}
