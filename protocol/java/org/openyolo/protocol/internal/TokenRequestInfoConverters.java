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

import org.openyolo.protocol.Protobufs;
import org.openyolo.protocol.TokenRequestInfo;

/**
 * ValueConverters for {@link TokenRequestInfo}.
 */
public final class TokenRequestInfoConverters {

    /**
     * Converts token request info protocol buffers into their validated, public API form.
     */
    public static final ValueConverter<Protobufs.TokenRequestInfo, TokenRequestInfo>
            PROTOBUF_TO_OBJECT = new ProtobufToObjectConverter();

    /**
     * Converts token request info values into their protocol buffer equivalent.
     */
    public static final ValueConverter<TokenRequestInfo, Protobufs.TokenRequestInfo>
            OBJECT_TO_PROTOBUF = new ObjectToProtobufConverter();

    private TokenRequestInfoConverters() {
        throw new IllegalStateException("not intended to be constructed");
    }

    private static final class ProtobufToObjectConverter
            implements ValueConverter<Protobufs.TokenRequestInfo, TokenRequestInfo> {
        @Override
        public TokenRequestInfo convert(Protobufs.TokenRequestInfo proto) {
            return new TokenRequestInfo.Builder(proto).build();
        }
    }

    private static final class ObjectToProtobufConverter
            implements ValueConverter<TokenRequestInfo, Protobufs.TokenRequestInfo> {
        @Override
        public Protobufs.TokenRequestInfo convert(TokenRequestInfo value) {
            return value.toProtobuf();
        }
    }

}
