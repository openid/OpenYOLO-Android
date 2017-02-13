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

package org.openyolo.api;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openyolo.api.internal.CollectionConverter;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.valid4j.errors.RequireViolation;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AuthenticationDomainTest {

    private static final String SELF_PACKAGE_NAME = "com.example.app";

    private byte[] sigBytes = new byte[]
            { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };

    private String sigSha512HashBase64 =
            "2qKVvu1OLulMJAFbVq9ia08h759E8rPUD8QckJAKa_G0hnxDxXzaVNG2_Uhps_I8"
                    + "7V4Lo8BdCxaA307H0HYkAw==";
    private PackageInfo packageInfo;

    @Mock
    PackageManager mockPackageManager;
    @Mock
    Context mockContext;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        packageInfo = new PackageInfo();
        packageInfo.signatures = new Signature[2];
        packageInfo.signatures[0] = new Signature(sigBytes);
        packageInfo.signatures[1] = new Signature(new byte[16]);

        when(mockPackageManager.getPackageInfo("com.example.app", PackageManager.GET_SIGNATURES))
                .thenReturn(packageInfo);

        when(mockContext.getPackageName()).thenReturn(SELF_PACKAGE_NAME);
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);

    }

    @Test
    public void testGetSelfAuthDomain() throws Exception {
        AuthenticationDomain authDomain = AuthenticationDomain.getSelfAuthDomain(mockContext);
        assertThat(authDomain).isNotNull();
        assertThat(authDomain.isAndroidAuthDomain()).isTrue();
        assertThat(authDomain.toString())
                .isEqualTo("android://" + sigSha512HashBase64 + "@com.example.app");
    }

    @Test
    public void testListForPackage() {
        assertThat(AuthenticationDomain.listForPackage(mockContext, "com.example.app")).contains(
                new AuthenticationDomain("android://" + sigSha512HashBase64 + "@com.example.app"));
    }

    @Test
    public void testListForPackage_nullPackageName() throws Exception {
        assertThat(AuthenticationDomain.listForPackage(mockContext, null)).isEmpty();
    }

    @Test
    public void testEquals() throws Exception {
        AuthenticationDomain authDomain = AuthenticationDomain.getSelfAuthDomain(mockContext);
        AuthenticationDomain authDomain2 = AuthenticationDomain.getSelfAuthDomain(mockContext);
        assertTrue(authDomain.equals(authDomain2));
    }

    @Test
    public void testEquals_differentType() throws Exception {
        AuthenticationDomain domain = new AuthenticationDomain("https://www.example.com");
        assertFalse(domain.equals("https://www.example.com"));
    }

    @Test
    public void testGetUri() throws Exception{
        AuthenticationDomain authDomain = AuthenticationDomain.getSelfAuthDomain(mockContext);
        String expectedUri = "android://" +
                "2qKVvu1OLulMJAFbVq9ia08h759E8rPUD8QckJAKa_G0hnxDxXzaVNG2_" +
                "Uhps_I87V4Lo8BdCxaA307H0HYkAw==@com.example.app";
        assertEquals(expectedUri, authDomain.getUri().toString());
    }

    @Test(expected = RequireViolation.class)
    public void testCreateAndroidAuthDomain_nullDomain() throws Exception{
        AuthenticationDomain.createAndroidAuthDomain(null, new Signature(""));
    }

    @Test
    public void testIsWebAuthDomain(){
        AuthenticationDomain authDomain = AuthenticationDomain.getSelfAuthDomain(mockContext);
        assertFalse(authDomain.isWebAuthDomain());
    }

    @Test
    public void testGetAndroidPackageName(){
        AuthenticationDomain authDomain = AuthenticationDomain.getSelfAuthDomain(mockContext);
        assertEquals("com.example.app", authDomain.getAndroidPackageName());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetAndroidPackageName_fromWebDomain() {
        AuthenticationDomain authDomain = new AuthenticationDomain("https://www.example.com");
        authDomain.getAndroidPackageName();
    }

    @Test
    public void stringsToAuthDomains() throws Exception {
        List<String> domains = new ArrayList<>();
        domains.add("https://www.google.com");
        domains.add("https://www.yahoo.com");
        domains.add("https://www.facebook.com");
        List<AuthenticationDomain> result =
                CollectionConverter.toList(
                        domains,
                        AuthenticationDomain.CONVERTER_STRING_TO_DOMAIN);
        assertEquals(3, result.size());
        assertEquals(result.get(0).getUri().toString(), domains.get(0));
        assertEquals(result.get(1).getUri().toString(), domains.get(1));
        assertEquals(result.get(2).getUri().toString(), domains.get(2));
    }

    @Test
    public void testGetHashCode(){
        AuthenticationDomain authDomain = AuthenticationDomain.getSelfAuthDomain(mockContext);
        assertEquals(authDomain.getUri().hashCode(), authDomain.hashCode());
    }

    @Test
    public void testCompareTo(){
        AuthenticationDomain authDomain = AuthenticationDomain.getSelfAuthDomain(mockContext);
        AuthenticationDomain authDomain1 = AuthenticationDomain.getSelfAuthDomain(mockContext);

        assertEquals(0, authDomain.compareTo(authDomain1));
    }
}
