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
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_ANOTHER_KEY;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_STRING_VALUE;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_TEST_KEY;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_TWO_BYTE_VALUE;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_ZERO_BYTE_VALUE;

import android.content.Intent;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.TestConstants.ValidProperties;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Unit tests for {@link CredentialRetrieveResult}. */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CredentialRetrieveResultTest {

  private static final class ValidCredential {
    public static final String ID = "email@gogo.com";
    public static final AuthenticationMethod AUTHENTICATION_METHOD = AuthenticationMethods.EMAIL;
    public static final String AUTH_DOMAIN_STR = "https://www.example.com";
    public static final AuthenticationDomain AUTHENTICATION_DOMAIN =
        new AuthenticationDomain(AUTH_DOMAIN_STR);
    public static final Credential INSTANCE =
        new Credential.Builder(ID, AUTHENTICATION_METHOD, AUTHENTICATION_DOMAIN).build();

    private static void assertEqualTo(Credential credential) {
      assertThat(credential.getIdentifier()).isEqualTo(ID);
    }

    private static void assertEqualTo(Protobufs.Credential credential) {
      assertThat(credential.getId()).isEqualTo(ID);
    }
  }

  private static final class ValidCredentialSelectedResult {
    public static int RESULT_CODE = CredentialRetrieveResult.CODE_CREDENTIAL_SELECTED;
    public static Map<String, byte[]> ADDITIONAL_PROPERTIES = ValidProperties.MAP_INSTANCE;

    public static final CredentialRetrieveResult INSTANCE =
        new CredentialRetrieveResult.Builder(RESULT_CODE)
            .setCredential(ValidCredential.INSTANCE)
            .setAdditionalProperties(ADDITIONAL_PROPERTIES)
            .build();

    private static void assertEqualTo(Protobufs.CredentialRetrieveResult result) {
      assertThat(result.getResultCodeValue()).isEqualTo(RESULT_CODE);
      ValidCredential.assertEqualTo(result.getCredential());
      ValidProperties.assertEqualTo(result.getAdditionalPropsMap(), false /* dummy */);
    }

    private static void assertEqualTo(CredentialRetrieveResult result) {
      assertThat(result.getResultCode()).isEqualTo(RESULT_CODE);
      ValidCredential.assertEqualTo(result.getCredential());
      ValidProperties.assertEqualTo(result.getAdditionalProperties());
    }
  }

  private static final class ValidBadRequestResult {
    public static int RESULT_CODE = CredentialRetrieveResult.CODE_BAD_REQUEST;
    public static Map<String, byte[]> ADDITIONAL_PROPERTIES = ValidProperties.MAP_INSTANCE;

    public static final CredentialRetrieveResult INSTANCE =
        new CredentialRetrieveResult.Builder(RESULT_CODE)
            .setAdditionalProperties(ADDITIONAL_PROPERTIES)
            .build();

    private static void assertEqualTo(Protobufs.CredentialRetrieveResult result) {
      assertThat(result.getResultCodeValue()).isEqualTo(RESULT_CODE);
      assertThat(result.getCredential()).isEqualTo(Protobufs.Credential.getDefaultInstance());
      ValidProperties.assertEqualTo(result.getAdditionalPropsMap(), false /* dummy */);
    }

    private static void assertEqualTo(CredentialRetrieveResult result) {
      assertThat(result.getResultCode()).isEqualTo(RESULT_CODE);
      assertThat(result.getCredential()).isNull();
      ValidProperties.assertEqualTo(result.getAdditionalProperties());
    }
  }

  @Test
  public void testBuilder_setAdditionalProperty() {
    CredentialRetrieveResult cdr = new CredentialRetrieveResult.Builder(
            CredentialRetrieveResult.CODE_NO_CREDENTIALS_AVAILABLE)
            .setAdditionalProperty(ADDITIONAL_PROP_TEST_KEY, ADDITIONAL_PROP_TWO_BYTE_VALUE)
            .setAdditionalProperty(ADDITIONAL_PROP_ANOTHER_KEY, ADDITIONAL_PROP_ZERO_BYTE_VALUE)
            .build();

    Map<String, byte[]> additionalProps = cdr.getAdditionalProperties();
    assertThat(additionalProps.size()).isEqualTo(2);
    assertThat(additionalProps.containsKey(ADDITIONAL_PROP_TEST_KEY));
    assertThat(additionalProps.get(ADDITIONAL_PROP_TEST_KEY))
            .isEqualTo(ADDITIONAL_PROP_TWO_BYTE_VALUE);
    assertThat(additionalProps.containsKey(ADDITIONAL_PROP_ANOTHER_KEY));
    assertThat(additionalProps.get(ADDITIONAL_PROP_ANOTHER_KEY))
            .isEqualTo(ADDITIONAL_PROP_ZERO_BYTE_VALUE);
  }

  @Test
  public void testBuilder_setAdditionalProperty_overwriteExistingValue() {
    CredentialRetrieveResult cdr = new CredentialRetrieveResult.Builder(
            CredentialRetrieveResult.CODE_NO_CREDENTIALS_AVAILABLE)
            .setAdditionalProperty(ADDITIONAL_PROP_TEST_KEY, ADDITIONAL_PROP_TWO_BYTE_VALUE)
            .setAdditionalProperty(ADDITIONAL_PROP_TEST_KEY, ADDITIONAL_PROP_ZERO_BYTE_VALUE)
            .build();

    Map<String, byte[]> additionalProps = cdr.getAdditionalProperties();
    assertThat(additionalProps.size()).isEqualTo(1);
    assertThat(additionalProps.containsKey(ADDITIONAL_PROP_TEST_KEY));
    assertThat(additionalProps.get(ADDITIONAL_PROP_TEST_KEY))
            .isEqualTo(ADDITIONAL_PROP_ZERO_BYTE_VALUE);
  }

  @Test
  public void testBuilder_setAdditionalPropertyAsString() {
    CredentialRetrieveResult cdr = new CredentialRetrieveResult.Builder(
            CredentialRetrieveResult.CODE_NO_CREDENTIALS_AVAILABLE)
            .setAdditionalPropertyAsString(
                    ADDITIONAL_PROP_TEST_KEY,
                    ADDITIONAL_PROP_STRING_VALUE)
            .build();

    Map<String, byte[]> additionalProps = cdr.getAdditionalProperties();
    assertThat(additionalProps.size()).isEqualTo(1);
    assertThat(additionalProps.containsKey(ADDITIONAL_PROP_TEST_KEY));
    assertThat(additionalProps.get(ADDITIONAL_PROP_TEST_KEY))
            .isEqualTo(AdditionalPropertiesHelper.encodeStringValue(
                    ADDITIONAL_PROP_STRING_VALUE));
  }

    @Test
    public void testGetAdditionalProperty() {
        CredentialRetrieveResult cdr = new CredentialRetrieveResult.Builder(
                CredentialRetrieveResult.CODE_NO_CREDENTIALS_AVAILABLE)
                .setAdditionalProperties(ImmutableMap.of(
                        ADDITIONAL_PROP_TEST_KEY,
                        ADDITIONAL_PROP_TWO_BYTE_VALUE))
                .build();

        assertThat(cdr.getAdditionalProperty(ADDITIONAL_PROP_TEST_KEY))
                .isEqualTo(ADDITIONAL_PROP_TWO_BYTE_VALUE);
    }

    @Test
    public void testGetAdditionalProperty_withMissingKey() {
        CredentialRetrieveResult cdr = new CredentialRetrieveResult.Builder(
                CredentialRetrieveResult.CODE_NO_CREDENTIALS_AVAILABLE)
                .build();
        assertThat(cdr.getAdditionalProperty("missingKey")).isNull();
    }

    @Test
    public void testGetAdditionalPropertyAsString() {
        CredentialRetrieveResult cdr = new CredentialRetrieveResult.Builder(
                CredentialRetrieveResult.CODE_NO_CREDENTIALS_AVAILABLE)
                .setAdditionalProperties(ImmutableMap.of(
                        ADDITIONAL_PROP_TEST_KEY,
                        AdditionalPropertiesHelper.encodeStringValue(ADDITIONAL_PROP_STRING_VALUE)))
                .build();

        assertThat(cdr.getAdditionalPropertyAsString(ADDITIONAL_PROP_TEST_KEY))
                .isEqualTo(ADDITIONAL_PROP_STRING_VALUE);
    }

  @Test
  public void fromProtobufBytes_withValidCredentialSelectedResult_returnsEquivalent() throws Exception {
    byte[] encodedBytes = ValidCredentialSelectedResult.INSTANCE.toProtobuf().toByteArray();

    CredentialRetrieveResult result = CredentialRetrieveResult.fromProtobufBytes(encodedBytes);

    ValidCredentialSelectedResult.assertEqualTo(result);
  }

  @Test
  public void fromProtobufBytes_withValidBadRequestResult_returnsEquivalent() throws Exception {
    byte[] encodedBytes = ValidBadRequestResult.INSTANCE.toProtobuf().toByteArray();

    CredentialRetrieveResult result = CredentialRetrieveResult.fromProtobufBytes(encodedBytes);

    ValidBadRequestResult.assertEqualTo(result);
  }

  @Test
  public void toProtobuf_withValidCredentialSelectedResult_returnsEquivalent() throws Exception {
    ValidCredentialSelectedResult.assertEqualTo(
        ValidCredentialSelectedResult.INSTANCE.toProtobuf());
  }

  @Test
  public void toProtobuf_withValidBadRequestResult_returnsEquivalent() throws Exception {
    ValidBadRequestResult.assertEqualTo(ValidBadRequestResult.INSTANCE.toProtobuf());
  }

  @Test
  public void toResultDataIntent_withValidRequest_encodesIntoIntent() throws Exception {
    Intent intent = ValidBadRequestResult.INSTANCE.toResultDataIntent();

    byte[] encodedResult = intent.getByteArrayExtra(ProtocolConstants.EXTRA_RETRIEVE_RESULT);
    CredentialRetrieveResult result = CredentialRetrieveResult.fromProtobufBytes(encodedResult);
    ValidBadRequestResult.assertEqualTo(result);
  }
}
