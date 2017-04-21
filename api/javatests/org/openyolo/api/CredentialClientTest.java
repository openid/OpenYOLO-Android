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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openyolo.protocol.AuthenticationMethods.EMAIL;
import static org.openyolo.protocol.ProtocolConstants.EXTRA_CREDENTIAL;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.AuthenticationMethods;
import org.openyolo.protocol.Credential;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Battery of tests for CredentialClient
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CredentialClientTest {
    @Mock
    private Context mockContext;

    private CredentialClient underTest;
    public static final String EMAIL_ID = "alice@example.com";
    public static final AuthenticationDomain AUTH_DOMAIN =
            new AuthenticationDomain("https://www.example.com");
    private Credential testCredentials;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        underTest = new CredentialClient(mockContext);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        testCredentials = new Credential.Builder(
                EMAIL_ID,
                EMAIL,
                new AuthenticationDomain("https://www.example.com")).build();
    }

    @Test
    public void testGetApplicationBoundInstance() throws Exception {
        CredentialClient test = CredentialClient.getApplicationBoundInstance(mockContext);
        assertNotNull(test);
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void getSaveIntent() throws Exception {
        PackageManager mockPackageManager = mock(PackageManager.class);
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
        List<ResolveInfo> response = new ArrayList<>();
        ResolveInfo info = new ResolveInfo();
        ActivityInfo mockActivityInfo = new ActivityInfo();
        mockActivityInfo.packageName =  "com.dashlane";
        mockActivityInfo.name =  "Dashlane";
        info.activityInfo = mockActivityInfo;
        response.add(info);
        when(mockPackageManager.queryIntentActivities(any(Intent.class), anyInt())).thenReturn(response);

        Intent i = underTest.getSaveIntent(testCredentials);
        assertNotNull(i);
        assertTrue(i.getExtras().getParcelableArrayList("providerIntents").size()==1);

    }

    @Test
    public void getCredentialFromActivityResult_noExtras() throws Exception {
        Intent intent = new Intent();

        Credential result = underTest.getCredentialFromActivityResult(intent);
        assertNull(result);
    }

    @Test
    public void getCredentialFromActivityResult_withExtras() throws Exception {
        Intent intent = new Intent();
        byte[] array = testCredentials.getProto().toByteArray();
        intent.putExtra(EXTRA_CREDENTIAL, array);
        Credential result = underTest.getCredentialFromActivityResult(intent);
        assertEquals(result.getIdentifier(), "alice@example.com");
        assertEquals(result.getAuthenticationMethod(), AuthenticationMethods.EMAIL);
        assertEquals(result.getAuthenticationDomain(), AUTH_DOMAIN);
    }
}