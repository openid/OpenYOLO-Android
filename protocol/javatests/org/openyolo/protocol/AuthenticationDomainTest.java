/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openyolo.protocol;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.openyolo.protocol.TestConstants.INVALID_PROTO_BYTES;

import android.content.Context;
import android.content.pm.Signature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.TestConstants.ValidApplication;
import org.openyolo.protocol.TestConstants.ApplicationWithMultipleSignatures;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Unit tests for {@link AuthenticationDomain}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AuthenticationDomainTest {

    private static final String WEB_AUTH_DOMAIN_STR = "https://www.example.com";

    Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = ValidApplication.install(RuntimeEnvironment.application);
    }

    @Test
    public void toProtobufBytes_fromProtobufBytes_isEquivalent() throws Exception {
        byte[] encodedAuthDomain = ValidApplication.AuthDomain.make().toProtobuf().toByteArray();

        AuthenticationDomain authenticationDomain =
                AuthenticationDomain.fromProtobufBytes(encodedAuthDomain);

        assertThat(authenticationDomain).isEqualTo(ValidApplication.AuthDomain.make());
    }

    @Test(expected = MalformedDataException.class)
    public void fromProtobufBytes_withMalformedBytes_throwsMalformedDataException() throws Exception {
        AuthenticationDomain.fromProtobufBytes(INVALID_PROTO_BYTES);
    }

    @Test(expected = MalformedDataException.class)
    public void fromProtobufBytes_withInvalidUri_throwsInvalidProtoBufferException() throws Exception {
        byte[] invalidProtoBytes =
                Protobufs.AuthenticationDomain.newBuilder()
                        .setUri("Invalid uri")
                        .build()
                        .toByteArray();

        AuthenticationDomain.fromProtobufBytes(invalidProtoBytes);
    }

    @Test(expected = MalformedDataException.class)
    public void fromProtobuf_withInvalidUri_throwsInvalidProtoBufferException() throws Exception {
        Protobufs.AuthenticationDomain invalidProto =
            Protobufs.AuthenticationDomain.newBuilder()
                .setUri("Invalid uri")
                .build();

        AuthenticationDomain.fromProtobuf(invalidProto);
    }

    @Test
    public void getSelfAuthDomain_withValidInput_returnsValidDomain() throws Exception {
        AuthenticationDomain authDomain = AuthenticationDomain.getSelfAuthDomain(mContext);

        ValidApplication.AuthDomain.assertEquals(authDomain);
    }

    @Test
    public void fromPackageName_forValidPackage_containsValidAuthenticationDomain() {
        AuthenticationDomain authenticationDomain =
                AuthenticationDomain.fromPackageName(mContext, ValidApplication.PACKAGE_NAME);

        ValidApplication.AuthDomain.assertEquals(authenticationDomain);
    }

    @Test
    public void fromPackageName_packageDoesNotExist_returnsNull() throws Exception {
        assertThat(AuthenticationDomain.fromPackageName(mContext, "does.not.exist")).isNull();
    }

    @Test
    public void fromPackageName_hasMultipleSignatures_returnsNull() throws Exception {
        Context context = ApplicationWithMultipleSignatures.install(RuntimeEnvironment.application);

        AuthenticationDomain authenticationDomain =
                AuthenticationDomain.fromPackageName(
                        context,
                        ApplicationWithMultipleSignatures.PACKAGE_NAME);

        assertThat(authenticationDomain).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void CreateAndroidAuthDomain_withNullDomain_throwsIllegalArgumentException()
            throws Exception{
        AuthenticationDomain.createAndroidAuthDomain(null /* authDomain */, mock(Signature.class));
    }

    @Test
    public void validWebAuthDomain_hasExpectedBehaviour() {
        AuthenticationDomain authDomain = new AuthenticationDomain("https://www.example.com");

        assertThat(authDomain.isAndroidAuthDomain()).isFalse();
        assertThat(authDomain.isWebAuthDomain()).isTrue();

        try {
            authDomain.getAndroidPackageName();

            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException ex) {}
    }

    @Test
    public void getAndroidPackageName_withAndroidAuthDomain(){
        AuthenticationDomain authDomain = AuthenticationDomain.getSelfAuthDomain(mContext);

        assertThat(authDomain.getAndroidPackageName()).isEqualTo(ValidApplication.PACKAGE_NAME);
    }

    @Test
    public void equals_withEquivalent_returnsTrue() throws Exception {
        AuthenticationDomain authDomainA = AuthenticationDomain.getSelfAuthDomain(mContext);
        AuthenticationDomain authDomainB = AuthenticationDomain.getSelfAuthDomain(mContext);

        assertThat(authDomainA).isEqualTo(authDomainA);
        assertThat(authDomainA).isEqualTo(authDomainB);

        assertThat(authDomainA.compareTo(authDomainB)).isEqualTo(0);
    }

    @Test
    public void equals_withDifferentType_returnsFalse() throws Exception {
        AuthenticationDomain domain = new AuthenticationDomain(WEB_AUTH_DOMAIN_STR);
        assertThat(domain).isNotEqualTo(WEB_AUTH_DOMAIN_STR);
    }

    @Test
    public void testHashCode_equivalent() throws Exception {
        AuthenticationDomain authDomainA = new AuthenticationDomain(WEB_AUTH_DOMAIN_STR);
        AuthenticationDomain authDomainB = new AuthenticationDomain(WEB_AUTH_DOMAIN_STR);

        assertThat(authDomainA.hashCode()).isEqualTo(authDomainB.hashCode());
    }

}
