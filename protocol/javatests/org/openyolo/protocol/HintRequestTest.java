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
import static org.assertj.core.data.MapEntry.entry;

import android.net.Uri;
import android.os.Parcel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.AuthenticationMethods;
import org.openyolo.protocol.HintRequest;
import org.openyolo.protocol.IdentifierTypes;
import org.openyolo.protocol.PasswordSpecification;
import org.openyolo.protocol.Protobufs;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.valid4j.errors.RequireViolation;

/**
 * Tests for {@link HintRequest}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class HintRequestTest {

    @Test
    public void testBuild() {
        PasswordSpecification originalSpec = new PasswordSpecification.Builder()
                .ofLength(8, 10)
                .allow(PasswordSpecification.ALPHANUMERIC)
                .require(PasswordSpecification.NUMERALS, 1)
                .build();

        HintRequest request = new HintRequest.Builder(
                AuthenticationMethods.ID_AND_PASSWORD,
                AuthenticationMethods.GOOGLE)
                .addAuthenticationMethod(AuthenticationMethods.FACEBOOK)
                .setIdentifierTypes(IdentifierTypes.EMAIL)
                .addIdentifierType(IdentifierTypes.PHONE)
                .addAdditionalProperty("a", new byte[] { 1, 2, 3})
                .addAdditionalProperty("b", "hello")
                .setPasswordSpecification(originalSpec)
                .build();

        assertThat(request.getAuthenticationMethods())
                .containsOnly(
                        AuthenticationMethods.ID_AND_PASSWORD,
                        AuthenticationMethods.GOOGLE,
                        AuthenticationMethods.FACEBOOK);

        assertThat(request.getIdentifierTypes())
                .containsOnly(
                        IdentifierTypes.EMAIL,
                        IdentifierTypes.PHONE);

        assertThat(request.getAdditionalProperties())
                .containsOnly(
                        entry("a", new byte[] {1, 2, 3}),
                        entry("b", "hello".getBytes(Charset.forName("UTF-8"))));

        assertThat(originalSpec).isEqualTo(request.getPasswordSpecification());

        assertThat(request.getAdditionalProperty("a")).isEqualTo(new byte[] {1, 2, 3});
        assertThat(request.getAdditionalPropertyAsString("b")).isEqualTo("hello");

        assertThat(request.getAdditionalProperty("c")).isNull();
        assertThat(request.getAdditionalPropertyAsString("d")).isNull();
    }

    @Test
    public void testBuild_overwriteAuthenticationMethods() {
        HintRequest request = new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setAuthenticationMethods(
                        AuthenticationMethods.GOOGLE,
                        AuthenticationMethods.FACEBOOK)
                .build();

        assertThat(request.getAuthenticationMethods())
                .containsOnly(AuthenticationMethods.GOOGLE, AuthenticationMethods.FACEBOOK);
    }

    @Test
    public void testBuild_overwriteIdentityTypes() {
        HintRequest request = new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setIdentifierTypes(IdentifierTypes.PHONE)
                .setIdentifierTypes(IdentifierTypes.EMAIL, IdentifierTypes.ALPHANUMERIC)
                .build();

        assertThat(request.getIdentifierTypes())
                .containsOnly(IdentifierTypes.EMAIL, IdentifierTypes.ALPHANUMERIC);
    }

    @Test
    public void testForEmailAndPasswordAccount() {
        HintRequest request = HintRequest.forEmailAndPasswordAccount();
        assertThat(request.getAuthenticationMethods())
                .containsOnly(AuthenticationMethods.ID_AND_PASSWORD);
        assertThat(request.getIdentifierTypes())
                .containsOnly(IdentifierTypes.EMAIL);
        assertThat(request.getPasswordSpecification())
                .isEqualTo(PasswordSpecification.DEFAULT);
        assertThat(request.getAdditionalProperties()).isEmpty();
    }

    @Test
    public void testSerialize() {
        HintRequest request = new HintRequest.Builder(
                AuthenticationMethods.ID_AND_PASSWORD,
                AuthenticationMethods.FACEBOOK)
                .setIdentifierTypes(IdentifierTypes.EMAIL, IdentifierTypes.ALPHANUMERIC)
                .setPasswordSpecification(
                        new PasswordSpecification.Builder()
                                .ofLength(10, 100)
                                .allow(PasswordSpecification.ALPHANUMERIC)
                                .require(PasswordSpecification.NUMERALS, 1)
                                .build())
                .build();

        Parcel p = Parcel.obtain();
        try {
            request.writeToParcel(p, 0);
            p.setDataPosition(0);
            HintRequest read = HintRequest.CREATOR.createFromParcel(p);

            assertThat(read.getAuthenticationMethods())
                    .containsOnlyElementsOf(request.getAuthenticationMethods());
            assertThat(read.getIdentifierTypes())
                    .containsOnlyElementsOf(request.getIdentifierTypes());
            assertThat(read.getPasswordSpecification())
                    .isEqualTo(request.getPasswordSpecification());
        } finally {
            p.recycle();
        }
    }

    /* **************************** constraint violation test cases *******************************/

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_constructor_nullRequestProto() {
        new HintRequest.Builder((Protobufs.HintRetrieveRequest) null).build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_constructor_nullAuthenticationMethod() {
        new HintRequest.Builder((Uri) null).build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_constructor_nullString() {
        new HintRequest.Builder((String) null).build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_constructor_nullUriSet() {
        new HintRequest.Builder((Set<Uri>) null).build();
    }

    @Test(expected = RequireViolation.class)
    public void testBuild_constructor_nullUriInVarargs() {
        new HintRequest.Builder(
                AuthenticationMethods.ID_AND_PASSWORD,
                null,
                AuthenticationMethods.GOOGLE).build();
    }

    @Test(expected = RequireViolation.class)
    public void testBuild_constructor_nullStringInVarargs() {
        new HintRequest.Builder(
                "custom://auth-method",
                null,
                "custom://another-auth-method")
                .build();
    }

    @Test(expected = RequireViolation.class)
    public void testBuild_constructor_invalidAuthMethodString() {
        new HintRequest.Builder("notAnAuthMethod").build();
    }

    @Test(expected = RequireViolation.class)
    public void testBuild_constructor_emptySet() {
        new HintRequest.Builder(new HashSet<Uri>()).build();
    }

    @Test(expected = RequireViolation.class)
    public void testBuild_constructor_setContainingNull() {
        HashSet<Uri> authMethods = new HashSet<>();
        authMethods.add(null);
        new HintRequest.Builder(new HashSet<>(authMethods)).build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setAuthenticationMethods_nullUri() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setAuthenticationMethods((Uri) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setAuthenticationMethods_nullUriInVarargs() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setAuthenticationMethods(
                        AuthenticationMethods.ID_AND_PASSWORD,
                        null,
                        AuthenticationMethods.GOOGLE)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setAuthenticationMethods_nullString() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setAuthenticationMethods((String) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setAuthenticationMethods_nullStringInVarargs() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setAuthenticationMethods(
                        "custom://auth-method",
                        null,
                        "custom://another-auth-method")
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setAuthenticationMethods_nullSet() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setAuthenticationMethods((Set<Uri>) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setAuthenticationMethods_emptySet() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setAuthenticationMethods(new HashSet<Uri>())
                .build();
    }

    @Test(expected = RequireViolation.class)
    public void testBuild_setAuthenticationMethods_setContainingNull() {
        HashSet<Uri> authMethods = new HashSet<>();
        authMethods.add(null);
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setAuthenticationMethods(authMethods)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addAuthenticationMethod_nullUri() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .addAuthenticationMethod((Uri) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addAuthenticationMethod_nullString() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .addAuthenticationMethod((String) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addAuthenticationMethod_emptyString() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .addAuthenticationMethod("")
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addAuthenticationMethod_invalidString() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .addAuthenticationMethod("notAnAuthMethod")
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setIdentifierTypes_nullUri() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setIdentifierTypes((Uri) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setIdentifierTypes_nullUriInVarargs() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setIdentifierTypes(
                        IdentifierTypes.EMAIL,
                        null,
                        IdentifierTypes.PHONE)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setIdentifierTypes_nullString() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setIdentifierTypes((String) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setIdentifierTypes_nullStringInVarargs() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setIdentifierTypes(
                        "custom://id-type",
                        null,
                        "custom://another-id-type")
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setIdentifierTypes_nullSet() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setIdentifierTypes((Set<Uri>) null)
                .build();
    }

    public void testBuild_setIdentifierTypes_emptySet() {
        HintRequest request = new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setIdentifierTypes(new HashSet<Uri>())
                .build();

        assertThat(request.getIdentifierTypes()).isEmpty();
    }

    @Test(expected = RequireViolation.class)
    public void testBuild_setIdentifierTypes_setContainingNull() {
        HashSet<Uri> idTypes = new HashSet<>();
        idTypes.add(null);
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setIdentifierTypes(idTypes)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addIdentifierType_nullUri() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .addIdentifierType((Uri) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addIdentifierType_nullString() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .addIdentifierType((String) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addIdentifierType_emptyString() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .addIdentifierType("")
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addIdentifierType_invalidString() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .addIdentifierType("notAnIdTpye")
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setAdditionalProperties_nullMap() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setAdditionalProperties(null)
                .build();
    }

    @Test(expected = RequireViolation.class)
    public void testBuild_setAdditionalProperties_nullKey() {
        HashMap<String, byte[]> additionalProps = new HashMap<>();
        additionalProps.put(null, new byte[0]);
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setAdditionalProperties(additionalProps)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setAdditionalProperty_nullValue() {
        HashMap<String, byte[]> additionalProps = new HashMap<>();
        additionalProps.put("a", null);
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setAdditionalProperties(additionalProps)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addAdditionalProperty_nullKey() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .addAdditionalProperty(null, "value")
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addAdditionalProperty_emptyKey() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .addAdditionalProperty("", "value")
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addAdditionalProperty_nullByteArrayValue() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .addAdditionalProperty("key", (byte[]) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addAdditionalProperty_nullStringValue() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .addAdditionalProperty("key", (String) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setPasswordSpecification_null() {
        new HintRequest.Builder(AuthenticationMethods.ID_AND_PASSWORD)
                .setPasswordSpecification(null)
                .build();
    }
}