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
import org.openyolo.protocol.internal.ClientVersionUtil;
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
    public void testToProtocolBuffer_includesClientVersion() {
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
            HintRequest request = HintRequest.forEmailAndPasswordAccount();
            Protobufs.HintRetrieveRequest proto = request.toProtocolBuffer();
            assertThat(proto.hasClientVersion());
            assertThat(proto.getClientVersion().getVendor()).isEqualTo(vendor);
            assertThat(proto.getClientVersion().getMajor()).isEqualTo(major);
            assertThat(proto.getClientVersion().getMinor()).isEqualTo(minor);
            assertThat(proto.getClientVersion().getPatch()).isEqualTo(patch);
        } finally {
            ClientVersionUtil.setClientVersion(null);
        }
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
        new HintRequest.Builder((AuthenticationMethod) null).build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_constructor_nullUriSet() {
        new HintRequest.Builder((Set<AuthenticationMethod>) null).build();
    }

    @Test(expected = RequireViolation.class)
    public void testBuild_constructor_nullUriInVarargs() {
        new HintRequest.Builder(
                AuthenticationMethods.EMAIL,
                null,
                AuthenticationMethods.GOOGLE).build();
    }

    @Test(expected = RequireViolation.class)
    public void testBuild_constructor_nullInVarargs() {
        new HintRequest.Builder(
                AuthenticationMethods.EMAIL,
                null,
                AuthenticationMethods.PHONE)
                .build();
    }

    @Test(expected = RequireViolation.class)
    public void testBuild_constructor_emptySet() {
        new HintRequest.Builder(new HashSet<AuthenticationMethod>()).build();
    }

    @Test(expected = RequireViolation.class)
    public void testBuild_constructor_setContainingNull() {
        HashSet<AuthenticationMethod> authMethods = new HashSet<>();
        authMethods.add(null);
        new HintRequest.Builder(new HashSet<>(authMethods)).build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setAuthenticationMethods_nullUri() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .setAuthenticationMethods((AuthenticationMethod) null)
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
    public void testBuild_setAuthenticationMethods_nullSet() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .setAuthenticationMethods(null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_setAuthenticationMethods_emptySet() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .setAuthenticationMethods(new HashSet<AuthenticationMethod>())
                .build();
    }

    @Test(expected = RequireViolation.class)
    public void testBuild_setAuthenticationMethods_setContainingNull() {
        HashSet<AuthenticationMethod> authMethods = new HashSet<>();
        authMethods.add(null);
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .setAuthenticationMethods(authMethods)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void testBuild_addAuthenticationMethod_nullUri() {
        new HintRequest.Builder(AuthenticationMethods.EMAIL)
                .addAuthenticationMethod(null)
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