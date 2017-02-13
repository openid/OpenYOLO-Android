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

package com.google.bbq;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.valid4j.errors.RequireViolation;

/**
 * Battery of tests for BroadcastQueryClient
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BroadcastQueryClientTest {
    @Mock
    private Context mockContext;
    @Mock
    private PackageManager mockPackageManager;

    private BroadcastQueryClient underTest;
    private ArrayList<ResolveInfo> mPackageinfos;

    @org.junit.Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        underTest = new BroadcastQueryClient(mockContext);
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);

        ResolveInfo mockinfo1=new ResolveInfo();
        mockinfo1.activityInfo = new ActivityInfo();
        mockinfo1.activityInfo.packageName= "com.openyolo";

        ResolveInfo mockinfo2=new ResolveInfo();
        mockinfo2.activityInfo = new ActivityInfo();
        mockinfo2.activityInfo.packageName= "com.google";

        mPackageinfos = new ArrayList<>(2);
        mPackageinfos.add(mockinfo1);
        mPackageinfos.add(mockinfo2);

        when(mockPackageManager.queryBroadcastReceivers(any(Intent.class),
                anyInt())).thenReturn(mPackageinfos);
    }

    @Test
    public void queryFor() throws Exception {
        String dataType = "datatype";
        byte[] queryMessage = new byte[64];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(queryMessage);
        long timeoutInMs = 500L;
        QueryCallback callback = new QueryCallback() {
            @Override
            public void onResponse(long queryId, List<QueryResponse> responses) {
                //not expecting the callback as this is happening through broadcasts.
                //if we get here, fail so it raises some eyebrows...
                fail();
            }
        };

        underTest.queryFor(dataType, queryMessage, timeoutInMs, callback);
        verify(mockContext, times(1)).registerReceiver(any(BroadcastReceiver.class),
                any(IntentFilter.class));
        verify(mockContext, times(mPackageinfos.size())).sendBroadcast(any(Intent.class));

    }

    @Test(expected = RequireViolation.class)
    public void queryFor_negative_timestamp() throws Exception {
        String dataType = "datatype";
        byte[] queryMessage = new byte[64];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(queryMessage);
        long timeoutInMs = -500L;
        QueryCallback callback = new QueryCallback() {
            @Override
            public void onResponse(long queryId, List<QueryResponse> responses) {
                //not expecting the callback as this is happening through broadcasts.
                //if we get here, fail so it raises some eyebrows...
                fail();
            }
        };

        underTest.queryFor(dataType, queryMessage, timeoutInMs, callback);
    }

    @Test(expected = RequireViolation.class)
    public void queryFor_pushNullDatatype() throws Exception {
        String dataType = null;
        byte[] queryMessage = new byte[64];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(queryMessage);
        long timeoutInMs = 500L;
        QueryCallback callback = new QueryCallback() {
            @Override
            public void onResponse(long queryId, List<QueryResponse> responses) {
            }
        };

        underTest.queryFor(dataType, queryMessage, timeoutInMs, callback);

    }

    @Test(expected = RequireViolation.class)
    public void queryFor_pushNullCallback() throws Exception {
        String dataType = "datatype";
        byte[] queryMessage = new byte[64];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(queryMessage);
        long timeoutInMs = 500L;
        underTest.queryFor(dataType, queryMessage, timeoutInMs, null);

    }

}