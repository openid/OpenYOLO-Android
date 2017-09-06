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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.TestConstants.ValidTokenRequestInfo;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Tests for {@link TokenRequestInfo}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class TokenRequestInfoTest {

    @Test
    public void build_validInputs_shouldSucceed() {
        TokenRequestInfo info = ValidTokenRequestInfo.make();
        ValidTokenRequestInfo.assertEquals(info);
    }

    @Test
    public void builderSetClientId_nullValue_shouldSucceed() {
        TokenRequestInfo info = new TokenRequestInfo.Builder()
                .setClientId(null)
                .build();

        assertThat(info.getClientId()).isNull();
    }

    @Test
    public void builderSetClientId_emptyValue_treatedAsNull() {
        TokenRequestInfo info = new TokenRequestInfo.Builder()
                .setClientId("")
                .build();

        assertThat(info.getClientId()).isNull();
    }

    @Test
    public void builderSetNonce_nullValue_shouldSucceed() {
        TokenRequestInfo info = new TokenRequestInfo.Builder()
                .setNonce(null)
                .build();

        assertThat(info.getNonce()).isNull();
    }

    @Test
    public void builderSetNonce_emptyValue_treatedAsNull() {
        TokenRequestInfo info = new TokenRequestInfo.Builder()
                .setNonce("")
                .build();

        assertThat(info.getNonce()).isNull();
    }

    @Test
    public void toProto_fromProto_isEquivalent() throws MalformedDataException {
        Protobufs.TokenRequestInfo proto = ValidTokenRequestInfo.make().toProtobuf();
        TokenRequestInfo info = TokenRequestInfo.fromProtobuf(proto);

        ValidTokenRequestInfo.assertEquals(info);
    }

    @Test
    public void fromProto_defaultInstance_shouldSucceed() throws MalformedDataException {
        Protobufs.TokenRequestInfo proto = Protobufs.TokenRequestInfo.getDefaultInstance();

        TokenRequestInfo info = TokenRequestInfo.fromProtobuf(proto);
        assertThat(info.getClientId()).isNull();
        assertThat(info.getNonce()).isNull();
        assertThat(info.getAdditionalProperties()).isEmpty();
    }

    @Test(expected = MalformedDataException.class)
    public void fromProtoBytes_invalidData_shouldThrowException() throws Exception {
        TokenRequestInfo.fromProtobufBytes(TestConstants.INVALID_PROTO_BYTES);
    }
}
