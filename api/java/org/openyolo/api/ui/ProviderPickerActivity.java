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

package org.openyolo.api.ui;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.valid4j.Assertive.require;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openyolo.api.CredentialClient;
import org.openyolo.api.KnownProviders;
import org.openyolo.api.R;

/**
 * Activity which presents the list of providers that claim to have a credential available.
 * The user can select one of these options, which fires the associated intent for that provider,
 * or cancel the whole operation.
 */
public class ProviderPickerActivity extends AppCompatActivity {

    private static final String EXTRA_PROVIDER_INTENTS = "providerIntents";
    private static final String EXTRA_TITLE_RES = "titleRes";
    private static final String LOG_TAG = "ProviderPicker";

    private CredentialClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_layout);

        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);

        Resources res = getResources();
        @DimenRes int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = 0;
        if (resourceId > 0) {
            statusBarHeight = res.getDimensionPixelSize(resourceId);
        }

        getWindow().setLayout(
                rectangle.width(),
                rectangle.height() - statusBarHeight);

        TextView titleView = (TextView) findViewById(R.id.picker_title);
        @StringRes int titleRes = getIntent().getIntExtra(EXTRA_TITLE_RES, 0);
        if (titleRes != 0) {
            //robotium has an issue recovering resources in the shadow classes.
            titleView.setText(titleRes);
        }

        ListView providerView = (ListView) findViewById(R.id.provider_list);
        List<Intent> retrieveIntents =
                getIntent().getParcelableArrayListExtra(EXTRA_PROVIDER_INTENTS);
        if (retrieveIntents == null) {
            retrieveIntents = Collections.emptyList();
        }
        providerView.setAdapter(new ProviderAdapter(retrieveIntents));
    }

    /**
     * Creates an intent for the picker activity, showing the user the list of credential providers
     * that can be used for the operation identified by the specified title text.
     */
    private static Intent createIntent(
            @NonNull Context context,
            @NonNull ArrayList<Intent> providerIntents,
            @StringRes int titleRes) {
        require(context, notNullValue());
        Intent intent = new Intent(context, ProviderPickerActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_PROVIDER_INTENTS, providerIntents);
        intent.putExtra(EXTRA_TITLE_RES, titleRes);
        return intent;
    }

    /**
     * Creates an intent for displaying a list of providers that claim to have a credential for
     * the current app, allowing the user to choose which to use.
     */
    public static Intent createRetrieveIntent(
            @NonNull Context context,
            @NonNull ArrayList<Intent> retrieveIntents) {
        return createIntent(context, retrieveIntents, R.string.retrieve_picker_prompt);
    }

    /**
     * Creates an intent for displaying a list of providers that can save a credential for the
     * user.
     */
    public static Intent createSaveIntent(
            @NonNull Context context,
            @NonNull ArrayList<Intent> retrieveIntents) {
        return createIntent(context, retrieveIntents, R.string.save_picker_prompt);
    }

    /**
     * Creates an intent for displaying a list of providers that can provide a login hint for
     * the current app, allowing the user to choose which to use.
     */
    public static Intent createHintIntent(
            @NonNull Context context,
            @NonNull ArrayList<Intent> hintIntents) {
        return createIntent(context, hintIntents, R.string.hint_picker_prompt);
    }

    /**
     * Creates an intent for display a list of providers to which a credential deletion request
     * should be sent.
     */
    public static Intent createDeleteIntent(
            @NonNull Context context,
            @NonNull ArrayList<Intent> deleteIntents) {
        return createIntent(context, deleteIntents, R.string.delete_picker_prompt);
    }

    private final class ProviderAdapter extends ArrayAdapter<Intent> {

        private final List<Drawable> mProviderIcons;
        private final List<String> mProviderNames;
        private final List<Boolean> mProviderKnown;

        ProviderAdapter(List<Intent> providerIntents) {
            super(ProviderPickerActivity.this, 0, providerIntents);
            mProviderIcons = new ArrayList<>(providerIntents.size());
            mProviderNames = new ArrayList<>(providerIntents.size());
            mProviderKnown = new ArrayList<>(providerIntents.size());

            KnownProviders knownProviders = KnownProviders.getInstance(
                    ProviderPickerActivity.this.getApplicationContext());
            PackageManager pm = getPackageManager();
            for (Intent providerIntent : providerIntents) {
                try {
                    String packageName = providerIntent.getComponent().getPackageName();
                    ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
                    mProviderIcons.add(info.loadIcon(pm));
                    mProviderNames.add(pm.getApplicationLabel(info).toString());
                    mProviderKnown.add(knownProviders.isKnown(packageName));
                } catch (PackageManager.NameNotFoundException e) {
                    Log.wtf(LOG_TAG, "Failed to retrieve package info for intent");
                }
            }
        }

        @Override
        public View getView(int position, View itemView, ViewGroup parent) {
            final Intent providerIntent = getItem(position);
            if (itemView == null) {
                itemView = LayoutInflater.from(ProviderPickerActivity.this)
                        .inflate(R.layout.provider_item, parent, false);
            }

            ImageView providerIconView = (ImageView) itemView.findViewById(R.id.provider_icon);
            TextView providerNameView = (TextView) itemView.findViewById(R.id.provider_name);
            TextView providerUnsafe =
                    (TextView) itemView.findViewById(R.id.unsafe_provider_warning);

            providerIconView.setImageDrawable(mProviderIcons.get(position));
            providerNameView.setText(mProviderNames.get(position));

            if (mProviderKnown.get(position)) {
                providerUnsafe.setVisibility(View.GONE);
            } else {
                providerUnsafe.setVisibility(View.VISIBLE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    providerIntent.setFlags(
                            providerIntent.getFlags() | Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    startActivity(providerIntent);
                    finish();
                }
            });
            return itemView;
        }

        @SuppressWarnings("deprecation")
        private int getColor(@ColorRes int colorRes) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                return getResources().getColor(colorRes, null);
            } else {
                return getResources().getColor(colorRes);
            }
        }
    }
}
