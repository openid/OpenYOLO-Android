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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java.io.IOException;
import java.util.List;
import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient;
import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient.ConnectedCallback;
import org.openyolo.protocol.Protobufs.Credential;

/**
 * Displays the user's stored credentials.
 */
public class CredentialListActivity
        extends AppCompatActivity
        implements ConnectedCallback {

    private static final String LOG_TAG = "CLActivity";

    private CredentialStorageClient mStorageClient;

    @BindView(R.id.headerToolbar)
    Toolbar mToolbar;

    @BindView(R.id.credential_list)
    RecyclerView mCredentialsView;

    private CredentialAdapter mCredentialAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.credential_list_layout);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mCredentialAdapter = new CredentialAdapter();
        mCredentialsView.setAdapter(mCredentialAdapter);
        mCredentialsView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected void onStart() {
        super.onStart();
        CredentialStorageClient.connect(this, this);
    }

    @Override
    public void onStorageConnected(CredentialStorageClient client) {
        mStorageClient = client;
        mCredentialAdapter.setCredentialStorageClient(client);
        if (checkUnlocked()) {
            new LoadCredentialsTask().execute();
        }
    }

    private boolean checkUnlocked() {
        if (!mStorageClient.isUnlocked()) {
            startActivity(new Intent(this, UnlockActivity.class));
            finish();
            return false;
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mStorageClient.disconnect(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        }

        if (R.id.action_lock == item.getItemId()) {
            mStorageClient.lock();
            startActivity(new Intent(this, UnlockActivity.class));
            finish();
            return true;
        }

        if (R.id.action_wipe == item.getItemId()) {
            startActivity(new Intent(this, NeverSaveListActivity.class));
            return true;
        }
        return false;
    }

    @OnClick(R.id.create_credential)
    void onCreateCredential() {
        startActivity(new Intent(this, CreateCredentialActivity.class));
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadCredentialsTask extends AsyncTask<Void, Void, List<Credential>> {

        @Override
        protected List<Credential> doInBackground(Void... params) {
            try {
                return mStorageClient.listAllCredentials();
            } catch (IOException ex) {
                Log.e(LOG_TAG, "Failed to list credentials", ex);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Credential> credentials) {
            if (credentials != null) {
                ((CredentialAdapter) mCredentialsView.getAdapter()).setCredentials(credentials);
            }
        }
    }
}
