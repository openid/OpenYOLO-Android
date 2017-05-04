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

import org.openyolo.protocol.Credential;
import org.openyolo.protocol.Protobufs;

/**
 * Converts credential instances to and from their protocol buffer equivalent.
 */
public final class CredentialConverter {

    /**
     * Converts credential instances to their protocol buffer equivalent.
     */
    public static final ValueConverter<Credential, Protobufs.Credential> CREDENTIAL_TO_PROTO =
            new CredentialToProtoConverter();

    /**
     * Converts credential instances from their protocol buffer equivalent.
     */
    public static final ValueConverter<Protobufs.Credential, Credential> PROTO_TO_CREDENTIAL =
            new ProtoToCredentialConverter();

    private static final class CredentialToProtoConverter
            implements ValueConverter<Credential, Protobufs.Credential> {
        @Override
        public Protobufs.Credential convert(Credential value) {
            return value.toProtobuf();
        }
    }

    private static final class ProtoToCredentialConverter
            implements ValueConverter<Protobufs.Credential, Credential> {
        @Override
        public Credential convert(Protobufs.Credential value) {
            return Credential.fromProtobuf(value);
        }
    }
}
