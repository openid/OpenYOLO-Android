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
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.openyolo.demoprovider.barbican.R;
import org.openyolo.demoprovider.barbican.UnlockActivity;
import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.CredentialSaveResult;
import org.openyolo.protocol.MalformedDataException;

/**
 * Confirms with the user whether they want to save a credential provided by the
 * calling app. Verification of the app's right to save such a credential occurs
 * in {@link SaveCredentialActivity} before reaching this point.
 */
public class SaveCredentialConfirmationActivity extends AppCompatActivity {

    private static final String LOG_TAG = "SaveCredential";
    private static final String EXTRA_CALLING_PACKAGE = "callingPackage";
    private static final String EXTRA_CREDENTIAL = "credential";
    private static final long STORAGE_CONNECT_TIMEOUT_MS = 1000;

    @BindView(R.id.save_prompt)
    TextView mSavePromptView;

    private Credential mCredential;
    private String mCallingAppName;

    private AtomicReference<CredentialStorageClient> mClient = new AtomicReference<>();
    private final CountDownLatch mClientConnected = new CountDownLatch(1);

    /**
     * Creates an intent to confirm whether the user wishes to save the specified credential.
     */
    public static Intent createIntent(
            Context context,
            String callingPackage,
            Credential credential) {
        Intent intent = new Intent(context, SaveCredentialConfirmationActivity.class);
        intent.putExtra(EXTRA_CALLING_PACKAGE, callingPackage);
        intent.putExtra(EXTRA_CREDENTIAL, credential.toProtobuf().toByteArray());
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mCredential = Credential.fromProtoBytes(
                    getIntent().getByteArrayExtra(EXTRA_CREDENTIAL));
        } catch (MalformedDataException e) {
            Log.e(LOG_TAG, "Unable to decode credential", e);
            finishWithResult(CredentialSaveResult.UNKNOWN);
        }

        mCallingAppName = CallerUtil.getCallingAppName(this);

        CredentialStorageClient.connect(this, new CredentialStorageClient.ConnectedCallback() {
            @Override
            public void onStorageConnected(CredentialStorageClient client) {
                mClient.set(client);
                mClientConnected.countDown();
            }
        });

        setContentView(R.layout.save_layout);
        ButterKnife.bind(this);

        String savePromptTemplate = getString(R.string.save_prompt_template);
        mSavePromptView.setText(String.format(savePromptTemplate, mCallingAppName).trim());
    }

    private void finishWithResult(CredentialSaveResult result) {
        setResult(result.getResultCode(), result.toResultDataIntent());
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CredentialStorageClient client = mClient.get();
        if (client != null) {
            client.disconnect(this);
        }
    }

    @OnClick(R.id.save_button)
    void onSaveClicked() {
        CredentialStorageClient client = getStorageClient();
        if (client == null) {
            Log.w(LOG_TAG, "Unable to establish storage connection in a timely manner");
            finishWithResult(CredentialSaveResult.UNKNOWN);
            return;
        }

        if (client.isUnlocked()) {
            try {
                client.upsertCredential(mCredential.toProtobuf());
                finishWithResult(CredentialSaveResult.SAVED);
            } catch (IOException e) {
                Log.w(LOG_TAG, "Failed to write credential to storage");
                finishWithResult(CredentialSaveResult.UNKNOWN);
            }
        } else {
            Intent saveAfterUnlock = UnlockActivity.createIntent(
                    this,
                    true,
                    SaveAfterUnlockActivity.createIntent(this, mCredential.toProtobuf()));
            saveAfterUnlock.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(saveAfterUnlock);
            finish();
        }
    }

    @OnClick(R.id.never_save_button)
    void onNeverSaveClicked() {
        CredentialStorageClient client = getStorageClient();
        if (client == null) {
            finishWithResult(CredentialSaveResult.USER_REFUSED);
            return;
        }

        try {
            client.addToNeverSaveList(mCredential.getAuthenticationDomain());
        } catch (IOException e) {
            Log.w(LOG_TAG, "Failed to add app to never save list", e);
        }

        finishWithResult(CredentialSaveResult.USER_REFUSED);
    }

    private CredentialStorageClient getStorageClient() {
        try {
            mClientConnected.await(STORAGE_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            return mClient.get();
        } catch (InterruptedException e) {
            Log.w(LOG_TAG, "Failed to connect to the storage client in a timely manner", e);
            return null;
        }
    }
}
