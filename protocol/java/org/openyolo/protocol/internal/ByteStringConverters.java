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

import com.google.protobuf.ByteString;

/**
 * Instances of {@link ValueConverter} related to {@link com.google.protobuf.ByteString}
 * instances.
 */
public final class ByteStringConverters {

    /**
     * Extracts byte arrays from {@link ByteString} instances.
     */
    public static final ValueConverter<ByteString, byte[]> BYTE_STRING_TO_BYTE_ARRAY
            = new ByteStringToByteArrayConverter();

    /**
     * Copies byte arrays into {@link ByteString} instances.
     */
    public static final ValueConverter<byte[], ByteString> BYTE_ARRAY_TO_BYTE_STRING
            = new ByteArrayToByteStringConverter();

    private static final class ByteStringToByteArrayConverter
            implements ValueConverter<ByteString, byte[]> {
        @Override
        public byte[] convert(ByteString value) {
            return value.toByteArray();
        }
    }

    private static final class ByteArrayToByteStringConverter
            implements ValueConverter<byte[], ByteString> {
        @Override
        public ByteString convert(byte[] value) {
            return ByteString.copyFrom(value);
        }
    }
}
