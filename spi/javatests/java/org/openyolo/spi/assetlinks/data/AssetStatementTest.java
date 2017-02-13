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

package org.openyolo.spi.assetlinks.data;

import static junit.framework.Assert.assertTrue;
import static org.openyolo.spi.assetlinks.data.RelationType.GetLoginCreds;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Tests for {@link AssetStatement}
 */
@RunWith(Parameterized.class)
public class AssetStatementTest {

    private static final String fingerprint =
                "D5:03:9F:44:48:4A:70:C9:0A:AC:1D:3A:A7:67:07:EE:B8:BF:52:03:62:87:0C:98:B0:C9" +
                            ":E2:2A:8E:14:DF:A8";

    private static final List<RelationType> relations = Arrays.asList(GetLoginCreds);

    public AssetStatement assetStatement;

    public AssetStatementTest(AssetStatement assetStatement) {
        this.assetStatement = assetStatement;
    }

    @Test
    public final void testRelationsMethod() {
        assertTrue(assetStatement.getRelations().size() == 1);
        assertTrue(assetStatement.getRelations().contains(GetLoginCreds));
    }

    @Parameterized.Parameters
    public static Collection<Object[]> instancesToTest() {
        return Arrays.asList(
                    new Object[]{createAndroidAssetStatement()},
                    new Object[]{createWebSiteAssetStatement()}
        );
    }

    private static WebSiteAssetStatement createWebSiteAssetStatement() {
        return new WebSiteAssetStatement.Builder()
                    .relations(relations)
                    .webTarget(createWebTarget())
                    .build();
    }

    private static WebTarget createWebTarget() {
        return new WebTarget.Builder()
                    .site("org.example")
                    .build();
    }

    private static AndroidAssetStatement createAndroidAssetStatement() {
        return new AndroidAssetStatement.Builder()
                    .target(createAndroidTarget())
                    .relations(relations)
                    .build();
    }

    private static AndroidTarget createAndroidTarget() {
        return new AndroidTarget.Builder()
                    .packageName("org.example")
                    .sha256CertFingerprint(fingerprint)
                    .build();
    }

}
