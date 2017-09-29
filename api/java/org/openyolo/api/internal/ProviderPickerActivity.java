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

package org.openyolo.api.internal;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.valid4j.Assertive.require;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openyolo.api.R;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.CredentialDeleteResult;
import org.openyolo.protocol.CredentialRetrieveResult;
import org.openyolo.protocol.CredentialSaveResult;
import org.openyolo.protocol.HintRetrieveResult;

/**
 * Activity which presents the given list of providers and their associated intent operations.
 * The user can select one of these providers, which fires the associated intent, or cancel the
 * whole operation.
 */
public final class ProviderPickerActivity extends Activity {

    private static final String EXTRA_PROVIDER_INTENTS = "providerIntents";
    private static final String EXTRA_TITLE_RES_ID = "titleRes";
    private static final String EXTRA_USER_CANCELED_RESULT = "userCanceledResult";
    private static final String LOG_TAG = "ProviderPicker";

    private static final ActivityResult RETRIEVE_USER_CANCELED_RESULT =
            ActivityResult.of(
                    CredentialRetrieveResult.CODE_USER_CANCELED,
                    CredentialRetrieveResult.USER_CANCELED.toResultDataIntent());

    private static final ActivityResult SAVE_USER_CANCELED_RESULT =
            ActivityResult.of(
                    CredentialSaveResult.CODE_USER_CANCELED,
                    CredentialSaveResult.USER_CANCELED.toResultDataIntent());

    private static final ActivityResult HINT_USER_CANCELED_RESULT =
            ActivityResult.of(
                    HintRetrieveResult.CODE_USER_CANCELED,
                    HintRetrieveResult.USER_CANCELED.toResultDataIntent());

    private static final ActivityResult DELETE_USER_CANCELED_RESULT =
            ActivityResult.of(
                    CredentialDeleteResult.CODE_USER_CANCELED,
                    CredentialDeleteResult.USER_CANCELED.toResultDataIntent());

    private View mPickerContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.provider_picker_layout);
        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mPickerContainer = findViewById(R.id.picker_container);

        // Setup title
        TextView titleView = findViewById(R.id.picker_title);
        @StringRes int titleResId = getIntent().getIntExtra(EXTRA_TITLE_RES_ID, 0);
        String title = getResources().getString(titleResId);
        titleView.setText(title);

        // Setup provider list
        ListView providerView = findViewById(R.id.provider_list);
        List<Intent> retrieveIntents =
                getIntent().getParcelableArrayListExtra(EXTRA_PROVIDER_INTENTS);
        if (retrieveIntents == null) {
            retrieveIntents = Collections.emptyList();
        }
        providerView.setAdapter(new ProviderAdapter(retrieveIntents));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        // finishWithUserCanceled() if the user tapped outside the picker
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            Rect visibilityRect = new Rect();
            mPickerContainer.getGlobalVisibleRect(visibilityRect);
            boolean tappedOutsidePicker =
                    !visibilityRect.contains((int) event.getRawX(), (int) event.getRawY());
            if (tappedOutsidePicker) {
                finishWithUserCanceled();
            }
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        finishWithUserCanceled();
    }

    private void finishWithUserCanceled() {
        ActivityResult activityResult = getIntent().getParcelableExtra(EXTRA_USER_CANCELED_RESULT);

        if (null != activityResult) {
            setResult(activityResult.getResultCode(), activityResult.getData());
        }

        finish();
    }

    /**
     * Creates an intent for the picker activity, showing the user the list of credential providers
     * that can be used for the operation identified by the specified title text.
     */
    private static Intent createIntent(
            @NonNull Context context,
            @NonNull ArrayList<Intent> providerIntents,
            @StringRes int titleRes,
            @Nullable ActivityResult userCanceledResult) {
        require(context, notNullValue());
        Intent intent = new Intent(context, ProviderPickerActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_PROVIDER_INTENTS, providerIntents);
        intent.putExtra(EXTRA_TITLE_RES_ID, titleRes);
        intent.putExtra(EXTRA_USER_CANCELED_RESULT, userCanceledResult);
        return intent;
    }

    /**
     * Creates an intent for displaying a list of providers that claim to have a credential for
     * the current app, allowing the user to choose which to use.
     */
    public static Intent createRetrieveIntent(
            @NonNull Context context,
            @NonNull ArrayList<Intent> retrieveIntents) {

        return createIntent(
                context,
                retrieveIntents,
                R.string.retrieve_picker_prompt,
                RETRIEVE_USER_CANCELED_RESULT);
    }

    /**
     * Creates an intent for displaying a list of providers that can save a credential for the
     * user.
     */
    public static Intent createSaveIntent(
            @NonNull Context context,
            @NonNull ArrayList<Intent> retrieveIntents) {

        return createIntent(
                context,
                retrieveIntents,
                R.string.save_picker_prompt,
                SAVE_USER_CANCELED_RESULT);
    }

    /**
     * Creates an intent for displaying a list of providers that can provide a login hint for
     * the current app, allowing the user to choose which to use.
     */
    public static Intent createHintIntent(
            @NonNull Context context,
            @NonNull ArrayList<Intent> hintIntents) {

        return createIntent(
                context,
                hintIntents,
                R.string.hint_picker_prompt,
                HINT_USER_CANCELED_RESULT);
    }

    /**
     * Creates an intent for display a list of providers to which a credential deletion request
     * should be sent.
     */
    public static Intent createDeleteIntent(
            @NonNull Context context,
            @NonNull ArrayList<Intent> deleteIntents) {

        return createIntent(
                context,
                deleteIntents,
                R.string.delete_picker_prompt,
                DELETE_USER_CANCELED_RESULT);
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

                    // NOTE(dxslly): Google's provider implementation lives inside the Google Play
                    // Services APK which has a different application label and icon than user's
                    // expect. To avoid confusion, a special case is made to provide the correct
                    // branding. Ideally this should be solved at the protocol level (e.g. optional
                    // metadata specified in the manifest that can override these defaults for
                    // trusted providers).
                    AuthenticationDomain authDomain =
                            AuthenticationDomain.fromPackageName(getContext(), packageName);
                    if (KnownProviders.GOOGLE_PROVIDER.equals(authDomain)) {
                        mProviderIcons.add(getDrawable(R.drawable.google_g_standard_color));
                        mProviderNames.add(
                                getString(R.string.provider_picker_google_provider_name));
                    } else {
                        mProviderIcons.add(info.loadIcon(pm));
                        mProviderNames.add(pm.getApplicationLabel(info).toString());
                    }

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

            ImageView providerIconView = itemView.findViewById(R.id.provider_icon);
            TextView providerNameView = itemView.findViewById(R.id.provider_name);
            View providerUnsafe = itemView.findViewById(R.id.unsafe_provider_warning);

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
    }
}
