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

package org.openyolo.demoprovider.barbican.provider;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.openyolo.protocol.AuthenticationDomain;

/**
 * Extracts information on the caller of an activity.
 */
public final class CallerUtil {

    /**
     * Extracts the set of authentication domains for the caller of the provided activity.
     */
    @NonNull
    public static Set<AuthenticationDomain> extractCallerAuthDomains(@NonNull Activity activity) {
        ComponentName callingActivity = activity.getCallingActivity();
        if (callingActivity == null) {
            return Collections.emptySet();
        }

        String callingPackage = callingActivity.getPackageName();
        return new HashSet<>(AuthenticationDomain.listForPackage(activity, callingPackage));
    }

    /**
     * Determines the human-readable name of the caller of the provided activity.
     */
    public static String getCallingAppName(@NonNull Activity activity) {
        try {
            PackageManager pm = activity.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(activity.getCallingPackage(), 0);
            return pm.getApplicationLabel(info).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return "???";
        }
    }
}
