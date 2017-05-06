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
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.Protobufs.CredentialDeleteResult.ResultCode;
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

    private static final byte[] INVALID_PROTO_BYTES =
            new byte[] { 1, 2, 3, 4, 5 };

    @Test
    public void build() {
        CredentialDeleteResult result = new CredentialDeleteResult.Builder(
                CredentialDeleteResult.CODE_DELETED)
                .setAdditionalProperties(ADDITIONAL_PROPS)
                .build();

        assertThat(result.getResultCode()).isEqualTo(CredentialDeleteResult.CODE_DELETED);
        checkAdditionalProps(result.getAdditionalProperties());
    }

    @Test
    public void toProtobuf() {
        Protobufs.CredentialDeleteResult result = new CredentialDeleteResult.Builder(
                CredentialDeleteResult.CODE_DELETED)
                .setAdditionalProperties(ADDITIONAL_PROPS)
                .build()
                .toProtobuf();

        assertThat(result.getResultCodeValue()).isEqualTo(
                Protobufs.CredentialDeleteResult.ResultCode.DELETED_VALUE);
        checkAdditionalPropsFromProto(result.getAdditionalPropsMap());
    }

    @Test
    public void toResultIntentData() throws InvalidProtocolBufferException {
        Intent resultData = new CredentialDeleteResult.Builder(
                CredentialDeleteResult.CODE_DELETED)
                .setAdditionalProperties(ADDITIONAL_PROPS)
                .build()
                .toResultIntentData();

        assertThat(resultData.hasExtra(ProtocolConstants.EXTRA_DELETE_RESULT)).isTrue();

        byte[] resultBytes = resultData.getByteArrayExtra(ProtocolConstants.EXTRA_DELETE_RESULT);
        Protobufs.CredentialDeleteResult result =
                Protobufs.CredentialDeleteResult.parseFrom(resultBytes);

        assertThat(result.getResultCodeValue()).isEqualTo(ResultCode.DELETED_VALUE);
        checkAdditionalPropsFromProto(result.getAdditionalPropsMap());
    }

    @Test
    public void fromResultIntentData() throws Exception {
        byte[] resultBytes = Protobufs.CredentialDeleteResult.newBuilder()
                .setResultCode(ResultCode.USER_REFUSED)
                .putAdditionalProps(ADDITIONAL_KEY, ByteString.copyFrom(ADDITIONAL_VALUE))
                .build()
                .toByteArray();

        Intent resultData = new Intent();
        resultData.putExtra(ProtocolConstants.EXTRA_DELETE_RESULT, resultBytes);

        CredentialDeleteResult result = CredentialDeleteResult.fromResultIntentData(resultData);
        assertThat(result.getResultCode()).isEqualTo(CredentialDeleteResult.CODE_USER_REFUSED);
        checkAdditionalProps(result.getAdditionalProperties());
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

    @Test
    public void fromProtobuf() throws Exception {
        Protobufs.CredentialDeleteResult proto = Protobufs.CredentialDeleteResult.newBuilder()
                .setResultCode(ResultCode.BAD_REQUEST)
                .putAdditionalProps(ADDITIONAL_KEY, ByteString.copyFrom(ADDITIONAL_VALUE))
                .build();

        CredentialDeleteResult result = CredentialDeleteResult.fromProtobuf(proto);
        assertThat(result.getResultCode()).isEqualTo(CredentialDeleteResult.CODE_BAD_REQUEST);
        checkAdditionalProps(result.getAdditionalProperties());
    }

    @Test(expected=MalformedDataException.class)
    public void fromProtobuf_invalidProtoData_throwsException() throws Exception {
        CredentialDeleteResult.fromProtobuf(INVALID_PROTO);
    }

    @Test
    public void fromProtobufBytes() throws Exception {
        byte[] protoBytes = Protobufs.CredentialDeleteResult.newBuilder()
                .setResultCode(ResultCode.BAD_REQUEST)
                .putAdditionalProps(ADDITIONAL_KEY, ByteString.copyFrom(ADDITIONAL_VALUE))
                .build()
                .toByteArray();

        CredentialDeleteResult result = CredentialDeleteResult.fromProtobufBytes(protoBytes);
        assertThat(result.getResultCode()).isEqualTo(CredentialDeleteResult.CODE_BAD_REQUEST);
        checkAdditionalProps(result.getAdditionalProperties());
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
