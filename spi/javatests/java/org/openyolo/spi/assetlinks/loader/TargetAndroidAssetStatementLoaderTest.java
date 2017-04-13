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

package org.openyolo.spi.assetlinks.loader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openyolo.spi.assetlinks.data.RelationType.GetLoginCreds;
import static org.openyolo.spi.assetlinks.data.RelationType.HandleAllUrls;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.openyolo.spi.assetlinks.data.AndroidAssetStatement;
import org.openyolo.spi.assetlinks.data.AndroidTarget;
import org.openyolo.spi.assetlinks.data.AssetStatement;
import org.openyolo.spi.assetlinks.data.RelationType;
import org.openyolo.spi.assetlinks.data.WebSiteAssetStatement;
import org.openyolo.spi.assetlinks.data.WebTarget;

/**
 * Tests for {@link TargetAndroidAssetStatementLoader}.
 */
public class TargetAndroidAssetStatementLoaderTest {
    @Test
    public void containsRelation() throws Exception {
        AndroidTarget androidTarget = createAndroidTarget
                     ("org.example", "D5:03:9F:44:48:4A:70:C9:0A:AC:1D:3A:A7:67:07:EE:B8:BF:52:03" +
                                 ":62:87:0C:98:B0:C9:E2:2A:8E:14:DF:A8");

        final AndroidAssetStatement assetStatement = new AndroidAssetStatement.Builder()
                    .relations(Arrays.asList(GetLoginCreds, HandleAllUrls))
                    .target(androidTarget)
                    .build();

        final boolean containsRelation = TargetAndroidAssetStatementLoader.containsRelation
                    (assetStatement, GetLoginCreds);

        assertTrue(containsRelation);
    }

    @Test
    public void filterAssetStatementsByRelation() throws Exception {
        AndroidTarget androidTarget1 = createAndroidTarget
                    ("org.example", "D5:03:9F:44:48:4A:70:C9:0A:AC:1D:3A:A7:67:07:EE:B8:BF:52:03" +
                                ":62:87:0C:98:B0:C9:E2:2A:8E:14:DF:A8");
        final AssetStatement assetStmt1 = createAndroidAssetStatement(Arrays.asList
                    (GetLoginCreds, HandleAllUrls), androidTarget1);

        AndroidTarget androidTarget2 = createAndroidTarget
                    ("foo.example", "03:9F:9F:44:48:4A:70:C9:0A:AC:1D:3A:A7:67:07:EE:B8:BF:52:03" +
                                ":62:87:0C:98:B0:C9:E2:2A:8E:14:DF:A8");
        final AssetStatement assetStmt2 = createAndroidAssetStatement(Arrays.asList
                    (HandleAllUrls), androidTarget2);

        List<AssetStatement> assetStatements = Arrays.asList(assetStmt1,
                    assetStmt2);

        final List<AssetStatement> filteredStmts = TargetAndroidAssetStatementLoader
                    .filterAssetStatements(assetStatements, GetLoginCreds);

        assertNotNull(filteredStmts);
        assertTrue(filteredStmts.size() == 1);
        assertTrue(filteredStmts.get(0).getRelations().contains(GetLoginCreds));
    }

    @Test
    public void filterAssetStatementsByType() throws Exception {
        AndroidTarget androidTarget = createAndroidTarget
                    ("org.example", "D5:03:9F:44:48:4A:70:C9:0A:AC:1D:3A:A7:67:07:EE:B8:BF:52:03" +
                                ":62:87:0C:98:B0:C9:E2:2A:8E:14:DF:A8");

        final AndroidAssetStatement assetStatement = new AndroidAssetStatement.Builder()
                    .relations(Arrays.asList(GetLoginCreds, HandleAllUrls))
                    .target(androidTarget)
                    .build();

        final WebTarget webTarget = createWebTarget("com.example");
        final AssetStatement webAssetStatement = createWebAssetStatement(webTarget,
                    Arrays.asList(GetLoginCreds, HandleAllUrls));

        List<AssetStatement> assetStatements = Arrays.asList(assetStatement,
                    webAssetStatement);

        final List<AssetStatement> filteredStmts = TargetAndroidAssetStatementLoader
                    .filterAssetStatements(assetStatements,
                    AndroidAssetStatement.class);

        assertNotNull(filteredStmts);
        assertTrue(filteredStmts.size() == 1);
        assertTrue(filteredStmts.get(0) instanceof AndroidAssetStatement);
    }

    private WebTarget createWebTarget(String site) {
        return new WebTarget.Builder()
                    .site(site)
                    .build();
    }

    private AndroidTarget createAndroidTarget(String packageName, String fingerprint) {
        return new AndroidTarget.Builder()
                    .packageName(packageName)
                    .sha256CertFingerprint(fingerprint)
                    .build();
    }

    private AssetStatement createAndroidAssetStatement(List<RelationType> relations,
                AndroidTarget androidTarget) {
        return new AndroidAssetStatement.Builder()
                    .relations(relations)
                    .target(androidTarget)
                    .build();
    }

    private AssetStatement createWebAssetStatement(WebTarget webTarget, List<RelationType> relations) {
        return new WebSiteAssetStatement.Builder()
                    .webTarget(webTarget)
                    .relations(relations)
                    .build();
    }
}