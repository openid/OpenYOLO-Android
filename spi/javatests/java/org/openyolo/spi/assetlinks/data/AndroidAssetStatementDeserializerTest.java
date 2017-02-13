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

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.valid4j.errors.RequireViolation;

import java.util.List;

/**
 * Tests for {@link AndroidAssetStatementDeserializer}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AndroidAssetStatementDeserializerTest {

    @Test(expected = RequireViolation.class)
    public void testNullJson() {
        new AndroidAssetStatementDeserializer().deserialize(null);
    }

    @Test
    public void testNoTarget() {
        JSONObject json = new JSONObject();
        final List<AndroidAssetStatement> assetStatements = new AndroidAssetStatementDeserializer()
                    .deserialize(json);
        assertNotNull(assetStatements);
        assertTrue(assetStatements.isEmpty());
    }
}
