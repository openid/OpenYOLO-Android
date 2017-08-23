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
import java.util.Arrays;

import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.CredentialSaveRequest;
import org.openyolo.protocol.CredentialSaveResult;
import org.openyolo.protocol.MalformedDataException;
import org.openyolo.protocol.ProtocolConstants;

/**
 * Verifies the rights to save a credential, before proceeding to
 * {@link SaveCredentialConfirmationActivity}.
 */
public class SaveCredentialActivity extends AppCompatActivity {

    private static final String LOG_TAG = "SaveCredential";

    private CredentialSaveRequest mRequest;
    private String mCallingPackage;
    private AuthenticationDomain mAuthDomainForApp;
    private CredentialStorageClient mStorageClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final byte[] encodedRequest =
                getIntent().getByteArrayExtra(ProtocolConstants.EXTRA_SAVE_REQUEST);

        // Ensure the intent contains a save request
        if (null == encodedRequest) {
            Log.w(LOG_TAG, "No save request included in intent");
            finishWithResult(CredentialSaveResult.BAD_REQUEST);
            return;
        }

        // Ensure the request can successfully be parsed from it's encoded form
        try {
            mRequest = CredentialSaveRequest.fromProtoBytes(encodedRequest);
        } catch (MalformedDataException ex) {
            Log.w(LOG_TAG, "Failed to decode save request", ex);
            finishWithResult(CredentialSaveResult.BAD_REQUEST);
            return;
        }

        if (!verifyCaller()) {
            Log.w(LOG_TAG, "Rejecting attempt to forge save request for "
                    + mRequest.getCredential().getAuthenticationDomain());
            finishWithResult(CredentialSaveResult.BAD_REQUEST);
            return;
        }

        CredentialStorageClient.connect(this, new CredentialStorageClient.ConnectedCallback() {
            @Override
            public void onStorageConnected(final CredentialStorageClient client) {
                mStorageClient = client;
                runOnUiThread(new AfterStorageConnect());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mStorageClient != null) {
            mStorageClient.disconnect(this);
        }
    }

    @UiThread
    private void finishWithResult(CredentialSaveResult result) {
        setResult(result.getResultCode(), result.toResultDataIntent());
        finish();
    }

    private boolean verifyCaller() {
        final ComponentName callingActivity = getCallingActivity();
        if (callingActivity == null) {
            Log.w(LOG_TAG, "No calling activity found for save call");
            return false;
        }

        // TODO: a more complete implementation would need to expand this to the full set of
        // affiliated apps and sites, though it is generally unusual for an app to save a
        // credential for any authentication domain other than those it can directly identify as.
        mCallingPackage = callingActivity.getPackageName();
        mAuthDomainForApp = AuthenticationDomain.fromPackageName(this, mCallingPackage);

        final AuthenticationDomain authDomain = mRequest.getCredential().getAuthenticationDomain();
        if (!authDomain.equals(mAuthDomainForApp)) {
            Log.w(LOG_TAG, "App " + mCallingPackage
                    + " is not provably associated to authentication domain "
                    + authDomain
                    + " for the credential to be saved.");
            return false;
        }

        return true;
    }

    private final class AfterStorageConnect implements Runnable {
        @Override
        public void run() {
            try {
                // Ensure authentication domain is not on never save list
                if (mStorageClient.isOnNeverSaveList(Arrays.asList(mAuthDomainForApp))) {
                    finishWithResult(CredentialSaveResult.PROVIDER_REFUSED);
                    return;
                }

                // Start confirm activity
                Intent confirmIntent = SaveCredentialConfirmationActivity.createIntent(
                        SaveCredentialActivity.this,
                        mCallingPackage,
                        mRequest.getCredential());
                confirmIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(confirmIntent);
                finish();
            } catch (IOException ex) {
                finishWithResult(CredentialSaveResult.UNSPECIFIED);
            }
        }
    }
}
