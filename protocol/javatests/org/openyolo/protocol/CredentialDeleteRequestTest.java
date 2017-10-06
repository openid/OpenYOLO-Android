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

import static org.openyolo.protocol.TestConstants.INVALID_PROTO_BYTES;

import android.content.Intent;
import com.google.protobuf.ByteString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.TestConstants.ValidAdditionalProperties;
import org.openyolo.protocol.TestConstants.ValidFacebookCredential;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

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

    private static final class ValidDeleteRequest {
        public static final CredentialDeleteRequest make() {
            return new CredentialDeleteRequest.Builder(ValidFacebookCredential.make())
                            .setAdditionalProperties(ValidAdditionalProperties.make())
                            .build();
        }

        public static void assertEquals(CredentialDeleteRequest request) {
            ValidFacebookCredential.assertEqualTo(request.getCredential());
            ValidAdditionalProperties.assertEquals(request.getAdditionalProperties());
        }
    }

    @Test
    public void build() {
        CredentialDeleteRequest request = ValidDeleteRequest.make();

        ValidDeleteRequest.assertEquals(request);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void build_nullCredential_throwsIllegalArgumentException() {
        new CredentialDeleteRequest.Builder(null).build();
    }

    @Test
    public void fromProtobuf() throws Exception {
        Protobufs.CredentialDeleteRequest proto = ValidDeleteRequest.make().toProtobuf();

        CredentialDeleteRequest request = CredentialDeleteRequest.fromProtobuf(proto);

        ValidDeleteRequest.assertEquals(request);
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
        Protobufs.CredentialDeleteRequest proto = ValidDeleteRequest.make().toProtobuf();
        CredentialDeleteRequest request =
                CredentialDeleteRequest.fromProtobufBytes(proto.toByteArray());

        ValidDeleteRequest.assertEquals(request);
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
    public void fromRequestIntent() throws Exception {
        byte[] protoBytes = ValidDeleteRequest.make().toProtobuf().toByteArray();
        Intent requestIntent = new Intent();
        requestIntent.putExtra(ProtocolConstants.EXTRA_DELETE_REQUEST, protoBytes);

        CredentialDeleteRequest request = CredentialDeleteRequest.fromRequestIntent(requestIntent);

        ValidDeleteRequest.assertEquals(request);
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
