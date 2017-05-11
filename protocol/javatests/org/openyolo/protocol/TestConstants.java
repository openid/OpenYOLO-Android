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

import android.content.pm.Signature;
import android.net.Uri;
import android.util.Base64;
import com.google.protobuf.ByteString;
import java.util.HashMap;
import java.util.Map;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.CollectionConverter;

/**
 * Useful constants and associated verification methods for tests.
 */
public final class TestConstants {

    public static final String ALICE_EMAIL = "alice@example.com";
    public static final String ALICE_NAME = "Alice McTesterson";
    public static final String ALICE_DISPLAY_PICTURE_URI_STR = "https://robohash.org/alice";
    public static final Uri ALICE_DISPLAY_PICTURE_URI =
            Uri.parse(ALICE_DISPLAY_PICTURE_URI_STR);
    public static final String ALICE_PASSWORD = "CorrectH0rseBatteryStapl3";

    public static final String ALICE_ID_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NSIsImF1ZCI"
                    + "6IlhZWi5jbGllbnQuaWQiLCJpc3MiOiJpZHAuZXhhbXBsZS5jb20iLCJlbWFpbCI"
                    + "6ImFsaWNlQGV4YW1wbGUuY29tIiwibmFtZSI6IkFsaWNlIE1jVGVzdGVyc29uIiw"
                    + "icGljdHVyZSI6Imh0dHBzOi8vcm9ib2hhc2gub3JnL2FsaWNlIn0.2-D7AZ1C7mv"
                    + "dLRf6Q7aqH8Ah4rlK1uuHPSU2HPImtyk";

    public static final String CLIENT_ID = "XYZ.client.id";
    public static final String NONCE = "asdf";

    public static final String EXAMPLE_APP_PACKAGE_NAME = "com.example.app";

    public static final byte[] EXAMPLE_APP_SIGNATURE_BYTES =
            Base64.decode(
                    "MIICwzCCAaugAwIBAgIEYUHE8DANBgkqhkiG9w0BAQsFADASMRAwDgYDVQQDEwd0"
                    + "ZXN0aW5nMB4XDTE3MDUxMDIxMTY1M1oXDTQ0MDkyNTIxMTY1M1owEjEQMA4GA1UE"
                    + "AxMHdGVzdGluZzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJ2+5bxE"
                    + "24gsczcfwoAgJrerBGgew5rHiUYekp6nlfOlqQqJbC5KNKOr8qK1IF92MSrcphIw"
                    + "CWWkp67Bqqe49nK3ce9kVHqhXjaz9w4HVex3N3kWt1r7s08lNax/67vTrfQXlDnI"
                    + "1VDijm82vklmLtcXXnww10FQRKVdtxSkCmtUjmuYpLqFZY5cDIG5fpoeFhzoDolj"
                    + "pfmYkDsFGEUVOIilrM70rdwziEuvXPVGIzI8Lz88OkdamtQ2dtWSFP+4O8tv6qSW"
                    + "Q00/YAIm/RV2Z3NFIPma1n7GmTqm+QBM4lq8Irc24yL/a78nVT+fibfBOr7Iu02Z"
                    + "qy4Rpkosq4bfgt8CAwEAAaMhMB8wHQYDVR0OBBYEFJXEQr/2vuy2E2o2lz8LRbZW"
                    + "b/QzMA0GCSqGSIb3DQEBCwUAA4IBAQB5N++YqygWTFDwfCGgBT3pytaKVGbSujvB"
                    + "ChmBry2kfT5SpZcMerTboxq+0Jny50jS+2FAl0apKYC56R+FZC3Zg1qUBlqcCOrZ"
                    + "j2r7INQHWfiZo76zBjsaf9iDJwwDHKox5Bu8TK0Iux4hPi3J0hWg+MXDq6GUHQPT"
                    + "aFfxVAPNjnu+BnMrdw3YwEGxUBNTm0BeJruF2Hvzt9s/HOJ4y70dhlnz3McrxSQ2"
                    + "Tmlo1G0YwaTDO3jYDtD7CJ2V9EdAr9HjUeIlOiHwSHxDZexRxsiJf9rADP3Mqh7r"
                    + "I+gmzIbXs+UA7nsHXVgTyg5NviDbmYcu/hqKOLf1UeMwAEjQu0U9",
                    Base64.NO_PADDING);

