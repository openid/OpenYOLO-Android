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
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import com.google.protobuf.ByteString;
import java.util.HashMap;
import java.util.Map;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.CollectionConverter;

/**
 * Useful constants and associated verification methods for tests.
 */
public final class TestConstants {

    public static final byte[] INVALID_PROTO_BYTES =
            new byte[] { 'i', 'n', 'v', 'a', 'l', 'i', 'd' };

    public static final class ValidAdditionalProperties {
        private static final String ADDITIONAL_KEY = "extra";
        private static final byte[] ADDITIONAL_VALUE = "value".getBytes();

        public static Map<String, byte[]> make() {
            Map<String, byte[]> additionalProps = new HashMap<>();
            additionalProps.put(ADDITIONAL_KEY, ADDITIONAL_VALUE);
            return additionalProps;
        }

        public static Map<String, ByteString> makeForProto() {
            return CollectionConverter.convertMapValues(
                    make(),
                    ByteStringConverters.BYTE_ARRAY_TO_BYTE_STRING);
        }


        public static void assertEquals(Map<String, byte[]> additionalProps) {
            assertThat(additionalProps).isNotNull();
            assertThat(additionalProps).hasSize(1);
            assertThat(additionalProps).containsKey(ADDITIONAL_KEY);
            assertThat(additionalProps.get(ADDITIONAL_KEY)).isEqualTo(ADDITIONAL_VALUE);
        }

        public static void assertEqualsForProto(Map<String, ByteString> additionalProps) {
            assertEquals(CollectionConverter.convertMapValues(
                    additionalProps,
                    ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY));
        }
    }

    public static final class ValidTokenRequestInfo {
        private static final String CLIENT_ID = "XYZ.client.id";
        private static final String NONCE = "asdf";

        public static TokenRequestInfo make() {
            return new TokenRequestInfo.Builder()
                    .setClientId(CLIENT_ID)
                    .setNonce(NONCE)
                    .setAdditionalProperties(ValidAdditionalProperties.make())
                    .build();
        }

        public static void assertEquals(TokenRequestInfo info) {
            assertThat(info.getClientId()).isEqualTo(CLIENT_ID);
            assertThat(info.getNonce()).isEqualTo(NONCE);
            ValidAdditionalProperties.assertEquals(info.getAdditionalProperties());
        }

        public static void assertEquals(Protobufs.TokenRequestInfo info) {
            assertThat(info.getClientId()).isEqualTo(CLIENT_ID);
            assertThat(info.getNonce()).isEqualTo(NONCE);
            ValidAdditionalProperties.assertEqualsForProto(info.getAdditionalPropsMap());
        }

    }

    public static final class ValidTokenProviderMap {
        private static final String TOKEN_PROVIDER_1 = "https://idp1.example.com";
        private static final String TOKEN_PROVIDER_2 = "https://idp2.example.com";


        public static Map<String, TokenRequestInfo> make() {
            HashMap<String, TokenRequestInfo> tokenProviders = new HashMap<>();
            tokenProviders.put(TOKEN_PROVIDER_1, ValidTokenRequestInfo.make());
            tokenProviders.put(TOKEN_PROVIDER_2, null);
            return tokenProviders;
        }

        public static void assertEquals(Map<String, TokenRequestInfo> tokenProviders) {
            assertThat(tokenProviders).hasSize(2);
            assertThat(tokenProviders).containsKey(TOKEN_PROVIDER_1);
            assertThat(tokenProviders).containsKey(TOKEN_PROVIDER_2);
            ValidTokenRequestInfo.assertEquals(tokenProviders.get(TOKEN_PROVIDER_1));
            assertThat(tokenProviders.get(TOKEN_PROVIDER_2).getClientId()).isNull();
            assertThat(tokenProviders.get(TOKEN_PROVIDER_2).getNonce()).isNull();
            assertThat(tokenProviders.get(TOKEN_PROVIDER_2).getAdditionalProperties()).isEmpty();
        }
    }

    public static final class ValidProperties {
        public static final String PROPERTY_A_NAME = "Property A";
        public static final byte[] PROPERTY_A_VALUE = new byte [] { 0, 1 };

        public static final String PROPERTY_B_NAME = "Property B";
        public static final byte[] PROPERTY_B_VALUE = new byte [] { 2, 2 };

        public static final Map<String, byte[]> MAP_INSTANCE;

        static {
            MAP_INSTANCE = new HashMap<>();
            MAP_INSTANCE.put(PROPERTY_A_NAME, PROPERTY_A_VALUE);
            MAP_INSTANCE.put(PROPERTY_B_NAME, PROPERTY_B_VALUE);
        }

