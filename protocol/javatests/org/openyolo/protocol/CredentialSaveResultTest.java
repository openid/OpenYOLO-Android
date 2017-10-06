/*
 * Copyright 2017 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openyolo.protocol;

import static org.assertj.core.api.Java6Assertions.assertThat;

import android.content.Intent;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.TestConstants.ValidProperties;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.CollectionConverter;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Unit tests for {@link CredentialSaveResult} */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public final class CredentialSaveResultTest {

    private static final class ValidResult {
        public static int RESULT_CODE = CredentialSaveResult.CODE_SAVED;
        public static Map<String, byte[]> ADDITIONAL_PROPERTIES = ValidProperties.MAP_INSTANCE;

        public static final CredentialSaveResult INSTANCE =
                new CredentialSaveResult.Builder(RESULT_CODE)
                        .setAdditionalProperties(ADDITIONAL_PROPERTIES)
                        .build();

        public static void assertEqualTo(CredentialSaveResult result) {
            assertThat(result.getResultCode()).isEqualTo(RESULT_CODE);
            ValidProperties.assertEqualTo(result.getAdditionalProperties());
        }

        public static void assertEqualTo(Protobufs.CredentialSaveResult resultProto) {
            assertThat(resultProto.getResultCodeValue()).isEqualTo(RESULT_CODE);

            final Map<String, byte[]> additionalProperties =
                    CollectionConverter.convertMapValues(
                            resultProto.getAdditionalPropsMap(),
                            ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY);

            ValidProperties.assertEqualTo(additionalProperties);
        }
    }

    @Test
    public void fromProtobufBytes_withValidProto_returnsEquivalentResult() throws Exception {
        final CredentialSaveResult result =
                CredentialSaveResult.fromProtobufBytes(
                        ValidResult.INSTANCE.toProtobuf().toByteArray());

        ValidResult.assertEqualTo(result);
    }

    @Test
    public void fromProtobuf_withValidProto_returnsEquivalentResult() throws Exception {
        final CredentialSaveResult result =
                CredentialSaveResult.fromProtobuf(ValidResult.INSTANCE.toProtobuf());

        ValidResult.assertEqualTo(result);
    }

    @Test
    public void toProtobuf_withValidResult_returnsEquivalentResult() throws Exception {
        ValidResult.assertEqualTo(ValidResult.INSTANCE.toProtobuf());
    }

    @Test
    public void toResultDataIntent_withValidResult_returnsValidIntent() throws Exception {
        final Intent resultIntent = ValidResult.INSTANCE.toResultDataIntent();

        final byte[] encodedResult =
                resultIntent.getByteArrayExtra(ProtocolConstants.EXTRA_SAVE_RESULT);
        ValidResult.assertEqualTo(CredentialSaveResult.fromProtobufBytes(encodedResult));
    }

    @Test(expected = MalformedDataException.class)
    public void fromProto_withNullProto_throwsMalformedDataException() throws Exception {
        CredentialSaveResult.fromProtobuf(null);
    }
}