    public static final Signature EXAMPLE_APP_SIGNATURE =
            new Signature(EXAMPLE_APP_SIGNATURE_BYTES);

    public static final String EXAMPLE_APP_SHA256_FINGERPRINT =
            "uHq8C9rrDcJ_MqkyeauSba4OYMSOJJqUDWEuMWiOdfo=";

    public static final String EXAMPLE_APP_SHA512_FINGERPRINT =
            "KSYmxK5qmKUKhNxHJYv__Tgg8nFXkm_w7mhJd_feckMW"
                    + "NqBXGK7yhZugh4OVI5ffJn8_V4SEN-mqetuIqiqPVA==";

    public static final AuthenticationDomain EXAMPLE_APP_AUTH_DOMAIN =
            AuthenticationDomain.createAndroidAuthDomain(
                    EXAMPLE_APP_PACKAGE_NAME,
                    EXAMPLE_APP_SIGNATURE);

    public static final String TOKEN_PROVIDER_1 = "https://idp1.example.com";
    public static final String TOKEN_PROVIDER_2 = "https://idp2.example.com";

    public static final String ADDITIONAL_KEY = "extra";
    public static final byte[] ADDITIONAL_VALUE = "value".getBytes();
    public static final Map<String, byte[]> ADDITIONAL_PROPS;
    public static final Map<String, ByteString> ADDITIONAL_PROPS_FOR_PROTO;

    public static final byte[] INVALID_PROTO_BYTES = new byte[] { 1, 2, 3 };

    static {
        Map<String, byte[]> additionalProps = new HashMap<>();
        additionalProps.put(ADDITIONAL_KEY, ADDITIONAL_VALUE);
        ADDITIONAL_PROPS = additionalProps;
        ADDITIONAL_PROPS_FOR_PROTO = CollectionConverter.convertMapValues(
                additionalProps,
                ByteStringConverters.BYTE_ARRAY_TO_BYTE_STRING);
    }

    public static void checkAdditionalProps(Map<String, byte[]> additionalProps) {
        assertThat(additionalProps).isNotNull();
        assertThat(additionalProps).hasSize(1);
        assertThat(additionalProps).containsKey(ADDITIONAL_KEY);
        assertThat(additionalProps.get(ADDITIONAL_KEY)).isEqualTo(ADDITIONAL_VALUE);
    }

    public static void checkAdditionalPropsFromProto(Map<String, ByteString> additionalProps) {
        checkAdditionalProps(CollectionConverter.convertMapValues(
                additionalProps,
                ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY));
    }

    public static TokenRequestInfo createTokenRequestInfo() {
        return new TokenRequestInfo.Builder()
                .setClientId(CLIENT_ID)
                .setNonce(NONCE)
                .setAdditionalProperties(ADDITIONAL_PROPS)
                .build();
    }

    public static Protobufs.TokenRequestInfo createTokenRequestInfoProto() {
        return Protobufs.TokenRequestInfo.newBuilder()
                .setClientId(CLIENT_ID)
                .setNonce(NONCE)
                .putAllAdditionalProps(ADDITIONAL_PROPS_FOR_PROTO)
                .build();
    }

