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

package org.openyolo.spi;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import com.google.bbq.proto.BroadcastQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import okio.ByteString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openyolo.api.AuthenticationDomain;
import org.openyolo.api.RetrieveRequest;
import org.openyolo.proto.CredentialRetrieveRequest;
import org.openyolo.proto.KeyValuePair;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Battery of tests for BaseCredentialQueryReceiver
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BaseCredentialQueryReceiverTest {
    private BaseCredentialQueryReceiver underTest;
    @Mock
    Context mockContext;
    @Mock
    PackageManager mockPackageManager;
    @Mock
    Intent mockIntent;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        underTest = new BaseCredentialQueryReceiver("testTage") {
            @Override
            protected void processCredentialRequest(
                    @NonNull Context context,
                    @NonNull BroadcastQuery query,
                    @NonNull RetrieveRequest request,
                    @NonNull Set<AuthenticationDomain> requestorDomains) {
                assertNotNull(context);
                assertNotNull(query);
                assertNotNull(request);
                assertNotNull(requestorDomains);
            }
        };
        PackageInfo mockPackageInfo = new PackageInfo();
        Signature[] signatures = new Signature[1];
        signatures[0] = mock(Signature.class);
        when(signatures[0].toByteArray()).thenReturn(new byte[10]);
        mockPackageInfo.signatures = signatures;
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
        when(mockPackageManager.getPackageInfo(anyString(), anyInt())).thenReturn(mockPackageInfo);

    }

    @Test
    public void testOnReceive() throws Exception {
        underTest.onReceive(mockContext, mockIntent);
    }

    @Test
    public void testProcessQuery_nullPointer() throws Exception {

        BroadcastQuery local = new BroadcastQuery.Builder()
                .requestingApp("org.openyolo")
                .dataType("blah")
                .requestId(101L)
                .responseId(102L)
                .queryMessage(null)
                .build();
        underTest.processQuery(mockContext, local);
    }

    @Test
    public void testProcessQuery_nonNull() throws Exception {

        List<String> authDomains = new ArrayList<>();
        authDomains.add("https://www.google.com");
        authDomains.add("https://www.yahoo.com");
        List<String> authMethods = new ArrayList<>();
        authMethods.add("custom://one");
        authMethods.add("custom://two");
        List<KeyValuePair> additionalParams = new ArrayList<>();
        CredentialRetrieveRequest query = new CredentialRetrieveRequest(
                authDomains,
                authMethods,
                additionalParams,
                ByteString.EMPTY);

        byte[] data = CredentialRetrieveRequest.ADAPTER.encode(query);
        BroadcastQuery local = new BroadcastQuery.Builder()
                .requestingApp("org.openyolo")
                .dataType("blah")
                .requestId(101L)
                .responseId(102L)
                .queryMessage(ByteString.of(data))
                .build();
        underTest.processQuery(mockContext, local);
    }
}