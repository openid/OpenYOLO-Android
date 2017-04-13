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

package org.openyolo.spi.assetlinks.data;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for {@link NamespaceType}.
 */
public class NamespaceTypeTest {
    @Test
    public void androidAppTest() {
        final NamespaceType namespaceType = NamespaceType.getNamespaceType
                    ("android_app");
        assertNotNull(namespaceType);
        assertTrue(namespaceType == NamespaceType.AndroidApp);
    }

    @Test
    public void webTest() {
        final NamespaceType namespaceType = NamespaceType.getNamespaceType
                    ("web");
        assertNotNull(namespaceType);
        assertTrue(namespaceType == NamespaceType.Web);
    }

    @Test
    public void invalidRelationType() {
        final NamespaceType namespaceType = NamespaceType.getNamespaceType
                    ("foo");
        assertNull(namespaceType);
    }

    @Test
    public void nullInput() {
        final NamespaceType namespaceType = NamespaceType.getNamespaceType
                    (null);
        assertNull(namespaceType);
    }
}