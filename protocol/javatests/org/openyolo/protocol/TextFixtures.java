/*
 * Copyright 2017 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openyolo.protocol;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.google.protobuf.ByteString;
import java.util.HashMap;
import java.util.Map;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.CollectionConverter;

/** Collection of shared test constants */
public final class TextFixtures {
    public static final class ValidProperties {
        public static final String PROPERTY_A_NAME = "Property A";
        public static final byte[] PROPERTY_A_VALUE = new byte [] { 0, 1 };

        public static final String PROPERTY_B_NAME = "Property B";
        public static final byte[] PROPERTY_B_VALUE = new byte [] { 2, 2 };

        public static final Map<String, byte[]> MAP_INSTANCE;

        static {
            MAP_INSTANCE = new HashMap<>();
            MAP_INSTANCE.put(PROPERTY_A_NAME, PROPERTY_A_VALUE);
            MAP_INSTANCE.put(PROPERTY_B_NAME, PROPERTY_B_VALUE);
        }

        public static void assertEqualTo(Map<String, ByteString> map, boolean dummy) {
            Map<String, byte[]> byteMap =
                    CollectionConverter.convertMapValues(
                            map,
                            ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY);

            assertEqualTo(byteMap);
        }

        public static void assertEqualTo(Map<String, byte[]> map) {
            assertThat(map).hasSize(2);
            assertThat(map.get(PROPERTY_A_NAME)).isEqualTo(PROPERTY_A_VALUE);
            assertThat(map.get(PROPERTY_B_NAME)).isEqualTo(PROPERTY_B_VALUE);
        }
    }
}
