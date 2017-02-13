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

package org.openyolo.demoprovider.barbican;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Interrogates the system for installed, non-system applications.
 */
public class InstalledAppsUtil {

    /**
     * Retrieves a set of the package names of all non-system applications.
     */
    public static Set<String> getInstalledApps(Context context) {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> installedAppInfos =
                pm.getInstalledApplications(PackageManager.GET_META_DATA);

        TreeSet<String> apps = new TreeSet<>();
        for (ApplicationInfo info : installedAppInfos) {
            // ignore self
            if (context.getPackageName().equals(info.packageName)) {
                continue;
            }

            // skip system apps
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                continue;
            }

            apps.add(info.packageName);
        }

        return apps;
    }

    /**
     * Retrieves a list of the package names of all non-system applications.
     */
    public static List<String> getInstalledAppsList(Context context) {
        return new ArrayList<>(getInstalledApps(context));
    }
}
