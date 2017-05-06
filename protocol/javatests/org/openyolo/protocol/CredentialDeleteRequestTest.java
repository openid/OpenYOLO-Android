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
import static org.openyolo.protocol.TestConstants.ADDITIONAL_KEY;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROPS;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_VALUE;
import static org.openyolo.protocol.TestConstants.checkAdditionalProps;
import static org.openyolo.protocol.TestConstants.checkAdditionalPropsFromProto;

import android.content.Intent;
import com.google.protobuf.ByteString;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.valid4j.errors.RequireViolation;

/**
 * Tests for {@link CredentialDeleteRequest}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CredentialDeleteRequestTest {

    // invalid as empty string keys are not permitted
    private static final Protobufs.CredentialDeleteRequest INVALID_PROTO =
            Protobufs.CredentialDeleteRequest.newBuilder()
                    .putAdditionalProps("", ByteString.copyFrom(new byte[0]))
                    .build();

    private static final byte[] INVALID_PROTO_BYTES =
            new byte[] { 1, 2, 3, 4, 5 };

    private static final Credential CREDENTIAL =
            new Credential.Builder(
                    "alice@example.com",
                    AuthenticationMethods.EMAIL,
                    new AuthenticationDomain("https://example.com"))
            .build();

    @Test
    public void build() {
        CredentialDeleteRequest request =
                new CredentialDeleteRequest.Builder(CREDENTIAL)
                        .setAdditionalProperties(ADDITIONAL_PROPS)
                        .build();

        assertThat(request.getCredential()).isNotNull();
        checkAdditionalProps(request.getAdditionalProps());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void build_nullCredential_throwsException() {
        new CredentialDeleteRequest.Builder((Credential) null).build();
    }

    @Test
    public void fromProtobuf() throws Exception {
        Protobufs.CredentialDeleteRequest proto = Protobufs.CredentialDeleteRequest.newBuilder()
                .setCredential(CREDENTIAL.toProtobuf())
                .putAdditionalProps(ADDITIONAL_KEY, ByteString.copyFrom(ADDITIONAL_VALUE))
                .build();

        CredentialDeleteRequest request = CredentialDeleteRequest.fromProtobuf(proto);
        assertThat(request.getCredential()).isNotNull();
        checkAdditionalProps(request.getAdditionalProps());
    }

    @Test(expected = MalformedDataException.class)
    public void fromProtobuf_nullProto_throwsException() throws Exception {
        CredentialDeleteRequest.fromProtobuf(null);
    }

    @Test(expected = MalformedDataException.class)
    public void fromProtobuf_invalidProto_throwsException() throws Exception {
        CredentialDeleteRequest.fromProtobuf(INVALID_PROTO);
    }

    @Test
    public void fromProtobufBytes() throws Exception {
        Protobufs.CredentialDeleteRequest proto = Protobufs.CredentialDeleteRequest.newBuilder()
                .setCredential(CREDENTIAL.toProtobuf())
                .putAdditionalProps(ADDITIONAL_KEY, ByteString.copyFrom(ADDITIONAL_VALUE))
                .build();

        CredentialDeleteRequest request =
                CredentialDeleteRequest.fromProtobufBytes(proto.toByteArray());
        assertThat(request.getCredential()).isNotNull();
        checkAdditionalProps(request.getAdditionalProps());
    }

    @Test(expected = MalformedDataException.class)
    public void fromProtobufBytes_nullArray_throwsException() throws Exception {
        CredentialDeleteRequest.fromProtobufBytes(null);
    }

    @Test(expected = MalformedDataException.class)
    public void fromProtobufBytes_invalidProtoBytes_throwsException() throws Exception {
        CredentialDeleteRequest.fromProtobufBytes(INVALID_PROTO_BYTES);
    }

    @Test(expected = MalformedDataException.class)
    public void fromProtobufBytes_invalidProto_throwsException() throws Exception {
        CredentialDeleteRequest.fromProtobufBytes(INVALID_PROTO.toByteArray());
    }

    @Test
    public void toProtobuf() {
        Protobufs.CredentialDeleteRequest proto =
                new CredentialDeleteRequest.Builder(CREDENTIAL)
                        .setAdditionalProperties(ADDITIONAL_PROPS)
                        .build()
                .toProtobuf();

        assertThat(proto.hasCredential());
        checkAdditionalPropsFromProto(proto.getAdditionalPropsMap());
    }

    @Test
    public void fromRequestIntent() throws Exception {
        byte[] protoBytes =
                new CredentialDeleteRequest.Builder(CREDENTIAL)
                        .setAdditionalProperties(ADDITIONAL_PROPS)
                        .build()
                        .toProtobuf()
                        .toByteArray();

        Intent requestIntent = new Intent();
        requestIntent.putExtra(ProtocolConstants.EXTRA_DELETE_REQUEST, protoBytes);

        CredentialDeleteRequest request = CredentialDeleteRequest.fromRequestIntent(requestIntent);

        assertThat(request.getCredential()).isNotNull();
        checkAdditionalProps(request.getAdditionalProps());
    }

    @Test(expected = MalformedDataException.class)
    public void fromRequestIntent_nullIntent_throwsException() throws Exception {
        CredentialDeleteRequest.fromRequestIntent(null);
    }

    @Test(expected = MalformedDataException.class)
    public void fromRequestIntent_missingExtra_throwsException() throws Exception {
        Intent intent = new Intent();
        CredentialDeleteRequest.fromRequestIntent(intent);
    }

    @Test(expected = MalformedDataException.class)
    public void fromRequestIntent_wrongExtraType_throwsException() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ProtocolConstants.EXTRA_DELETE_REQUEST, "notAByteArray");
        CredentialDeleteRequest.fromRequestIntent(intent);
    }

    @Test(expected = MalformedDataException.class)
    public void fromRequestIntent_invalidProtoBytes_throwsException() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ProtocolConstants.EXTRA_DELETE_REQUEST, INVALID_PROTO_BYTES);
        CredentialDeleteRequest.fromRequestIntent(intent);
    }

    @Test(expected = MalformedDataException.class)
    public void fromRequestIntent_invalidProto_throwsException() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ProtocolConstants.EXTRA_DELETE_REQUEST, INVALID_PROTO.toByteArray());
        CredentialDeleteRequest.fromRequestIntent(intent);
    }
}
