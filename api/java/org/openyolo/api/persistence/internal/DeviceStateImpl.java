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

import org.openyolo.api.persistence.DeviceState;

/**
 * The default implementation of {@link DeviceState}.
 */
public final class DeviceStateImpl implements DeviceState {

    private static final String SETTING_NAME_SPACE = "DeviceState";
    private static final String KEY_IS_AUTO_SIGN_IN_DISABLED = "is_auto_sign_in_disabled";

    private final SettingsFactory.BooleanSetting mIsAutoSignInDisabled;

    /**
     * Returns a new instance of {@link DeviceStateImpl}.
     */
    public static DeviceState getInstance(Context context) {
        return new DeviceStateImpl(SettingsFactoryImpl.getInstance(context, SETTING_NAME_SPACE));
    }

    private DeviceStateImpl(SettingsFactory settingsFactory) {
        mIsAutoSignInDisabled =
                settingsFactory.makeBoolean(KEY_IS_AUTO_SIGN_IN_DISABLED, false /* defaultValue */);
    }

    @Override
    public boolean isAutoSignInDisabled() {
        return mIsAutoSignInDisabled.get();
    }

    @Override
    public void setIsAutoSignInDisabled(boolean isDisabled) {
        mIsAutoSignInDisabled.set(isDisabled);
    }
}
