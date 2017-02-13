/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
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

package org.openyolo.api.ui;

import static junit.framework.TestCase.assertNotNull;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openyolo.api.R;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

/**
 * Battery of tests for the ProviderPickerActivity
 */
@RunWith(YoloTestRunner.class)
@Config(manifest = "AndroidManifest.xml",
        sdk = Build.VERSION_CODES.LOLLIPOP,
        packageName = "org.openyolo.api")
public class ProviderPickerActivityTest {

    @Mock
    Context mockContext;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void createRetrieveIntent() throws Exception {
        ArrayList<Intent> retrieveIntents = new ArrayList<>();
        Intent item = ProviderPickerActivity.createRetrieveIntent(mockContext, retrieveIntents);
        assertNotNull(item);

    }

    @Test
    public void createSaveIntent() throws Exception {
        ArrayList<Intent> retrieveIntents = new ArrayList<>();
        Intent item = ProviderPickerActivity.createSaveIntent(mockContext, retrieveIntents);
        assertNotNull(item);
    }

    @Test
    public void testActivity_main() throws Exception {
        Activity activity = Robolectric.buildActivity(ProviderPickerActivity.class).create().get();
        assertNotNull(activity);
        TextView titleView = (TextView) activity.findViewById(R.id.picker_title);
    }

    @Test
    public void testActivity_toViewport_save() throws Exception {
        final String SAVE_CREDENTIAL_ACTION = "org.openyolo.save";
        final String OPENYOLO_CATEGORY = "org.openyolo";
        final String EXTRA_PROVIDER_INTENTS = "providerIntents";

            Intent saveIntent = new Intent(SAVE_CREDENTIAL_ACTION);
            saveIntent.setClassName(
                    "com.dashlane",
                    "LoginActivity");
            ArrayList<Intent> intents = new ArrayList<>();
            saveIntent.addCategory(OPENYOLO_CATEGORY);
            saveIntent.putParcelableArrayListExtra(EXTRA_PROVIDER_INTENTS, intents);
            //saveIntent.setData(credentialToSave.getAuthenticationMethod());


        Activity activity = Robolectric.buildActivity(ProviderPickerActivity.class)
                .withIntent(saveIntent)
                .create()
                .start()
                .resume()
                .visible()
                .get();
        assertNotNull(activity);
        ListAdapter adapter = ((ListView) activity.findViewById(R.id.provider_list)).getAdapter();
        assertNotNull(adapter);
    }
}