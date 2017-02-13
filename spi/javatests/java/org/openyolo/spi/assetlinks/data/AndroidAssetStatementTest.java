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

import org.junit.Test;
import org.valid4j.errors.RequireViolation;

import java.util.Arrays;

/**
 * Tests for {@link AndroidAssetStatement).
 */
public class AndroidAssetStatementTest {
    private final String fingerprint =
                "D5:03:9F:44:48:4A:70:C9:0A:AC:1D:3A:A7:67:07:EE:B8:BF:52:03:62:87:0C:98:B0:C9" +
                            ":E2:2A:8E:14:DF:A8";

    @Test (expected = RequireViolation.class)
    public void testNullRelations() {
        new AndroidAssetStatement.Builder()
                    .target(createAndroidTarget())
                    .build();
    }

    @Test (expected = RequireViolation.class)
    public void testNullTarget() {
        new AndroidAssetStatement.Builder()
                    .relations(Arrays.asList(RelationType.GetLoginCreds))
                    .build();
    }

    private AndroidTarget createAndroidTarget() {
        return new AndroidTarget.Builder()
                    .packageName("org.example")
                    .sha256CertFingerprint(fingerprint)
                    .build();
    }
}
