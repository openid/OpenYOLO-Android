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

package org.openyolo.demoprovider.barbican.provider;

import java.util.ArrayList;
import java.util.List;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.MalformedDataException;
import org.openyolo.protocol.Protobufs;

/**
 * Collection of utility methods related to {@link org.openyolo.protocol.Protobufs.Credential}.
 */
public final class CredentialTransformUtils {

    /**
     * Given a list of {@link Protobufs.Credential} returns an equivalent list of
     * {@link Credential}.
     * @throws MalformedDataException if a given {@link Protobufs.Credential} can not be
     *     successfully transformed into it's object representation.
     */
    public static List<Credential> fromProtoList(List<Protobufs.Credential> protoCredentials)
            throws MalformedDataException {
        List<Credential> credentials = new ArrayList<>(protoCredentials.size());
        for (Protobufs.Credential protoCredential : protoCredentials) {
            credentials.add(Credential.fromProtobuf(protoCredential));
        }

        return credentials;
    }

    /**
     * Given a list {@link Credential} returns an equivalent list of {@link Protobufs.Credential}.
     */
    public static List<Protobufs.Credential> toProtoList(List<Credential> credentials) {
        List<Protobufs.Credential> protoCredentials = new ArrayList<>(credentials.size());
        for (Credential credential : credentials) {
            protoCredentials.add(credential.toProtobuf());
        }

        return protoCredentials;
    }
}
