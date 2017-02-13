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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Tests for {@link IncludeStatement}
 */
public class IncludeStatementTest {

    @Test (expected = RequireViolation.class)
    public void testNullSite() {
        new IncludeStatement.Builder()
                    .build();
    }

    @Test
    public void testIncludeSite() {
        final String url = "http://example.digitalassetlinks.org/.well-known/assetlinks.json";
        final IncludeStatement statement = new IncludeStatement.Builder()
                    .url(url)
                    .build();
        assertNotNull(statement);
        assertEquals(url, statement.getInclude());
        assertNotNull(statement.getRelations());
        assertTrue(statement.getRelations().isEmpty());
    }

}
