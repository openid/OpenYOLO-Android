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

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.AuthenticationMethods;
import org.openyolo.protocol.Credential;

/**
 * Creates credentials from manual user input for storage.
 */
public class CreateCredentialActivity
        extends AppCompatActivity
        implements CredentialStorageClient.ConnectedCallback {

    private static final String LOG_TAG = "CreateCredential";

    private static final int MIN_ID_LENGTH = 1;
    private static final int MIN_PASS_LENGTH = 1;

    @BindView(android.R.id.content)
    View mRootView;

    @BindView(R.id.headerToolbar)
    Toolbar mToolbar;

    @BindView(R.id.appBar)
    AppBarLayout mAppBar;

    @BindView(R.id.credential_authority)
    Spinner mCredentialAuthority;

    @BindView(R.id.credential_id)
    EditText mCredentialId;

    @BindView(R.id.credential_password)
    EditText mCredentialPassword;

    private MenuItem mDoneButton;

    private CredentialStorageClient mStorageClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_credential_layout);
        ButterKnife.bind(this);
        getSupportActionBar();
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mCredentialAuthority.setAdapter(new AppAdapter(getInstalledApps()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        CredentialStorageClient.connect(this, this);
    }

    @Override
    public void onStorageConnected(CredentialStorageClient client) {
        mStorageClient = client;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mStorageClient.disconnect(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create, menu);
        mDoneButton = menu.findItem(R.id.action_done);
        onFieldsChanged();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        }

        if (R.id.action_done == item.getItemId()) {
            App app = (App) mCredentialAuthority.getSelectedItem();
            String username = mCredentialId.getText().toString();
            String password = mCredentialPassword.getText().toString();
            new StoreCredentialTask(app.mPackageName, username, password).execute();
        }

        return false;
    }

    @OnItemSelected(R.id.credential_authority)
    @OnTextChanged({R.id.credential_id, R.id.credential_password})
    void onFieldsChanged() {
        boolean validCredential =
                mCredentialAuthority.getSelectedItemPosition() != Spinner.INVALID_POSITION
                && mCredentialId.length() >= MIN_ID_LENGTH
                && mCredentialPassword.length() >= MIN_PASS_LENGTH;

        if (mDoneButton != null) {
            mDoneButton.setEnabled(validCredential);
        }
    }

    private List<App> getInstalledApps() {
        List<String> appPackageNames = InstalledAppsUtil.getInstalledAppsList(this);
        ArrayList<App> apps = new ArrayList<>();
        PackageManager pm = getPackageManager();
        for (String packageName : appPackageNames) {
            try {
                ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
                String appName = pm.getApplicationLabel(info).toString();
                Drawable appIcon = pm.getApplicationIcon(info);
                apps.add(new App(packageName, appName, appIcon));
            } catch (PackageManager.NameNotFoundException e) {
                // skip this app
            }
        }

        return apps;
    }

    @SuppressLint("StaticFieldLeak")
    private class StoreCredentialTask extends AsyncTask<Void, Void, Boolean> {

        final String mPackageName;
        final String mUsername;
        final String mPassword;

        StoreCredentialTask(
                String packageName,
                String username,
                String password) {
            mPackageName = packageName;
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            AuthenticationDomain domain = AuthenticationDomain.fromPackageName(
                    CreateCredentialActivity.this,
                    mPackageName);
            if (null == domain) {
                Log.e(LOG_TAG, "Failed to find auth-domain associated with package name.");
                return null;
            }

            Credential credential = new Credential.Builder(
                    mUsername,
                    AuthenticationMethods.EMAIL,
                    domain)
                    .setPassword(mPassword)
                    .build();
            try {
                mStorageClient.upsertCredential(credential.toProtobuf());
                return true;
            } catch (IOException e) {
                Log.w(LOG_TAG, "Failed to upsert credential to database");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                finish();
            } else {
                Snackbar.make(
                        mRootView,
                        R.string.save_failed,
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private class App implements Comparable<App> {
        @NonNull String mName;
        @NonNull String mPackageName;
        @Nullable Drawable mIcon;

        App(@NonNull String packageName, @NonNull String name, @Nullable Drawable icon) {
            mPackageName = packageName;
            mName = name;
            mIcon = icon;
        }

        @Override
        public int compareTo(@NonNull App app) {
            return mName.compareTo(app.mName);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof App)) {
                return false;
            }

            App other = (App) obj;
            return mPackageName.equals(other.mPackageName);
        }

        @Override
        public int hashCode() {
            return mPackageName.hashCode();
        }
    }

    private class AppAdapter extends BaseAdapter {

        private final List<App> mApps;
        private final LayoutInflater mInflater;

        AppAdapter(@NonNull List<App> apps) {
            mApps = apps;
            mInflater = LayoutInflater.from(CreateCredentialActivity.this);
        }

        @Override
        public int getCount() {
            return mApps.size();
        }

        @Override
        public Object getItem(int index) {
            return mApps.get(index);
        }

        @Override
        public long getItemId(int index) {
            return mApps.get(index).hashCode();
        }

        @Override
        public View getView(int index, View view, ViewGroup viewGroup) {
            App app = mApps.get(index);
            if (view == null) {
                view = mInflater.inflate(R.layout.app_item, viewGroup, false);
            }

            ImageView iconView = (ImageView) view.findViewById(R.id.app_icon);
            if (app.mIcon != null) {
                iconView.setImageDrawable(app.mIcon);
                iconView.setVisibility(View.VISIBLE);
            } else {
                iconView.setVisibility(View.INVISIBLE);
            }

            TextView nameView = (TextView) view.findViewById(R.id.app_name);
            nameView.setText(app.mName);

            return view;
        }
    }
}
