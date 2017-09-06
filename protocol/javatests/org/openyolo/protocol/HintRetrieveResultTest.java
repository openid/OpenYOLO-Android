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
import static org.openyolo.protocol.TestConstants.ValidAdditionalProperties;

import android.content.Intent;
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
    public void fromProtobuf_withDefaultInstanceOfHint_doesNotThrowException() throws Exception {
        Protobufs.HintRetrieveResult hintRetrieveResult =
                Protobufs.HintRetrieveResult.newBuilder().build();

        HintRetrieveResult.fromProtobuf(hintRetrieveResult);
    }

    @Test
    public void buildSetAdditionalProperties() {
        HintRetrieveResult result = new HintRetrieveResult.Builder(
                HintRetrieveResult.CODE_NO_HINTS_AVAILABLE)
                .setAdditionalProperties(ValidAdditionalProperties.make())
                .build();

        assertThat(result.getAdditionalProperties()).hasSize(1);
        ValidAdditionalProperties.assertEquals(result.getAdditionalProperties());
    }

    @Test
    public void toProtobuf() {
        HintRetrieveResult result = new HintRetrieveResult.Builder(
                HintRetrieveResult.CODE_HINT_SELECTED)
                .setHint(HINT)
                .setAdditionalProperties(ValidAdditionalProperties.make())
                .build();

        Protobufs.HintRetrieveResult proto = result.toProtobuf();
        assertThat(proto.getResultCodeValue()).isEqualTo(ResultCode.HINT_SELECTED_VALUE);
        assertThat(proto.hasHint());
        ValidAdditionalProperties.assertEqualsForProto(proto.getAdditionalPropsMap());
    }

    @Test
    public void fromProtobuf() throws Exception {
        Protobufs.HintRetrieveResult proto = Protobufs.HintRetrieveResult.newBuilder()
                .setResultCode(ResultCode.HINT_SELECTED)
                .setHint(Protobufs.Hint.newBuilder()
                        .setId(ALICE_ID)
                        .setAuthMethod(AuthenticationMethods.EMAIL.toProtobuf()))
                .putAllAdditionalProps(ValidAdditionalProperties.makeForProto())
                .build();

        HintRetrieveResult result = HintRetrieveResult.fromProtobuf(proto);
        assertThat(result.getResultCode()).isEqualTo(HintRetrieveResult.CODE_HINT_SELECTED);
        assertThat(result.getHint()).isNotNull();
        ValidAdditionalProperties.assertEquals(result.getAdditionalProperties());
    }

    @Test
    public void toResultIntentData() throws Exception {
        HintRetrieveResult result = new HintRetrieveResult.Builder(
                HintRetrieveResult.CODE_HINT_SELECTED)
                .setHint(HINT)
                .setAdditionalProperties(ValidAdditionalProperties.make())
                .build();

        Intent intent = result.toResultDataIntent();
        assertThat(intent.hasExtra(ProtocolConstants.EXTRA_HINT_RESULT));
        byte[] data = intent.getByteArrayExtra(ProtocolConstants.EXTRA_HINT_RESULT);

        HintRetrieveResult readResult = HintRetrieveResult.fromProtobufBytes(data);
        assertThat(readResult.getResultCode()).isEqualTo(HintRetrieveResult.CODE_HINT_SELECTED);
        assertThat(readResult.getHint()).isNotNull();
        ValidAdditionalProperties.assertEquals(readResult.getAdditionalProperties());
    }
}
