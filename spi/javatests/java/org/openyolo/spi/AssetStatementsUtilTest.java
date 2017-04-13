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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Battery of tests for AssetStatementsUtil
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AssetStatementsUtilTest {
    private static final String ASSET_STATEMENTS = "asset_statements";

    @Mock
    Context mockContext;
    @Mock
    PackageManager mockPackageManager;
    @Mock
    ApplicationInfo mockAppInfo;
    @Mock
    Resources mockResources;

    private static final String APPROVED_PACKAGE = "com.dashlane";
    private static final int KEY = 5;
    private static final String MOCK_RESPONSE = "mockResponse";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mockContext.getPackageManager())
                .thenReturn(mockPackageManager);
        when(mockPackageManager.getApplicationInfo(anyString(), anyInt()))
                .thenReturn(mockAppInfo);
        when(mockPackageManager.getResourcesForApplication(any(ApplicationInfo.class)))
                .thenReturn(mockResources);
        when(mockResources.getString(KEY)).thenReturn(MOCK_RESPONSE);
    }

    @Test
    public void testGetAssetStatements_metadata_null() throws Exception {
        mockAppInfo.metaData = null;
        String assetStatement = AssetStatementsUtil.getAssetStatements(mockContext,
                APPROVED_PACKAGE);
        assertNull(assetStatement);
    }

    @Test
    public void testGetAssetStatements_metadata_noKey() throws Exception {
        mockAppInfo.metaData = new Bundle();
        String assetStatement = AssetStatementsUtil.getAssetStatements(mockContext,
                APPROVED_PACKAGE);
        assertNull(assetStatement);
    }

    @Test
    public void testGetAssetStatements_metadata_withKey() throws Exception {
        mockAppInfo.metaData = new Bundle();
        mockAppInfo.metaData.putInt(ASSET_STATEMENTS, KEY);
        String assetStatement = AssetStatementsUtil.getAssetStatements(mockContext,
                APPROVED_PACKAGE);
        assertEquals(MOCK_RESPONSE, assetStatement);
    }

    @Test
    public void testGetAssetStatements_throwNameNotFoundExceptionOnAppInfo() throws Exception {
        mockAppInfo.metaData = new Bundle();
        mockAppInfo.metaData.putInt(ASSET_STATEMENTS, KEY);
        when(mockPackageManager.getApplicationInfo(anyString(), anyInt()))
                .thenThrow(PackageManager.NameNotFoundException.class);
        String assetStatement = AssetStatementsUtil.getAssetStatements(mockContext,
                APPROVED_PACKAGE);
        assertNull(assetStatement);
    }

    @Test
    public void getAssetStatements_throwNameNotFoundExceptionOnResources() throws Exception {
        mockAppInfo.metaData = new Bundle();
        mockAppInfo.metaData.putInt(ASSET_STATEMENTS, KEY);
        when(mockPackageManager.getResourcesForApplication(any(ApplicationInfo.class)))
                .thenThrow(PackageManager.NameNotFoundException.class);
        String assetStatement = AssetStatementsUtil.getAssetStatements(mockContext,
                APPROVED_PACKAGE);
        assertNull(assetStatement);
    }

}