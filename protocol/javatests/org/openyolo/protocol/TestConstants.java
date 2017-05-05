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

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.google.protobuf.ByteString;
import java.util.HashMap;
import java.util.Map;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.CollectionConverter;

/**
 * Useful constants and associated verification methods for tests.
 */
public final class TestConstants {

    public static final String ADDITIONAL_KEY = "extra";
    public static final byte[] ADDITIONAL_VALUE = "value".getBytes();
    public static final Map<String, byte[]> ADDITIONAL_PROPS;

    static {
        Map<String, byte[]> additionalProps = new HashMap<>();
        additionalProps.put(ADDITIONAL_KEY, ADDITIONAL_VALUE);
        ADDITIONAL_PROPS = additionalProps;
    }

    public static void checkAdditionalProps(Map<String, byte[]> additionalProps) {
        assertThat(additionalProps).isNotNull();
        assertThat(additionalProps).hasSize(1);
        assertThat(additionalProps).containsKey(ADDITIONAL_KEY);
        assertThat(additionalProps.get(ADDITIONAL_KEY)).isEqualTo(ADDITIONAL_VALUE);
    }

    public static void checkAdditionalPropsFromProto(Map<String, ByteString> additionalProps) {
        checkAdditionalProps(CollectionConverter.convertMapValues(
                additionalProps,
                ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY));
    }
}
