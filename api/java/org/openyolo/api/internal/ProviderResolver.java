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

package org.openyolo.api.internal;

import static org.openyolo.protocol.ProtocolConstants.OPENYOLO_CATEGORY;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for producing OpenYOLO resolving the activities exposed by providers to handle specific,
 * OpenYOLO actions.
 */
public final class ProviderResolver {

    private ProviderResolver() {
        // not intended to be constructed
    }

    /**
     * Creates an explicit Intent to invoke the specified action on the specified provider,
     * if the provider has a handler registered.
     */
    @Nullable
    public static Intent createIntentForAction(
            @NonNull Context applicationContext,
            @NonNull String providerPackageName,
            @NonNull String action) {

        ComponentName providerComponent =
                findProvider(applicationContext, providerPackageName, action);

        if (providerComponent == null) {
            return null;
        }

        Intent intent = new Intent(action);
        intent.setClassName(
                providerComponent.getPackageName(),
                providerComponent.getClassName());
        intent.addCategory(OPENYOLO_CATEGORY);
        return intent;
    }

    /**
     * Resolves a {@link ComponentName} for the activity defined in the specified provider, for
     * the specified action.
     */
    @Nullable
    public static ComponentName findProvider(
            @NonNull Context applicationContext,
            @NonNull String providerPackageName,
            @NonNull String action) {
        List<ComponentName> providers = findProviders(applicationContext, action);

        for (ComponentName provider : providers) {
            if (providerPackageName.equals(provider.getPackageName())) {
                return provider;
            }
        }

        return null;
    }

    /**
     * Resolves the {@link ComponentName components} for all providers which can handle the
     * specified OpenYOLO action.
     */
    @NonNull
    public static List<ComponentName> findProviders(
            @NonNull Context applicationContext,
            @NonNull String action) {
        Intent providerIntent = new Intent(action);
        providerIntent.addCategory(OPENYOLO_CATEGORY);

        List<ResolveInfo> resolveInfos =
                applicationContext.getPackageManager()
                        .queryIntentActivities(providerIntent, 0);

        ArrayList<ComponentName> responders = new ArrayList<>();
        for (ResolveInfo info : resolveInfos) {
            responders.add(new ComponentName(
                    info.activityInfo.packageName,
                    info.activityInfo.name));
        }

        return responders;
    }
}
