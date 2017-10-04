/*
 * Copyright 2017 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openyolo.protocol;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Unit tests for {@link AuthenticationDomain}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public final class AdditionalPropertiesHelperTest {

    @Test
    public void encodeStringValue_withNull() {
        assertThat(AdditionalPropertiesHelper.encodeStringValue(null)).isNull();
    }

    @Test
    public void decodeStringValue_withNull() {
        assertThat(AdditionalPropertiesHelper.decodeStringValue(null)).isNull();
    }

    @Test
    public void encodeAndDecode() {
        byte[] encoded = AdditionalPropertiesHelper.encodeStringValue("testing");
        assertThat(AdditionalPropertiesHelper.decodeStringValue(encoded)).isEqualTo("testing");
    }
}
