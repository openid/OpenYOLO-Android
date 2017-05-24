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

import org.openyolo.protocol.AuthenticationMethod;
import org.openyolo.protocol.Protobufs;

/**
 * Implementations of {@link ValueConverter} related to authentication methods.
 */
public final class AuthenticationMethodConverters {

    /**
     * Creates the protocol buffer equivalent of an {@link AuthenticationMethod}.
     */
    public static final ValueConverter<AuthenticationMethod, Protobufs.AuthenticationMethod>
            OBJECT_TO_PROTOBUF = new ObjectToProtobufConverter();

    /**
     * Creates an {@link AuthenticationMethod} from its protocol buffer equivalent.
     */
    public static final ValueConverter<Protobufs.AuthenticationMethod, AuthenticationMethod>
            PROTOBUF_TO_OBJECT = new ProtobufToObjectConverter();

    private static final class ObjectToProtobufConverter
            implements ValueConverter<AuthenticationMethod,Protobufs.AuthenticationMethod> {

        @Override
        public Protobufs.AuthenticationMethod convert(AuthenticationMethod value) {
            return value.toProtobuf();
        }
    }

    private static final class ProtobufToObjectConverter
            implements ValueConverter<Protobufs.AuthenticationMethod, AuthenticationMethod> {

        @Override
        public AuthenticationMethod convert(Protobufs.AuthenticationMethod value) {
            return new AuthenticationMethod(value.getUri());
        }
    }
}
