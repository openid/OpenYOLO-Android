/*
 *
 *  Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package com.google.bbq;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.ArrayList;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;
import org.valid4j.errors.RequireViolation;

/**
 * Battery of tests for The intent handling utility within BBQ
 */
@RunWith(org.robolectric.RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class QueryUtilTest {

    @Mock
    private Context mockContext;
    @Mock
    private PackageManager mockPackageManager;

    private ArrayList<ResolveInfo> mPackageinfos;
    private ResolveInfo mockinfo2;
    private ResolveInfo mockinfo1;

    @org.junit.Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockinfo1=new ResolveInfo();
        mockinfo1.activityInfo = new android.content.pm.ActivityInfo();
        mockinfo1.activityInfo.packageName= "com.openyolo";

        mockinfo2=new ResolveInfo();
        mockinfo2.activityInfo = new android.content.pm.ActivityInfo();
        mockinfo2.activityInfo.packageName= "com.google";

        mPackageinfos = new java.util.ArrayList<>(2);
        mPackageinfos.add(mockinfo1);
        mPackageinfos.add(mockinfo2);

        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
        when(mockPackageManager.queryBroadcastReceivers(any(Intent.class),
                anyInt())).thenReturn(mPackageinfos);


    }

    @Test
    public void testCreateEmptyQueryIntent_ValidData() throws Exception {
        String mockCategory = "MockCategory";
        Intent result = QueryUtil.createEmptyQueryIntent(mockCategory);
        Set<String> categories = result.getCategories();
        assertEquals(1, categories.size());
        assertTrue(categories.contains(QueryUtil.BBQ_CATEGORY));
        result.getAction().equals(mockCategory);
    }
    @Test(expected = RequireViolation.class)
    public void testCreateEmptyQueryIntent_InvalidData() throws Exception {
        QueryUtil.createEmptyQueryIntent(null);
    }

    @Test
    public void testCreateResponseAction_ValidData() throws Exception {
        String dataType = "datatype";
        long requestId = 128L;
        String action = QueryUtil.createResponseAction(dataType, requestId);
        assertTrue((dataType + ":" + QueryUtil.longAsHex(requestId)).equals(action));
    }

    @Test
    public void testCreateResponseAction_InvalidData() throws Exception {
        String dataType = "datatype";
        long requestId = 128L;
        String action = QueryUtil.createResponseAction(dataType, requestId);
        assertTrue((dataType + ":" + QueryUtil.longAsHex(requestId)).equals(action));
    }

    @Test
    public void getRespondersForDataType() throws Exception {
        String dataType = "dataType";
        Set<String> result = QueryUtil.getRespondersForDataType(mockContext, dataType);
        assertEquals(2, result.size());
        assertTrue(result.contains(mockinfo1.activityInfo.packageName));
        assertTrue(result.contains(mockinfo2.activityInfo.packageName));
    }

}