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
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import java.io.IOException;
import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient;

public class UnlockActivity
        extends AppCompatActivity
        implements CredentialStorageClient.ConnectedCallback {

    private static final String EXTRA_AS_DIALOG = "asDialog";
    private static final String EXTRA_AFTER_UNLOCK = "afterUnlock";
    private static final String LOG_TAG = "UnlockActivity";

    private CredentialStorageClient mStorageClient;

    @BindView(android.R.id.content)
    View mRootView;

    @BindView(R.id.password_entry)
    View mPasswordEntryForm;

    @BindView(R.id.invalid_password)
    View mInvalidPasswordError;

    @BindView(R.id.password)
    EditText mPasswordField;

    @BindView(R.id.loading)
    View mLoadingView;

    /**
     * Creates an intent to display the unlock screen, triggering the provided intent after
     * unlock.
     */
    public static Intent createIntent(Context context, boolean asDialog, Intent afterUnlock) {
        Intent intent = new Intent(context, UnlockActivity.class);
        intent.putExtra(EXTRA_AS_DIALOG, asDialog);
        intent.putExtra(EXTRA_AFTER_UNLOCK, afterUnlock);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (showAsDialog()) {
            setTheme(R.style.AppTheme_Dialog);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }

        setContentView(R.layout.unlock_layout);
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

        if (client.isUnlocked()) {
            startActivity(getAfterUnlock());
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mStorageClient.disconnect(this);
    }

    @OnEditorAction(R.id.password)
    boolean onPasswordConfirmedViaIme() {
        new UnlockTask(mPasswordField.getText().toString()).execute();
        return true;
    }

    private boolean showAsDialog() {
        return getIntent().getBooleanExtra(EXTRA_AS_DIALOG, false);
    }

    private Intent getAfterUnlock() {
        if (getIntent().hasExtra(EXTRA_AFTER_UNLOCK)) {
            return getIntent().getParcelableExtra(EXTRA_AFTER_UNLOCK);
        } else {
            return new Intent(this, CredentialListActivity.class);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class UnlockTask extends AsyncTask<Void, Void, Boolean> {

        private final String mPassword;

        UnlockTask(String password) {
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            mPasswordEntryForm.setVisibility(View.GONE);
            mLoadingView.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return mStorageClient.unlock(mPassword);
            } catch (IOException ex) {
                Log.e(LOG_TAG, "Failed to unlock credential store", ex);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                startActivity(getAfterUnlock());
                finish();
            } else {
                mPasswordEntryForm.setVisibility(View.VISIBLE);
                mInvalidPasswordError.setVisibility(View.VISIBLE);
                mLoadingView.setVisibility(View.GONE);
            }
        }
    }
}
