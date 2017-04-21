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
                AuthenticationMethods.EMAIL,
                AuthenticationMethods.GOOGLE)
                .addAuthenticationMethod(AuthenticationMethods.FACEBOOK)
                .addAdditionalProperty("a", new byte[] { 1, 2, 3})
                .addAdditionalProperty("b", "hello")
                .setPasswordSpecification(originalSpec)
                .build();

        assertThat(request.getAuthenticationMethods())
                .containsOnly(
                        AuthenticationMethods.EMAIL,
                        AuthenticationMethods.GOOGLE,
                        AuthenticationMethods.FACEBOOK);


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
        HintRequest request = new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .setAuthenticationMethods(
                        AuthenticationMethods.GOOGLE,
                        AuthenticationMethods.FACEBOOK)
                .build();

        assertThat(request.getAuthenticationMethods())
                .containsOnly(AuthenticationMethods.GOOGLE, AuthenticationMethods.FACEBOOK);
    }

    @Test
    public void testForEmailAndPasswordAccount() {
        HintRequest request = HintRequest.forEmailAndPasswordAccount();
        assertThat(request.getAuthenticationMethods())
                .containsOnly(AuthenticationMethods.EMAIL);
        assertThat(request.getPasswordSpecification())
                .isEqualTo(PasswordSpecification.DEFAULT);
        assertThat(request.getAdditionalProperties()).isEmpty();
    }

    @Test
    public void testSerialize() {
        HintRequest request = new HintRequest.Builder(
                AuthenticationMethods.EMAIL,
                AuthenticationMethods.FACEBOOK)
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
                AuthenticationMethods.EMAIL,
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
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .setAuthenticationMethods((Uri) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setAuthenticationMethods_nullUriInVarargs() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .setAuthenticationMethods(
                        AuthenticationMethods.EMAIL,
                        null,
                        AuthenticationMethods.GOOGLE)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setAuthenticationMethods_nullString() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .setAuthenticationMethods((String) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setAuthenticationMethods_nullStringInVarargs() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .setAuthenticationMethods(
                        "custom://auth-method",
                        null,
                        "custom://another-auth-method")
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setAuthenticationMethods_nullSet() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .setAuthenticationMethods((Set<Uri>) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setAuthenticationMethods_emptySet() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .setAuthenticationMethods(new HashSet<Uri>())
                .build();
    }

    @Test(expected = RequireViolation.class)
    public void testBuild_setAuthenticationMethods_setContainingNull() {
        HashSet<Uri> authMethods = new HashSet<>();
        authMethods.add(null);
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .setAuthenticationMethods(authMethods)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addAuthenticationMethod_nullUri() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .addAuthenticationMethod((Uri) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addAuthenticationMethod_nullString() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .addAuthenticationMethod((String) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addAuthenticationMethod_emptyString() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .addAuthenticationMethod("")
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addAuthenticationMethod_invalidString() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .addAuthenticationMethod("notAnAuthMethod")
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setAdditionalProperties_nullMap() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .setAdditionalProperties(null)
                .build();
    }

    @Test(expected = RequireViolation.class)
    public void testBuild_setAdditionalProperties_nullKey() {
        HashMap<String, byte[]> additionalProps = new HashMap<>();
        additionalProps.put(null, new byte[0]);
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .setAdditionalProperties(additionalProps)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setAdditionalProperty_nullValue() {
        HashMap<String, byte[]> additionalProps = new HashMap<>();
        additionalProps.put("a", null);
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .setAdditionalProperties(additionalProps)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addAdditionalProperty_nullKey() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .addAdditionalProperty(null, "value")
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addAdditionalProperty_emptyKey() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .addAdditionalProperty("", "value")
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addAdditionalProperty_nullByteArrayValue() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .addAdditionalProperty("key", (byte[]) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addAdditionalProperty_nullStringValue() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .addAdditionalProperty("key", (String) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setPasswordSpecification_null() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .setPasswordSpecification(null)
                .build();
    }
}