        public static void assertEqualTo(Map<String, ByteString> map, boolean dummy) {
            Map<String, byte[]> byteMap =
                    CollectionConverter.convertMapValues(
                            map,
                            ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY);

            assertEqualTo(byteMap);
        }

        public static void assertEqualTo(Map<String, byte[]> map) {
            assertThat(map).hasSize(2);
            assertThat(map.get(PROPERTY_A_NAME)).isEqualTo(PROPERTY_A_VALUE);
            assertThat(map.get(PROPERTY_B_NAME)).isEqualTo(PROPERTY_B_VALUE);
        }
    }

    public static final class ValidFacebookCredential {
        public final static String ID = "bob@facebook.com";
        public final static String ID_TOKEN = "'His name is bob' - xoxo facebook";
        public final static AuthenticationMethod AUTHENTICATION_METHOD =
                AuthenticationMethods.FACEBOOK;
        public final static String PASSWORD = "hunter2";
        public final static String DISPLAY_NAME = "Bob";
        public final static Uri DISPLAY_PICTURE_URL =
                Uri.parse("https://pictures.facebook.com/bob_pics.jpeg");
        public final static Map<String, byte[]> ADDITIONAL_PROPERTIES =
                ValidProperties.MAP_INSTANCE;
        public final static String AUTHENTICATION_DOMAIN_STRING = "https://accounts.google.com";
        public final static AuthenticationDomain AUTHENTICATION_DOMAIN =
                new AuthenticationDomain(AUTHENTICATION_DOMAIN_STRING);

        public static Credential make() {
            return new Credential.Builder(ID, AUTHENTICATION_METHOD, AUTHENTICATION_DOMAIN)
                    .setDisplayName(DISPLAY_NAME)
                    .setDisplayPicture(DISPLAY_PICTURE_URL)
                    .setPassword(PASSWORD)
                    .setIdToken(ID_TOKEN)
                    .setAdditionalProperties(ADDITIONAL_PROPERTIES)
                    .build();
        }

        public static void assertEqualTo(Credential credential) {
            assertThat(credential.getIdentifier()).isEqualTo(ID);
            assertThat(credential.getIdToken()).isEqualTo(ID_TOKEN);
            assertThat(credential.getPassword()).isEqualTo(PASSWORD);
            assertThat(credential.getDisplayName()).isEqualTo(DISPLAY_NAME);
            assertThat(credential.getDisplayPicture()).isEqualTo(DISPLAY_PICTURE_URL);
            ValidProperties.assertEqualTo(credential.getAdditionalProperties());
            assertThat(credential.getAuthenticationMethod()).isEqualTo(AUTHENTICATION_METHOD);
            assertThat(credential.getAuthenticationDomain()).isEqualTo(AUTHENTICATION_DOMAIN);
        }

        public static void assertEqualTo(Protobufs.Credential credential) throws Exception {
            assertEqualTo(Credential.fromProtobuf(credential));
        }
    }

    public static final class ValidEmailHint {
        private static final String ALICE_ID = "alice@example.com";
        private static final String ALICE_NAME = "Alice";
        private static final String ALICE_DISPLAY_PICTURE_URI_STR = "https://avatars.example.com/alice";
        private static final Uri ALICE_DISPLAY_PICTURE_URI = Uri.parse(ALICE_DISPLAY_PICTURE_URI_STR);
        private static final String GENERATED_PASSWORD = "correctH0rseBatterySt4ple";
        private static final String ID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
                + "eyJzdWIiOiJ5b2xvIn0."
                + "BI1g9ns0shv6PKfwlhfPKwh5XzxQyg_el_35_wZbtsI";

        public static Hint make() {
            return new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL)
                    .setDisplayName(ALICE_NAME)
                    .setDisplayPictureUri(ALICE_DISPLAY_PICTURE_URI)
                    .setGeneratedPassword(GENERATED_PASSWORD)
                    .setIdToken(ID_TOKEN)
                    .build();
        }

        public static void assertEquals(Context context, Credential credential) {
            assertThat(credential.getIdentifier()).isEqualTo(ALICE_ID);
            assertThat(credential.getAuthenticationMethod()).isEqualTo(AuthenticationMethods.EMAIL);
            assertThat(credential.getDisplayName()).isEqualTo(ALICE_NAME);
            assertThat(credential.getDisplayPicture()).isEqualTo(ALICE_DISPLAY_PICTURE_URI);
            assertThat(credential.getPassword()).isEqualTo(GENERATED_PASSWORD);
            assertThat(credential.getIdToken()).isEqualTo(ID_TOKEN);
            assertThat(credential.getAuthenticationDomain())
                    .isEqualTo(AuthenticationDomain.getSelfAuthDomain(context));
        }

