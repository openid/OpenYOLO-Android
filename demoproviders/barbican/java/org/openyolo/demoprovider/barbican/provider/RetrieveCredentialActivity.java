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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import java.io.IOException;
import java.util.List;
import org.openyolo.demoprovider.barbican.UnlockActivity;
import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.Protobufs.Credential;

/**
 * A UI-less activity that determines how to retrieve a requested credential. If the credential
 * store is locked, an unlock activity is started before proceeding. If the store is unlocked
 * and a single credential exists for the request, an auto sign-in activity is displayed.
 * If more than one credential exists, a picker is displayed.
 */
public class RetrieveCredentialActivity
        extends AppCompatActivity
        implements CredentialStorageClient.ConnectedCallback {

    private static final String LOG_TAG = "RetrieveCredential";
    private CredentialStorageClient mClient;

    /**
     * Creates the intent to handle releasing a credential to the calling app.
     */
    public static Intent createIntent(Context context) {
        return new Intent(context, RetrieveCredentialActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getCallingPackage() == null) {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        CredentialStorageClient.connect(this, this);
    }

    @Override
    public void onStorageConnected(CredentialStorageClient client) {
        mClient = client;
        if (!client.isUnlocked()) {
            Intent retrieveAfterUnlockIntent =
                    RetrieveCredentialActivity.createIntent(this);
            retrieveAfterUnlockIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

            Intent unlockIntent =
                    UnlockActivity.createIntent(this, true, retrieveAfterUnlockIntent);
            unlockIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(unlockIntent);
            finish();
        } else {
            // TODO: we actually need the full set of requested domains from the original request,
            // not just those based on the package name.

            List<AuthenticationDomain> authDomains =
                    AuthenticationDomain.listForPackage(this, getCallingPackage());

            List<Credential> credentials;
            try {
                credentials = client.listCredentials(authDomains);
            } catch (IOException ex) {
                Log.w(LOG_TAG, "Failed to list credentials for retrieve", ex);
                finish();
                return;
            }

            if (credentials.size() == 1) {
                Intent autoSignInIntent =
                        AutoSignInActivity.createIntent(this, credentials.get(0));
                autoSignInIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(autoSignInIntent);
            } else {
                Intent pickerIntent =
                        CredentialPickerActivity.createIntent(this, credentials);
                pickerIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(pickerIntent);
            }
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mClient.disconnect(this);
    }
}
