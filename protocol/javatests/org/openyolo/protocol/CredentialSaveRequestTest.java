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

import android.os.Parcel;
import java.io.IOException;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.TextFixtures.ValidProperties;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.ClientVersionUtil;
import org.openyolo.protocol.internal.CollectionConverter;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.valid4j.errors.RequireViolation;

/** Unit tests for {@link CredentialSaveRequest} */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public final class CredentialSaveRequestTest {

    private static final class ValidClientVersion {
        public static final int MAJOR = 1;
        public static final int MINOR = 2;
        public static final int PATCH = 3;
        public static final String VENDOR = "com.customVendor";

        public static final Protobufs.ClientVersion INSTANCE = Protobufs.ClientVersion.newBuilder()
                .setMajor(MAJOR)
                .setMinor(MINOR)
                .setPatch(PATCH)
                .setVendor(VENDOR)
                .build();
    }

    private static final class ValidRequest {
        public static final Credential CREDENTIAL = ValidCredential.INSTANCE;
        public static final Map<String, byte[]> ADDITIONAL_PROPERTIES = ValidProperties.MAP_INSTANCE;

        public static final CredentialSaveRequest INSTANCE =
                new CredentialSaveRequest.Builder(CREDENTIAL)
                        .setAdditionalProperties(ADDITIONAL_PROPERTIES)
                        .build();

        public static void assertEqualTo(CredentialSaveRequest request) {
            ValidCredential.assertEqualTo(request.getCredential());
            ValidProperties.assertEqualTo(request.getAdditionalProperties());
        }

        public static void assertEqualTo(Protobufs.CredentialSaveRequest request) {
            ValidCredential.assertEqualTo(request.getCredential());
            assertThat(request.getClientVersion()).isEqualTo(ValidClientVersion.INSTANCE);

            final Map<String, byte[]> additionalProperties =
                    CollectionConverter.convertMapValues(
                            request.getAdditionalPropsMap(),
                            ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY);

            ValidProperties.assertEqualTo(additionalProperties);
        }
    }

    private static final class ValidCredential {
        public final static String ID = "Credential ID";
        public final static String AUTHENTICATION_DOMAIN_STRING = "https://.google.com";
        public final static AuthenticationMethod AUTHENTICATION_METHOD =
                AuthenticationMethods.FACEBOOK;
        public final static AuthenticationDomain AUTHENTICATION_DOMAIN =
                new AuthenticationDomain(AUTHENTICATION_DOMAIN_STRING);

        public final static Credential INSTANCE =
                new Credential.Builder(ID, AUTHENTICATION_METHOD, AUTHENTICATION_DOMAIN).build();

        public static void assertEqualTo(Credential credential) {
            assertThat(credential.getIdentifier()).isEqualTo(ID);
        }

        public static void assertEqualTo(Protobufs.Credential credential) {
            assertThat(credential.getId()).isEqualTo(ID);
        }
    }

    @Before
    public void setup() {
        ClientVersionUtil.setClientVersion(ValidClientVersion.INSTANCE);
    }

    @Test(expected = IOException.class)
    public void fromProtoBytes_withNull_throwsIOException() throws Exception {
        CredentialSaveRequest.fromProtoBytes(null /* bytes */);
    }

    @Test()
    public void fromProtoBytes_withValidRequestBytes_returnsEquivalentRequest() throws Exception {
        final byte[] encodedRequest = ValidRequest.INSTANCE.toProtocolBuffer().toByteArray();

        final CredentialSaveRequest request = CredentialSaveRequest.fromProtoBytes(encodedRequest);

        ValidRequest.assertEqualTo(request);
    }

    @Test
    public void forCredential_withValidCredential_returnsRequestForCredential() {
        final CredentialSaveRequest request =
                CredentialSaveRequest.fromCredential(ValidCredential.INSTANCE);

        assertThat(request.getCredential()).isSameAs(ValidCredential.INSTANCE);
        assertThat(request.getAdditionalProperties()).isEmpty();
    }

    @Test
    public void parcelAndUnparcel_withValidCredential_isEqual() {
        Parcel parcel = Parcel.obtain();
        ValidRequest.INSTANCE.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        final CredentialSaveRequest request =
                CredentialSaveRequest.CREATOR.createFromParcel(parcel);

        ValidRequest.assertEqualTo(request);
    }

    @Test
    public void toProtocolBuffer_withValidCredential_containsClientVersion() {
        final Protobufs.CredentialSaveRequest protoRequest =
                ValidRequest.INSTANCE.toProtocolBuffer();

        ValidRequest.assertEqualTo(protoRequest);
    }

    @Test
    public void builder_withValidCredential_resultIsEqual() {
        final CredentialSaveRequest request =
                new CredentialSaveRequest.Builder(ValidCredential.INSTANCE).build();

        assertThat(request.getCredential()).isSameAs(ValidCredential.INSTANCE);
        assertThat(request.getAdditionalProperties()).isEmpty();
    }

    @Test(expected = RequireViolation.class)
    public void builder_withNullCredential_throwsRequiresViolation() {
        new CredentialSaveRequest.Builder((Credential) null);
    }

    @Test
    public void builder_withValidProto_resultIsEqual() {
        final CredentialSaveRequest request =
                new CredentialSaveRequest.Builder(ValidRequest.INSTANCE.toProtocolBuffer()).build();

        assertThat(request.getCredential().getIdentifier()).isEqualTo(ValidCredential.ID);
    }

    @Test(expected = RequireViolation.class)
    public void builder_withNullProto_throwsRequiresViolation() {
        new CredentialSaveRequest.Builder((Protobufs.CredentialSaveRequest) null);
    }

    public void builder_setAdditionalPropertiesWithNull_returnsEmptyMap() {
        CredentialSaveRequest request =
                new CredentialSaveRequest.Builder(ValidRequest.CREDENTIAL)
                        .setAdditionalProperties(ValidRequest.ADDITIONAL_PROPERTIES)
                        .setAdditionalProperties(null)
                        .build();

        assertThat(request.getAdditionalProperties()).isEmpty();
    }

    @Test
    public void builder_setAdditionalProperties_isSuccessful() {
        final CredentialSaveRequest request =
                new CredentialSaveRequest.Builder(ValidRequest.CREDENTIAL)
                        .setAdditionalProperties(ValidRequest.ADDITIONAL_PROPERTIES)
                        .build();

        ValidProperties.assertEqualTo(request.getAdditionalProperties());
    }
}
