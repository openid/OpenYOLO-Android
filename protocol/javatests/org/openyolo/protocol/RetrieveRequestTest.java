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

import android.net.Uri;
import android.os.Parcel;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Battery of tests for RetrieveRequest
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RetrieveRequestTest {

    private static final byte[] testBytes = new byte[]
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

    private RetrieveRequest request;

    @Before
    public void setUp() {
        request = new RetrieveRequest.Builder(AuthenticationMethods.EMAIL)
                .addAdditionalProperty("a", "b")
                .addAdditionalProperty("c", testBytes)
                .build();
    }

    @Test
    public void testWriteAndRead() {
        Parcel p = Parcel.obtain();
        try {
            request.writeToParcel(p, 0);
            p.setDataPosition(0);
            RetrieveRequest deserialized = RetrieveRequest.CREATOR.createFromParcel(p);
            assertThat(deserialized).isNotNull();
            assertThat(deserialized.getAuthenticationMethods())
                    .isEqualTo(request.getAuthenticationMethods());
            assertMapsEqual(
                    deserialized.getAdditionalProperties(),
                    request.getAdditionalProperties());
        } finally {
            p.recycle();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderUriSetConstructor_withEmptySet_throwsIllegalArgumentException() {
        new RetrieveRequest.Builder(new HashSet<Uri>()  /* authenticationMethods */);
    }

    @Test(expected = IllegalArgumentException.class)
    public void builderProtoConstructor_withNull_throwsIllegalArgumentException() {
        new RetrieveRequest.Builder((Protobufs.CredentialRetrieveRequest) null);
    }

    @Test
    public void forAuthenticationMethods_withValidAuthenticationMethodsUsingVarArgs_returnsValidRequest() {
        RetrieveRequest request = RetrieveRequest
            .forAuthenticationMethods(AuthenticationMethods.GOOGLE, AuthenticationMethods.FACEBOOK);

        assertThat(request.getAuthenticationMethods())
            .containsOnly(AuthenticationMethods.GOOGLE, AuthenticationMethods.FACEBOOK);
    }

    @Test
    public void forAuthenticationMethods_withValidAuthenticationMethodUsingSet_returnsValidRequest() {
        Set<Uri> authenticationMethods = new HashSet<>();
        Collections.addAll(
            authenticationMethods,
            AuthenticationMethods.GOOGLE,
            AuthenticationMethods.FACEBOOK);

        RetrieveRequest request = RetrieveRequest.forAuthenticationMethods(authenticationMethods);

        assertThat(request.getAuthenticationMethods())
            .containsOnly(AuthenticationMethods.GOOGLE, AuthenticationMethods.FACEBOOK);
    }

    @Test
    public void testGetAdditionalProperties() {
        assertThat(request.getAdditionalProperties()).isNotNull();
        assertThat(request.getAdditionalProperties()).hasSize(2);
        assertThat(request.getAdditionalPropertyAsString("a")).isEqualTo("b");
        assertThat(request.getAdditionalProperty("c")).isEqualTo(testBytes);
    }

    @Test
    public void testGetAuthMethods() {
        assertThat(request.getAuthenticationMethods()).hasSize(1);
        assertThat(request.getAuthenticationMethods())
                .containsOnly(AuthenticationMethods.EMAIL);
    }

    @Test(expected = NullPointerException.class)
    public void testAdapter_nullArray() throws IOException {
        Protobufs.CredentialRetrieveRequest.parseFrom((byte[]) null);
    }

    private static <T, U> void assertMapsEqual(Map<T, U> a, Map<T, U> b) {
        assertThat(a.keySet()).isEqualTo(b.keySet());
        for (T key : a.keySet()) {
            assertThat(a.get(key)).isEqualTo(b.get(key));
        }
    }
}