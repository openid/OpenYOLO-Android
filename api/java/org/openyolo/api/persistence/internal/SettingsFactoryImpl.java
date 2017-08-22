/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
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
import android.content.SharedPreferences;

/**
 * The default {@link SettingsFactory} implementation backed by {@link SharedPreferences}.
 */
final class SettingsFactoryImpl implements SettingsFactory {
    private final SharedPreferences mSharedPreferences;

    /**
     * Returns a new instance of the {@link SettingsFactoryImpl} for the given settings namespace.
     */
    public static SettingsFactoryImpl getInstance(Context context, String namespace) {
        return new SettingsFactoryImpl(context, namespace);
    }

    private SettingsFactoryImpl(Context context, String namespace) {
        mSharedPreferences = context.getSharedPreferences(namespace, Context.MODE_PRIVATE);
    }

    /**
     * Returns a new {@link SettingsFactory.BooleanSetting} based on
     * the given key and default value.
     */
    public BooleanSetting makeBoolean(String key, boolean defaultValue) {
        return BooleanSettingImpl.make(mSharedPreferences, key, defaultValue);
    }

    /**
     * The default internal implementation of a {@link SettingsFactory.BooleanSetting}.
     */
    private static final class BooleanSettingImpl implements BooleanSetting {
        private final String mKey;
        private final boolean mDefaultValue;
        private final SharedPreferences mSharedPreferences;

        private static BooleanSetting make(
                SharedPreferences sharedPreferences,
                String key,
                boolean defaultValue) {
            return new BooleanSettingImpl(sharedPreferences, key, defaultValue);
        }

        private BooleanSettingImpl(
                SharedPreferences sharedPreferences,
                String key,
                boolean defaultValue) {
            mKey = key;
            mDefaultValue = defaultValue;
            mSharedPreferences = sharedPreferences;
        }

        @Override
        public boolean get() {
            return mSharedPreferences.getBoolean(mKey, mDefaultValue);
        }

        @Override
        public void set(boolean value) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(mKey, value);
            editor.apply();
        }
    }

}
