/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
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
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_ANOTHER_KEY;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_STRING_VALUE;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_TEST_KEY;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_TWO_BYTE_VALUE;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_ZERO_BYTE_VALUE;


import android.os.Parcel;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.internal.ClientVersionUtil;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.openyolo.protocol.TestConstants.ValidTokenProviderMap;
import org.openyolo.protocol.TestConstants.ValidAdditionalProperties;

/**
 * Tests for {@link CredentialRetrieveRequest}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CredentialRetrieveRequestTest {

    @Test
    public void build_validInputs_shouldSucceed() {
        CredentialRetrieveRequest request = new CredentialRetrieveRequest.Builder(
                AuthenticationMethods.EMAIL)
                .setTokenProviders(ValidTokenProviderMap.make())
                .setAdditionalProperties(ValidAdditionalProperties.make())
                .build();

        assertThat(request.getAuthenticationMethods()).hasSize(1);
        assertThat(request.getAuthenticationMethods()).contains(AuthenticationMethods.EMAIL);
        ValidTokenProviderMap.assertEquals(request.getTokenProviders());
        ValidAdditionalProperties.assertEquals(request.getAdditionalProperties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderUriSetConstructor_withEmptySet_throwsIllegalArgumentException() {
        new CredentialRetrieveRequest.Builder(new HashSet<AuthenticationMethod>());
    }

    @Test
    public void testBuilder_setAdditionalProperty() {
        CredentialRetrieveRequest cdr = new CredentialRetrieveRequest.Builder(
                AuthenticationMethods.EMAIL)
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
        CredentialRetrieveRequest cdr = new CredentialRetrieveRequest.Builder(
                AuthenticationMethods.EMAIL)
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
        CredentialRetrieveRequest cdr = new CredentialRetrieveRequest.Builder(
                AuthenticationMethods.EMAIL)
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
        CredentialRetrieveRequest cdr = new CredentialRetrieveRequest.Builder(
                AuthenticationMethods.EMAIL)
                .setAdditionalProperties(ImmutableMap.of(
                        ADDITIONAL_PROP_TEST_KEY,
                        ADDITIONAL_PROP_TWO_BYTE_VALUE))
                .build();

        assertThat(cdr.getAdditionalProperty(ADDITIONAL_PROP_TEST_KEY))
                .isEqualTo(ADDITIONAL_PROP_TWO_BYTE_VALUE);
    }

    @Test
    public void testGetAdditionalProperty_withMissingKey() {
        CredentialRetrieveRequest cdr = new CredentialRetrieveRequest.Builder(
                AuthenticationMethods.EMAIL)
                .build();
        assertThat(cdr.getAdditionalProperty("missingKey")).isNull();
    }

    @Test
    public void testGetAdditionalPropertyAsString() {
        CredentialRetrieveRequest cdr = new CredentialRetrieveRequest.Builder(
                AuthenticationMethods.EMAIL)
                .setAdditionalProperties(ImmutableMap.of(
                        ADDITIONAL_PROP_TEST_KEY,
                        AdditionalPropertiesHelper.encodeStringValue(ADDITIONAL_PROP_STRING_VALUE)))
                .build();

        assertThat(cdr.getAdditionalPropertyAsString(ADDITIONAL_PROP_TEST_KEY))
                .isEqualTo(ADDITIONAL_PROP_STRING_VALUE);
    }

    @Test(expected = MalformedDataException.class)
    public void fromProto_withNull_throwsMalformedDataException() throws Exception {
        CredentialRetrieveRequest.fromProtobuf(null);
    }

    @Test
    public void testWriteAndRead() {
        CredentialRetrieveRequest request = new CredentialRetrieveRequest.Builder(
                AuthenticationMethods.EMAIL)
                .setTokenProviders(ValidTokenProviderMap.make())
                .setAdditionalProperties(ValidAdditionalProperties.make())
                .setRequireUserMediation(true)
                .build();

        Parcel p = Parcel.obtain();
        try {
            request.writeToParcel(p, 0);
            p.setDataPosition(0);
            CredentialRetrieveRequest deserialized = CredentialRetrieveRequest.CREATOR.createFromParcel(p);
            assertThat(deserialized).isNotNull();
            assertThat(deserialized.getAuthenticationMethods())
                    .isEqualTo(request.getAuthenticationMethods());
            ValidTokenProviderMap.assertEquals(deserialized.getTokenProviders());
            ValidAdditionalProperties.assertEquals(deserialized.getAdditionalProperties());
            assertThat(deserialized.getRequireUserMediation()).isTrue();
        } finally {
            p.recycle();
        }
    }

    @Test
    public void forAuthenticationMethods_withValidAuthenticationMethodsUsingVarArgs_returnsValidRequest() {
        CredentialRetrieveRequest request = CredentialRetrieveRequest
            .fromAuthMethods(AuthenticationMethods.GOOGLE, AuthenticationMethods.FACEBOOK);

        assertThat(request.getAuthenticationMethods())
            .containsOnly(AuthenticationMethods.GOOGLE, AuthenticationMethods.FACEBOOK);
    }

    @Test
    public void forAuthenticationMethods_withValidAuthenticationMethodUsingSet_returnsValidRequest() {
        Set<AuthenticationMethod> authenticationMethods = new HashSet<>();
        Collections.addAll(
            authenticationMethods,
            AuthenticationMethods.GOOGLE,
            AuthenticationMethods.FACEBOOK);

        CredentialRetrieveRequest request = CredentialRetrieveRequest.fromAuthMethods(authenticationMethods);

        assertThat(request.getAuthenticationMethods())
            .containsOnly(AuthenticationMethods.GOOGLE, AuthenticationMethods.FACEBOOK);
    }


    @Test
    public void toProtocolBuffer_usingDefaultBuilder_returnsExpectedValues() {
        CredentialRetrieveRequest request =
                new CredentialRetrieveRequest.Builder(AuthenticationMethods.EMAIL).build();

        Protobufs.CredentialRetrieveRequest protoRequest = request.toProtocolBuffer();

        assertThat(protoRequest.getRequireUserMediation())
                .isEqualTo(CredentialRetrieveRequest.DEFAULT_REQUIRE_USER_MEDIATION_VALUE);
    }

    @Test
    public void toProtocolBuffer_includesClientVersion() {
        String vendor = "test";
        int major = 1;
        int minor = 2;
        int patch = 3;
        ClientVersionUtil.setClientVersion(
                Protobufs.ClientVersion.newBuilder()
                        .setVendor(vendor)
                        .setMajor(major)
                        .setMinor(minor)
                        .setPatch(patch)
                        .build());

        try {
            CredentialRetrieveRequest request = CredentialRetrieveRequest.fromAuthMethods(
                    AuthenticationMethods.EMAIL);
            Protobufs.CredentialRetrieveRequest proto = request.toProtocolBuffer();
            assertThat(proto.hasClientVersion());
            assertThat(proto.getClientVersion().getVendor()).isEqualTo(vendor);
            assertThat(proto.getClientVersion().getMajor()).isEqualTo(major);
            assertThat(proto.getClientVersion().getMinor()).isEqualTo(minor);
            assertThat(proto.getClientVersion().getPatch()).isEqualTo(patch);
        } finally {
            ClientVersionUtil.setClientVersion(null);
        }
    }
}