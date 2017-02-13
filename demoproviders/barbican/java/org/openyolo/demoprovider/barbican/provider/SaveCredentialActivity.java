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

package org.openyolo.demoprovider.barbican.provider;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import java.io.IOException;
import java.util.List;
import org.openyolo.api.AuthenticationDomain;
import org.openyolo.api.Credential;
import org.openyolo.api.CredentialClient;
import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient;

/**
 * Verifies the rights to save a credential, before proceeding to
 * {@link SaveCredentialConfirmationActivity}.
 */
public class SaveCredentialActivity extends AppCompatActivity {

    private static final String LOG_TAG = "SaveCredential";

    private Credential mCredential;
    private String mCallingPackage;
    private List<AuthenticationDomain> mAuthDomainsForApp;
    private CredentialStorageClient mClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getIntent().hasExtra(CredentialClient.EXTRA_CREDENTIAL)) {
            Log.w(LOG_TAG, "No credential included in request");
            finish(RESULT_CANCELED);
            return;
        }

        try {
            mCredential = Credential.fromProtoBytes(
                    getIntent().getByteArrayExtra(CredentialClient.EXTRA_CREDENTIAL));
        } catch (IOException ex) {
            Log.w(LOG_TAG, "Failed to decode credential for save request");
            finish(RESULT_CANCELED);
            return;
        }

        if (!verifyCaller()) {
            Log.w(LOG_TAG, "Rejecting attempt to forge save request for "
                    + mCredential.getAuthenticationDomain());
            finish(RESULT_CANCELED);
            return;
        }

        CredentialStorageClient.connect(this, new CredentialStorageClient.ConnectedCallback() {
            @Override
            public void onStorageConnected(final CredentialStorageClient client) {
                mClient = client;
                runOnUiThread(new AfterStorageConnect());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mClient != null) {
            mClient.disconnect(this);
        }
    }

    @UiThread
    private void finish(int resultCode) {
        setResult(resultCode);
        finish();
    }

    private boolean verifyCaller() {
        ComponentName callingActivity = getCallingActivity();
        if (callingActivity == null) {
            Log.w(LOG_TAG, "No calling activity found for save call");
            return false;
        }

        // TODO: a more complete implementation would need to expand this to the full set of
        // affiliated apps and sites, though it is generally unusual for an app to save a
        // credential for any authentication domain other than those it can directly identify as.
        mCallingPackage = callingActivity.getPackageName();
        mAuthDomainsForApp = AuthenticationDomain.listForPackage(this, mCallingPackage);

        if (!mAuthDomainsForApp.contains(mCredential.getAuthenticationDomain())) {
            Log.w(LOG_TAG, "App " + mCallingPackage
                    + " is not provably associated to authentication domain "
                    + mCredential.getAuthenticationDomain()
                    + " for the credential to be saved.");
            return false;
        }


        return true;
    }

    private final class AfterStorageConnect implements Runnable {
        @Override
        public void run() {
            try {
                if (mClient.isOnNeverSaveList(mAuthDomainsForApp)) {
                    finish(RESULT_CANCELED);
                } else {
                    // start confirm activity
                    Intent confirmIntent = SaveCredentialConfirmationActivity.createIntent(
                            SaveCredentialActivity.this,
                            mCallingPackage,
                            mCredential);
                    confirmIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    startActivity(confirmIntent);
                    finish();
                }
            } catch (IOException ex) {
                finish(RESULT_CANCELED);
            }
        }
    }
}
