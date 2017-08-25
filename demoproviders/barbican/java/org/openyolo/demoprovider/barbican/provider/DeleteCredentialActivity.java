/*
 * Copyright 2017 The OpenYOLO Authors. All Rights Reserved.
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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.io.IOException;
import java.util.Set;
import org.openyolo.demoprovider.barbican.R;
import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient;
import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient.ConnectedCallback;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.CredentialDeleteRequest;
import org.openyolo.protocol.CredentialDeleteResult;
import org.openyolo.protocol.MalformedDataException;
import org.openyolo.protocol.Protobufs;

public class DeleteCredentialActivity extends AppCompatActivity implements ConnectedCallback {

    private static final String LOG_TAG = "DeleteCrActivity";
    private CredentialStorageClient mStorageClient;
    private CredentialDeleteRequest mRequest;
    private Set<AuthenticationDomain> mCallerAuthDomains;


    @BindView(R.id.delete_prompt)
    TextView mDeletePromptView;

    @BindView(R.id.delete_yes_button)
    Button mAcceptDeleteButton;

    @BindView(R.id.delete_no_button)
    Button mRefuseDeleteButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mRequest = CredentialDeleteRequest.fromRequestIntent(getIntent());
        } catch (MalformedDataException ex) {
            Log.w(LOG_TAG, "Failed to decode hint request from intent", ex);
            setResultAndFinish(CredentialDeleteResult.BAD_REQUEST);
            return;
        }

        mCallerAuthDomains = CallerUtil.extractCallerAuthDomains(this);

        if (mCallerAuthDomains.isEmpty()) {
            Log.w(LOG_TAG, "Unable to identify calling app");
            setResultAndFinish(CredentialDeleteResult.BAD_REQUEST);
            return;
        }

        // TODO: expand the caller auth domains to all equivalent domains

        if (!mCallerAuthDomains.contains(mRequest.getCredential().getAuthenticationDomain())) {
            setResultAndFinish(CredentialDeleteResult.BAD_REQUEST);
            return;
        }

        CredentialStorageClient.connect(this, this);
    }

    @Override
    public void onStorageConnected(CredentialStorageClient client) {
        mStorageClient = client;

        final Credential credentialToDelete = mRequest.getCredential();

        if (!mStorageClient.isCreated()) {
            Log.i(LOG_TAG, "Store is not yet initialized");
            setResultAndFinish(CredentialDeleteResult.NO_MATCHING_CREDENTIAL);
            return;
        }

        try {
            Protobufs.Credential proto = credentialToDelete.toProtobuf();
            if (!mStorageClient.hasCredential(proto)) {
                setResultAndFinish(CredentialDeleteResult.NO_MATCHING_CREDENTIAL);
                return;
            }
        } catch (IOException ex) {
            setResultAndFinish(CredentialDeleteResult.UNKNOWN);
        }

        setContentView(R.layout.delete_layout);
        ButterKnife.bind(this);

        // monitor interactions outside of the dialog
        getWindow().setFlags(
                LayoutParams.FLAG_NOT_TOUCH_MODAL,
                LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(
                        LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

        String callingAppName = CallerUtil.getCallingAppName(this);
        String savePromptTemplate = getString(R.string.delete_prompt_template);
        mDeletePromptView.setText(String.format(
                savePromptTemplate,
                credentialToDelete.getIdentifier(),
                callingAppName).trim());
        mAcceptDeleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mStorageClient.deleteCredential(credentialToDelete.toProtobuf());
                    setResultAndFinish(CredentialDeleteResult.DELETED);
                } catch (IOException e) {
                    setResultAndFinish(CredentialDeleteResult.UNKNOWN);
                }
            }
        });

        mRefuseDeleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: record the fact that the user refused to delete this credential, so
                // that subsequent requests can be auto-refused.
                setResultAndFinish(CredentialDeleteResult.USER_REFUSED);
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResultAndFinish(CredentialDeleteResult.USER_CANCELED);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            setResultAndFinish(CredentialDeleteResult.USER_CANCELED);
            return true;
        }

        return super.onTouchEvent(event);
    }

    @UiThread
    private void setResultAndFinish(CredentialDeleteResult result) {
        setResult(result.getResultCode(), result.toResultDataIntent());
        finish();
    }
}