        public static void assertEquals(Hint hint) {
            assertThat(hint.getIdentifier()).isEqualTo(ALICE_ID);
            assertThat(hint.getAuthenticationMethod()).isEqualTo(AuthenticationMethods.EMAIL);
            assertThat(hint.getDisplayName()).isEqualTo(ALICE_NAME);
            assertThat(hint.getDisplayPictureUri()).isEqualTo(ALICE_DISPLAY_PICTURE_URI);
            assertThat(hint.getGeneratedPassword()).isEqualTo(GENERATED_PASSWORD);
            assertThat(hint.getIdToken()).isEqualTo(ID_TOKEN);
        }

        public static void assertEquals(Protobufs.Hint protoHint) {
            assertThat(protoHint.getId()).isEqualTo(ALICE_ID);
            assertThat(protoHint.getAuthMethod().getUri())
                    .isEqualTo(AuthenticationMethods.EMAIL.toString());
            assertThat(protoHint.getDisplayName()).isEqualTo(ALICE_NAME);
            assertThat(protoHint.getDisplayPictureUri()).isEqualTo(ALICE_DISPLAY_PICTURE_URI_STR);
            assertThat(protoHint.getGeneratedPassword()).isEqualTo(GENERATED_PASSWORD);
            assertThat(protoHint.getIdToken()).isEqualTo(ID_TOKEN);
        }
    }

    public static final class ValidApplication {
        public static final String PACKAGE_NAME = "com.example.app";
        private static final String FINGERPRINT =
                "2qKVvu1OLulMJAFbVq9ia08h759E8rPUD8QckJAKa_G0hnxDxXzaVNG2_Uhps_I8"
                        + "7V4Lo8BdCxaA307H0HYkAw==";
        private static byte[] SIGNATURE_BYTES = new byte[]
                { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };


        public static final class AuthDomain {
            private static final String AUTHENTICATION_DOMAIN_STRING =
                    "android://" + FINGERPRINT + "@" + PACKAGE_NAME;

            public static AuthenticationDomain make() {
                return new AuthenticationDomain(AUTHENTICATION_DOMAIN_STRING);
            }

            public static void assertEquals(AuthenticationDomain domain) {
                assertThat(domain).isEqualTo(make());
            }
        }

        public static Context install(Context context) throws Exception {
            PackageManager packageManager = mock(PackageManager.class);

            when(packageManager.getPackageInfo(eq(PACKAGE_NAME), eq(PackageManager.GET_SIGNATURES)))
                    .thenReturn(makePackageInfo());
            doThrow(PackageManager.NameNotFoundException.class)
                    .when(packageManager).getPackageInfo(
                            not(eq(PACKAGE_NAME)),
                            eq(PackageManager.GET_SIGNATURES));

            context = spy(context);
            when(context.getPackageName()).thenReturn(PACKAGE_NAME);
            when(context.getPackageManager()).thenReturn(packageManager);

            return context;
        }

        private static PackageInfo makePackageInfo() {
            PackageInfo packageInfo = new PackageInfo();
            packageInfo.signatures = new Signature[1];
            packageInfo.signatures[0] = new Signature(SIGNATURE_BYTES);

            return packageInfo;
        }
    }

    public static final class ApplicationWithMultipleSignatures {
        public static final String PACKAGE_NAME = "com.example.app.multiple_signatures";
        private static byte[] SIGNATURE_BYTES = new byte[]
                { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };

        public static Context install(Context context) throws Exception {
            PackageManager packageManager = mock(PackageManager.class);

            when(packageManager.getPackageInfo(eq(PACKAGE_NAME), eq(PackageManager.GET_SIGNATURES)))
                    .thenReturn(makePackageInfo());
            doThrow(PackageManager.NameNotFoundException.class)
                    .when(packageManager).getPackageInfo(
                    not(eq(PACKAGE_NAME)),
                    eq(PackageManager.GET_SIGNATURES));

            context = spy(context);
            when(context.getPackageName()).thenReturn(PACKAGE_NAME);
            when(context.getPackageManager()).thenReturn(packageManager);

            return context;
        }

        private static PackageInfo makePackageInfo() {
            PackageInfo packageInfo = new PackageInfo();
            packageInfo.signatures = new Signature[2];
            packageInfo.signatures[0] = new Signature(SIGNATURE_BYTES);
            packageInfo.signatures[1] = new Signature(SIGNATURE_BYTES);

            return packageInfo;
        }
    }
}
