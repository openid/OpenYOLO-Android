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

package org.openyolo.spi;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Utility methods for retrieving an app's asset statements.
 */
public final class AssetStatementsUtil {
    private static final String ASSET_STATEMENTS = "asset_statements";
    private static final String TAG = "AssetStatementsUtil";

    private AssetStatementsUtil() {
        throw new AssertionError(); // so this class isn't instantiated
    }

    /**
     * Get the <code>asset_statements</code> from the meta-data of specified app package.
     *
     * @param context     Context
     * @param packageName Package name of the app from which the asset_statements should be
     *                    retrieved
     * @return The asset_statements string or null if not found
     */
    @Nullable
    public static String getAssetStatements(final Context context,
                                            final String packageName) {
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Unable to find application info for package "
                    + packageName);
            return null;
        }

        if (applicationInfo.metaData == null
                || !applicationInfo.metaData.containsKey(ASSET_STATEMENTS)) {
            return null;
        }

        Resources resources;
        try {
            resources = context.getPackageManager().getResourcesForApplication(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }

        return resources.getString(applicationInfo.metaData.getInt(ASSET_STATEMENTS));
    }
}