    public static void checkTokenRequestInfo(TokenRequestInfo info) {
        assertThat(info.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(info.getNonce()).isEqualTo(NONCE);
        TestConstants.checkAdditionalProps(info.getAdditionalProperties());
    }

    public static void checkTokenRequestInfoProto(Protobufs.TokenRequestInfo info) {
        assertThat(info.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(info.getNonce()).isEqualTo(NONCE);
        TestConstants.checkAdditionalPropsFromProto(info.getAdditionalPropsMap());
    }

    public static Map<String, TokenRequestInfo> createTokenProviderMap() {
        HashMap<String, TokenRequestInfo> tokenProviders = new HashMap<>();
        tokenProviders.put(TOKEN_PROVIDER_1, createTokenRequestInfo());
        tokenProviders.put(TOKEN_PROVIDER_2, null);
        return tokenProviders;
    }

    public static void checkTokenProviderMap(Map<String, TokenRequestInfo> tokenProviders) {
        assertThat(tokenProviders).hasSize(2);
        assertThat(tokenProviders).containsKey(TOKEN_PROVIDER_1);
        assertThat(tokenProviders).containsKey(TOKEN_PROVIDER_2);
        checkTokenRequestInfo(tokenProviders.get(TOKEN_PROVIDER_1));
        assertThat(tokenProviders.get(TOKEN_PROVIDER_2).getClientId()).isNull();
        assertThat(tokenProviders.get(TOKEN_PROVIDER_2).getNonce()).isNull();
        assertThat(tokenProviders.get(TOKEN_PROVIDER_2).getAdditionalProperties()).isEmpty();
    }

    public static Credential createEmailCredential() {
        return new Credential.Builder(
                ALICE_EMAIL,
                AuthenticationMethods.EMAIL,
                EXAMPLE_APP_AUTH_DOMAIN)
                .setDisplayName(ALICE_NAME)
                .setDisplayPicture(ALICE_DISPLAY_PICTURE_URI)
                .setPassword(ALICE_PASSWORD)
                .setIdentifier(ALICE_ID_TOKEN)
                .setIdToken(ALICE_ID_TOKEN)
                .setAdditionalProperties(ADDITIONAL_PROPS)
                .build();
    }

    public static Protobufs.Credential createEmailCredentialProto() {
        return Protobufs.Credential.newBuilder()
                .setId(ALICE_EMAIL)
                .setAuthMethod(AuthenticationMethods.EMAIL.toProtobuf())
                .setAuthDomain(EXAMPLE_APP_AUTH_DOMAIN.toProtobuf())
                .setDisplayName(ALICE_NAME)
                .setDisplayPictureUri(ALICE_DISPLAY_PICTURE_URI_STR)
                .setPassword(ALICE_PASSWORD)
                .setIdToken(ALICE_ID_TOKEN)
                .putAllAdditionalProps(ADDITIONAL_PROPS_FOR_PROTO)
                .build();
    }

    public static void checkEmailCredential(Credential credential) {
        assertThat(credential.getIdentifier()).isEqualTo(ALICE_EMAIL);
        assertThat(credential.getAuthenticationMethod())
                .isEqualTo(AuthenticationMethods.EMAIL);
        assertThat(credential.getAuthenticationDomain()).isEqualTo(EXAMPLE_APP_AUTH_DOMAIN);
        assertThat(credential.getDisplayName()).isEqualTo(ALICE_NAME);
        assertThat(credential.getDisplayPicture()).isEqualTo(ALICE_DISPLAY_PICTURE_URI);
        assertThat(credential.getPassword()).isEqualTo(ALICE_PASSWORD);
        assertThat(credential.getIdToken()).isEqualTo(ALICE_ID_TOKEN);
    }

    public static void checkEmailCredentialProto(Protobufs.Credential proto) {
        assertThat(proto.getId()).isEqualTo(ALICE_EMAIL);
        assertThat(proto.getAuthMethod().getUri())
                .isEqualTo(AuthenticationMethods.EMAIL.toString());
        assertThat(proto.getAuthDomain().getUri())
                .isEqualTo(EXAMPLE_APP_AUTH_DOMAIN.toString());
        assertThat(proto.getDisplayName()).isEqualTo(ALICE_NAME);
        assertThat(proto.getDisplayPictureUri()).isEqualTo(ALICE_DISPLAY_PICTURE_URI_STR);
        assertThat(proto.getIdToken()).isEqualTo(ALICE_ID_TOKEN);
        checkAdditionalPropsFromProto(proto.getAdditionalPropsMap());
    }
}
