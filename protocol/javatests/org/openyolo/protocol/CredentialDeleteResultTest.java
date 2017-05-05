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
    public void fromResultIntentData() throws IOException {
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

    @Test
    public void fromProtobuf() {
        Protobufs.CredentialDeleteResult proto = Protobufs.CredentialDeleteResult.newBuilder()
                .setResultCode(ResultCode.BAD_REQUEST)
                .putAdditionalProps(ADDITIONAL_KEY, ByteString.copyFrom(ADDITIONAL_VALUE))
                .build();

        CredentialDeleteResult result = CredentialDeleteResult.fromProtobuf(proto);
        assertThat(result.getResultCode()).isEqualTo(CredentialDeleteResult.CODE_BAD_REQUEST);
        checkAdditionalProps(result.getAdditionalProperties());
    }

    @Test
    public void fromProtobufBytes() throws IOException {
        byte[] protoBytes = Protobufs.CredentialDeleteResult.newBuilder()
                .setResultCode(ResultCode.BAD_REQUEST)
                .putAdditionalProps(ADDITIONAL_KEY, ByteString.copyFrom(ADDITIONAL_VALUE))
                .build()
                .toByteArray();

        CredentialDeleteResult result = CredentialDeleteResult.fromProtobufBytes(protoBytes);
        assertThat(result.getResultCode()).isEqualTo(CredentialDeleteResult.CODE_BAD_REQUEST);
        checkAdditionalProps(result.getAdditionalProperties());
    }
}
