/*
 * Copyright 2017 The OpenYOLO Authors. All Rights Reserved.
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

package org.openyolo.api.internal;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.ProtocolConstants;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowPackageManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ProviderResolverTest {

    private String EXAMPLE_PROVIDER_PACKAGE = "com.example.provider";
    private String EXAMPLE_PROVIDER_ACTIVITY_CLASS = "com.example.provider.SaveActivity";

    private ShadowPackageManager shadowPackageManager;
    private Intent saveIntent;

    @Before
    public void setUp() {
        saveIntent = new Intent(ProtocolConstants.SAVE_CREDENTIAL_ACTION);
        saveIntent.addCategory(ProtocolConstants.OPENYOLO_CATEGORY);

        shadowPackageManager =
                Shadows.shadowOf(RuntimeEnvironment.application.getPackageManager());
    }

    @Test
    public void findProviders_noneInstalled() {
        List<ComponentName> providers = ProviderResolver.findProviders(
                RuntimeEnvironment.application,
                ProtocolConstants.SAVE_CREDENTIAL_ACTION);

        assertThat(providers).isEmpty();
    }

    @Test
    public void findProviders_singleInstalled() {
        shadowPackageManager.addResolveInfoForIntent(
                saveIntent,
                createResolveInfo(EXAMPLE_PROVIDER_PACKAGE, EXAMPLE_PROVIDER_ACTIVITY_CLASS));

        List<ComponentName> providers = ProviderResolver.findProviders(
                RuntimeEnvironment.application,
                ProtocolConstants.SAVE_CREDENTIAL_ACTION);

        ComponentName expectedComponent = new ComponentName(
                EXAMPLE_PROVIDER_PACKAGE,
                EXAMPLE_PROVIDER_ACTIVITY_CLASS);
        assertThat(providers).containsExactly(expectedComponent);
    }

    @Test
    public void findProvider_matchingPackage() {
        shadowPackageManager.addResolveInfoForIntent(
                saveIntent,
                createResolveInfo(EXAMPLE_PROVIDER_PACKAGE, EXAMPLE_PROVIDER_ACTIVITY_CLASS));

        ComponentName provider = ProviderResolver.findProvider(
                RuntimeEnvironment.application,
                EXAMPLE_PROVIDER_PACKAGE,
                ProtocolConstants.SAVE_CREDENTIAL_ACTION);

        assertThat(provider).isNotNull();
        assertThat(provider.getPackageName()).isEqualTo(EXAMPLE_PROVIDER_PACKAGE);
        assertThat(provider.getClassName()).isEqualTo(EXAMPLE_PROVIDER_ACTIVITY_CLASS);
    }

    @Test
    public void findProvider_nonMatchingPackage() {
        shadowPackageManager.addResolveInfoForIntent(
                saveIntent,
                createResolveInfo(EXAMPLE_PROVIDER_PACKAGE, EXAMPLE_PROVIDER_ACTIVITY_CLASS));

        ComponentName provider = ProviderResolver.findProvider(
                RuntimeEnvironment.application,
                "com.different.provider",
                ProtocolConstants.SAVE_CREDENTIAL_ACTION);

        assertThat(provider).isNull();
    }

    @Test
    public void createIntentForAction_matchingPackage() {
        shadowPackageManager.addResolveInfoForIntent(
                saveIntent,
                createResolveInfo(EXAMPLE_PROVIDER_PACKAGE, EXAMPLE_PROVIDER_ACTIVITY_CLASS));

        Intent intentForAction = ProviderResolver.createIntentForAction(
                RuntimeEnvironment.application,
                EXAMPLE_PROVIDER_PACKAGE,
                ProtocolConstants.SAVE_CREDENTIAL_ACTION);

        assertThat(intentForAction).isNotNull();
        assertThat(intentForAction.getAction())
                .isEqualTo(ProtocolConstants.SAVE_CREDENTIAL_ACTION);
        assertThat(intentForAction.getCategories())
                .containsExactly(ProtocolConstants.OPENYOLO_CATEGORY);
        assertThat(intentForAction.getComponent()).isNotNull();
        assertThat(intentForAction.getComponent().getPackageName())
                .isEqualTo(EXAMPLE_PROVIDER_PACKAGE);
        assertThat(intentForAction.getComponent().getClassName())
                .isEqualTo(EXAMPLE_PROVIDER_ACTIVITY_CLASS);
    }

    @Test
    public void createIntentForAction_nonMatchingPackage() {
        shadowPackageManager.addResolveInfoForIntent(
                saveIntent,
                createResolveInfo(EXAMPLE_PROVIDER_PACKAGE, EXAMPLE_PROVIDER_ACTIVITY_CLASS));

        Intent intentForAction = ProviderResolver.createIntentForAction(
                RuntimeEnvironment.application,
                "com.different.provider",
                ProtocolConstants.SAVE_CREDENTIAL_ACTION);

        assertThat(intentForAction).isNull();
    }

    private ResolveInfo createResolveInfo(String packageName, String activityClass) {
        ResolveInfo resolveInfo = new ResolveInfo();
        resolveInfo.activityInfo = new ActivityInfo();
        resolveInfo.activityInfo.packageName = packageName;
        resolveInfo.activityInfo.name = activityClass;

        return resolveInfo;
    }
}
