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
import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient;
import org.openyolo.protocol.Protobufs.Credential;


/**
 * Writes a credential to the credential store after it is unlocked.
 */
public class SaveAfterUnlockActivity extends AppCompatActivity {

    private static final String EXTRA_CREDENTIAL = "credential";
    private static final String LOG_TAG = "SaveAfterUnlock";

    private Credential mCredential;

    /**
     * Creates an intent to save the provided credential after the store is unlocked.
     */
    public static Intent createIntent(Context context, Credential credentialProto) {
        Intent intent = new Intent(context, SaveAfterUnlockActivity.class);
        intent.putExtra(EXTRA_CREDENTIAL, credentialProto.toByteArray());
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        byte[] credentialBytes = getIntent().getByteArrayExtra(EXTRA_CREDENTIAL);
        try {
            mCredential = Credential.parseFrom(credentialBytes);
        } catch (IOException e) {
            Log.w(LOG_TAG, "Failed to decode credential to save");
        }

        CredentialStorageClient.connect(this, new CredentialStorageClient.ConnectedCallback() {
            @Override
            public void onStorageConnected(CredentialStorageClient client) {
                final boolean success;
                try {
                    client.upsertCredential(mCredential);
                    runOnUiThread(new SendResultRunnable(RESULT_OK));
                } catch (IOException e) {
                    Log.w(LOG_TAG, "Failed to store credential after save", e);
                    runOnUiThread(new SendResultRunnable(RESULT_CANCELED));
                }

                client.disconnect(SaveAfterUnlockActivity.this);
            }
        });
    }

    private class SendResultRunnable implements Runnable {
        private int mResultCode;

        SendResultRunnable(int resultCode) {
            mResultCode = resultCode;
        }

        @Override
        public void run() {
            setResult(mResultCode);
            finish();
        }
    }
}
