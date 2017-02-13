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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import java.io.IOException;
import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient;

public class WelcomeActivity
        extends AppCompatActivity
        implements CredentialStorageClient.ConnectedCallback {

    private static final String LOG_TAG = "WelcomeActivity";
    private static final int MIN_PASSWORD_SIZE = 6;

    private CredentialStorageClient mStorageClient;

    @BindView(android.R.id.content)
    View mRootView;

    @BindView(R.id.password)
    EditText mPasswordField;

    @BindView(R.id.loading)
    View mLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_layout);
        ButterKnife.bind(this);
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

    @OnEditorAction(R.id.password)
    boolean onPasswordConfirmedViaIme() {
        if (!isPasswordValid(mPasswordField.getText().toString())) {
            return false;
        }

        initializeStorage();
        return true;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= MIN_PASSWORD_SIZE;
    }

    private void initializeStorage() {
        new InitializeStorageTask(mPasswordField.getText().toString()).execute();
    }

    private class InitializeStorageTask extends AsyncTask<Void, Void, Boolean> {

        private String mPassword;

        InitializeStorageTask(String password) {
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            mPasswordField.setVisibility(View.GONE);
            mLoadingView.setVisibility(View.VISIBLE);
        }

        protected Boolean doInBackground(Void... params) {
            try {
                mStorageClient.create(mPassword);
                return true;
            } catch (IOException ex) {
                Log.e(LOG_TAG, "Failed to create credential storage", ex);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                startActivity(new Intent(WelcomeActivity.this, CredentialListActivity.class));
                finish();
            } else {
                mPasswordField.setVisibility(View.VISIBLE);
                mLoadingView.setVisibility(View.GONE);
                Snackbar.make(mRootView,
                        R.string.create_failure,
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
