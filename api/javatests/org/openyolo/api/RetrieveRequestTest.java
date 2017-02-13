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

package org.openyolo.api;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Parcel;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openyolo.proto.CredentialRetrieveRequest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Battery of tests for RetrieveRequest
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RetrieveRequestTest {

    private static final AuthenticationDomain DOMAIN = new AuthenticationDomain(
            "android://2qKVvu1OLulMJAFbVq9ia08h759E8rPUD8QckJAKa_G0hnxDxXzaVNG2_Uhps_I8" +
            "7V4Lo8BdCxaA307H0HYkAw==@com.example.app");

    private static final byte[] testBytes = new byte[]
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

    private RetrieveRequest request;

    @Before
    public void setUp() {
        request = new RetrieveRequest.Builder(DOMAIN)
                .setAuthenticationMethods(AuthenticationMethods.ID_AND_PASSWORD)
                .addAdditionalParameter("a", "b")
                .addAdditionalParameter("c", testBytes)
                .build();
    }

    @SuppressLint("PackageManagerGetSignatures")
    @Test
    public void testForSelf() throws Exception {
        Context mockContext = mock(Context.class);
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.signatures = new Signature[2];
        packageInfo.signatures[0] = new Signature(testBytes);
        packageInfo.signatures[1] = new Signature(new byte[16]);

        PackageManager pm = mock(PackageManager.class);
        when(pm.getPackageInfo("com.example.app", PackageManager.GET_SIGNATURES))
                .thenReturn(packageInfo);
        when(mockContext.getPackageName()).thenReturn("com.example.app");
        when(mockContext.getPackageManager()).thenReturn(pm);

        RetrieveRequest request = RetrieveRequest.forSelf(mockContext);

        assertThat(request).isNotNull();
        assertThat(request.getAuthenticationDomains().size()).isEqualTo(1);
        assertThat(request.getAuthenticationDomains()).contains(DOMAIN);
        assertThat(request.getAuthenticationMethods()).isEmpty();
        assertThat(request.getAdditionalParameters()).isEmpty();
    }

    @Test
    public void testWriteAndRead() {
        Parcel p = Parcel.obtain();
        try {
            request.writeToParcel(p, 0);
            p.setDataPosition(0);
            RetrieveRequest deserialized = RetrieveRequest.CREATOR.createFromParcel(p);
            assertThat(deserialized).isNotNull();
            assertThat(deserialized.getAuthenticationDomains())
                    .isEqualTo(request.getAuthenticationDomains());
            assertThat(deserialized.getAuthenticationMethods())
                    .isEqualTo(request.getAuthenticationMethods());
            assertMapsEqual(
                    deserialized.getAdditionalParameters(),
                    request.getAdditionalParameters());
        } finally {
            p.recycle();
        }
    }

    @Test
    public void testGetAdditionalParameters() {
        assertThat(request.getAdditionalParameters()).isNotNull();
        assertThat(request.getAdditionalParameters()).hasSize(2);
        assertThat(request.getAdditionalParameterAsString("a")).isEqualTo("b");
        assertThat(request.getAdditionalParameter("c")).isEqualTo(testBytes);
    }

    @Test
    public void testGetAuthDomains() {
        assertThat(request.getAuthenticationDomains()).hasSize(1);
        assertThat(request.getAuthenticationDomains()).contains(DOMAIN);
    }

    @Test
    public void testGetAuthMethods() {
        assertThat(request.getAuthenticationMethods()).hasSize(1);
        assertThat(request.getAuthenticationMethods())
                .contains(AuthenticationMethods.ID_AND_PASSWORD);
    }

    @Test(expected = NullPointerException.class)
    public void testAdapter_nullArray() throws IOException {
        CredentialRetrieveRequest.ADAPTER.decode((byte[]) null);
    }

    private static <T, U> void assertMapsEqual(Map<T, U> a, Map<T, U> b) {
        assertThat(a.keySet()).isEqualTo(b.keySet());
        for (T key : a.keySet()) {
            assertThat(a.get(key)).isEqualTo(b.get(key));
        }
    }
}