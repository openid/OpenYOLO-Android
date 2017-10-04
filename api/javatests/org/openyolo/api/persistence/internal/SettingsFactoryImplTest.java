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

package org.openyolo.api.persistence.internal;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.api.persistence.internal.SettingsFactory.BooleanSetting;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Java6Assertions.assertThat;

/** Unit tests for {@link org.openyolo.api.persistence.internal.SettingsFactoryImpl} */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public final class SettingsFactoryImplTest {

    private static final String NAMESPACE_A = "com.namespace.a";
    private static final String NAMESPACE_B = "com.namespace.b";
    private static final String BOOLEAN_KEY = "whatIsLife?";
    private static final Context CONTEXT = RuntimeEnvironment.application;

    private SettingsFactory mSettingsFactoryA;
    private SettingsFactory mSettingsFactoryB;

    @Before
    public void setup() {
        mSettingsFactoryA = SettingsFactoryImpl.getInstance(CONTEXT, NAMESPACE_A);
        mSettingsFactoryB = SettingsFactoryImpl.getInstance(CONTEXT, NAMESPACE_B);
    }

    @Test
    public void makeBooleanSetting_defaultValueFalse_returnsFalse() {
        boolean value = mSettingsFactoryA.makeBoolean(BOOLEAN_KEY, false /* defaultValue */).get();
        assertThat(value).isFalse();
    }

    @Test
    public void makeBooleanSetting_defaultValueTrue_returnsTrue() {
        boolean value = mSettingsFactoryA.makeBoolean(BOOLEAN_KEY, true /* defaultValue */).get();
        assertThat(value).isTrue();
    }

    @Test
    public void makeBooleansSettings_sameNamespacesSameKey_haveSameValues() {
        final String key = "key";
        final Boolean defaultValue = false;

        BooleanSetting settingA = mSettingsFactoryA.makeBoolean(key, defaultValue);
        settingA.set(true);
        BooleanSetting settingB = mSettingsFactoryA.makeBoolean(key, defaultValue);

        assertThat(settingA.get()).isTrue();
        assertThat(settingB.get()).isTrue();
    }

    @Test
    public void makeBooleansSettings_differentNamespacesButSameKey_haveDifferentValues() {
        final String key = "key";
        final Boolean defaultValue = false;

        BooleanSetting settingA = mSettingsFactoryA.makeBoolean(key, defaultValue);
        settingA.set(true);
        BooleanSetting settingB = mSettingsFactoryB.makeBoolean(key, defaultValue);

        assertThat(settingA.get()).isTrue();
        assertThat(settingB.get()).isFalse();
    }
}
