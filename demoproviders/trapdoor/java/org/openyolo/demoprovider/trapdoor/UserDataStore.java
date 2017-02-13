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

package org.openyolo.demoprovider.trapdoor;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class to access and manipulate user data that is pertinent to password generation.
 */
public class UserDataStore {

    private static final String SHARED_PREF_NAME = "userData";

    private static final String KEY_USER_NAME = "userName";

    private static final String DEFAULT_USER_NAME = "jdoe";

    /**
     * Changes the stored user name to the provided value.
     */
    public static void setUserName(Context context, String userName) {
        SharedPreferences prefs =
                context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USER_NAME, userName).apply();
    }

    /**
     * Retrieves the stored user name for credentials.
     */
    public static String getUserName(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_NAME, DEFAULT_USER_NAME);
    }
}
