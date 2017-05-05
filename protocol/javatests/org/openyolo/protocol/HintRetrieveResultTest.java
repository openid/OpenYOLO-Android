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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.Protobufs.HintRetrieveResult.ResultCode;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class HintRetrieveResultTest {

    private static final String ALICE_ID = "alice@example.com";
    private static final Hint HINT =
            new Hint.Builder(
                    ALICE_ID,
                    AuthenticationMethods.EMAIL)
                    .build();

    @Test
    public void build() {

        HintRetrieveResult result = new HintRetrieveResult.Builder(
                HintRetrieveResult.CODE_HINT_SELECTED)
                .setHint(HINT)
                .build();

        assertThat(result.getResultCode()).isEqualTo(HintRetrieveResult.CODE_HINT_SELECTED);
        assertThat(result.getHint()).isSameAs(HINT);
        assertThat(result.getAdditionalProperties()).isEmpty();
    }

    @Test
    public void buildSetHint_nullValue() {
        HintRetrieveResult result = new HintRetrieveResult.Builder(
                HintRetrieveResult.CODE_NO_HINTS_AVAILABLE)
                .setHint(null)
                .build();

        assertThat(result.getHint()).isNull();
    }

    @Test
    public void buildSetAdditionalProperties() {
        HintRetrieveResult result = new HintRetrieveResult.Builder(
                HintRetrieveResult.CODE_NO_HINTS_AVAILABLE)
                .setAdditionalProperties(ADDITIONAL_PROPS)
                .build();

        assertThat(result.getAdditionalProperties()).hasSize(1);
        checkAdditionalProps(result.getAdditionalProperties());
    }

    @Test
    public void toProtobuf() {
        HintRetrieveResult result = new HintRetrieveResult.Builder(
                HintRetrieveResult.CODE_HINT_SELECTED)
                .setHint(HINT)
                .setAdditionalProperties(ADDITIONAL_PROPS)
                .build();

        Protobufs.HintRetrieveResult proto = result.toProtobuf();
        assertThat(proto.getResultCodeValue()).isEqualTo(ResultCode.HINT_SELECTED_VALUE);
        assertThat(proto.hasHint());
        checkAdditionalPropsFromProto(proto.getAdditionalPropsMap());
    }

    @Test
    public void fromProtobuf() {
        Protobufs.HintRetrieveResult proto = Protobufs.HintRetrieveResult.newBuilder()
                .setResultCode(ResultCode.HINT_SELECTED)
                .setHint(Protobufs.Hint.newBuilder()
                        .setId(ALICE_ID)
                        .setAuthMethod(AuthenticationMethods.EMAIL.toProtobuf()))
                .putAdditionalProps(ADDITIONAL_KEY, ByteString.copyFrom(ADDITIONAL_VALUE))
                .build();

        HintRetrieveResult result = HintRetrieveResult.fromProtobuf(proto);
        assertThat(result.getResultCode()).isEqualTo(HintRetrieveResult.CODE_HINT_SELECTED);
        assertThat(result.getHint()).isNotNull();
        checkAdditionalProps(result.getAdditionalProperties());
    }

    @Test
    public void toResultIntentData() throws IOException {
        HintRetrieveResult result = new HintRetrieveResult.Builder(
                HintRetrieveResult.CODE_HINT_SELECTED)
                .setHint(HINT)
                .setAdditionalProperties(ADDITIONAL_PROPS)
                .build();

        Intent intent = result.toResultDataIntent();
        assertThat(intent.hasExtra(ProtocolConstants.EXTRA_HINT_RESULT));
        byte[] data = intent.getByteArrayExtra(ProtocolConstants.EXTRA_HINT_RESULT);

        HintRetrieveResult readResult = HintRetrieveResult.fromProtobufBytes(data);
        assertThat(readResult.getResultCode()).isEqualTo(HintRetrieveResult.CODE_HINT_SELECTED);
        assertThat(readResult.getHint()).isNotNull();
        checkAdditionalProps(readResult.getAdditionalProperties());
    }
}
