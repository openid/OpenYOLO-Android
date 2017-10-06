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
import static org.openyolo.protocol.TestConstants.INVALID_PROTO_BYTES;

import android.content.Intent;
import com.google.protobuf.ByteString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Tests for {@link CredentialDeleteResult}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CredentialDeleteResultTest {

    // invalid as empty string keys are not permitted
    private static final Protobufs.CredentialDeleteResult INVALID_PROTO =
            Protobufs.CredentialDeleteResult.newBuilder()
                    .putAdditionalProps("", ByteString.copyFrom(new byte[0]))
                    .build();

    private static final class ValidDeleteResult {
        public static CredentialDeleteResult make() {
            return new CredentialDeleteResult.Builder(
                    CredentialDeleteResult.CODE_DELETED)
                    .setAdditionalProperties(TestConstants.ValidAdditionalProperties.make())
                    .build();
        }

        public static void assertEquals(CredentialDeleteResult result) {
            assertThat(result.getResultCode()).isEqualTo(CredentialDeleteResult.CODE_DELETED);
            TestConstants.ValidAdditionalProperties.assertEquals(result.getAdditionalProperties());
        }
    }

    @Test
    public void build() {
        CredentialDeleteResult result = ValidDeleteResult.make();

        ValidDeleteResult.assertEquals(result);
    }

    @Test
    public void toProtobuf_fromProtobuf_isEquivalent() throws Exception {
        Protobufs.CredentialDeleteResult protobuf = ValidDeleteResult.make().toProtobuf();
        CredentialDeleteResult result = CredentialDeleteResult.fromProtobuf(protobuf);

        ValidDeleteResult.assertEquals(result);
    }

    @Test
    public void toResultIntentData_fromResultIntentData_isEquivalent() throws Exception {
        Intent resultData = ValidDeleteResult.make().toResultDataIntent();
        CredentialDeleteResult result = CredentialDeleteResult.fromResultIntentData(resultData);

        ValidDeleteResult.assertEquals(result);
    }

    @Test(expected=MalformedDataException.class)
    public void fromResultIntentData_nullIntent_throwsException() throws Exception {
        CredentialDeleteResult.fromResultIntentData(null);
    }

    @Test(expected=MalformedDataException.class)
    public void fromResultIntentData_missingExtra_throwsException() throws Exception {
        CredentialDeleteResult.fromResultIntentData(new Intent());
    }

    @Test(expected=MalformedDataException.class)
    public void fromResultIntentData_wrongExtraType_throwsException() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ProtocolConstants.EXTRA_DELETE_RESULT, "notAByteArray");
        CredentialDeleteResult.fromResultIntentData(intent);
    }

    @Test(expected=MalformedDataException.class)
    public void fromResultIntentData_invalidProtoBytes_throwsException() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ProtocolConstants.EXTRA_DELETE_RESULT, INVALID_PROTO_BYTES);
        CredentialDeleteResult.fromResultIntentData(intent);
    }

    @Test(expected=MalformedDataException.class)
    public void fromResultIntentData_invalidProto_throwsException() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ProtocolConstants.EXTRA_DELETE_RESULT, INVALID_PROTO.toByteArray());

        CredentialDeleteResult.fromResultIntentData(intent);
    }

    @Test(expected=MalformedDataException.class)
    public void fromProtobuf_invalidProtoData_throwsException() throws Exception {
        CredentialDeleteResult.fromProtobuf(INVALID_PROTO);
    }

    @Test
    public void fromProtobufBytes() throws Exception {
        byte[] protoBytes = ValidDeleteResult.make().toProtobuf().toByteArray();

        CredentialDeleteResult result = CredentialDeleteResult.fromProtobufBytes(protoBytes);
        ValidDeleteResult.assertEquals(result);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = MalformedDataException.class)
    public void fromProtobufBytes_nullArray_throwsException() throws Exception {
        CredentialDeleteResult.fromProtobufBytes(null);
    }

    @Test(expected = MalformedDataException.class)
    public void fromProtobufBytes_invalidProtoBytes_throwsException() throws Exception {
        CredentialDeleteResult.fromProtobufBytes(INVALID_PROTO_BYTES);
    }

    @Test(expected = MalformedDataException.class)
    public void fromProtobufBytes_invalidProto_throwsException() throws Exception {
        CredentialDeleteResult.fromProtobufBytes(INVALID_PROTO.toByteArray());
    }
}
