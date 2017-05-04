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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.List;
import org.openyolo.demoprovider.barbican.ProtoListUtil;
import org.openyolo.demoprovider.barbican.R;
import org.openyolo.demoprovider.barbican.provider.AccountViewHolder.ClickHandler;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.CredentialRetrieveResult;
import org.openyolo.protocol.Protobufs;
import org.openyolo.protocol.internal.CollectionConverter;
import org.openyolo.protocol.internal.CredentialConverter;

/**
 * Displays a dialog to pick a credential from a list.
 */
public class CredentialPickerActivity extends AppCompatActivity {

    private static final String LOG_TAG = "CredentialPicker";

    private static final String EXTRA_CREDENTIALS = "credentials";

    @BindView(R.id.available_credentials)
    RecyclerView mCredentialView;

    /**
     * Creates an intent to display the credential picker with the provided set of credentials.
     */
    public static Intent createIntent(
            Context context,
            List<Credential> credentials) {
        Intent intent = new Intent(context, CredentialPickerActivity.class);
        ByteString data = ProtoListUtil.writeMessageList(
                CollectionConverter.toList(
                        credentials,
                        CredentialConverter.CREDENTIAL_TO_PROTO));

        intent.putExtra(EXTRA_CREDENTIALS, data.toByteArray());
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, "Picker invoked by " + getCallingPackage());

        setContentView(R.layout.credential_picker_layout);
        ButterKnife.bind(this);

        mCredentialView.setAdapter(new CredentialPickerAdapter(getCredentials()));
        mCredentialView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            setResultAndFinish(
                    new CredentialRetrieveResult.Builder(
                            CredentialRetrieveResult.RESULT_REJECTED_BY_USER)
                            .build());
            return true;
        }

        return super.onTouchEvent(event);
    }

    private void setResultAndFinish(CredentialRetrieveResult result) {
        setResult(result.getResultCode(), result.toResultDataIntent());
        finish();
    }

    private List<Credential> getCredentials() {
        byte[] credentialBytes = getIntent().getByteArrayExtra(EXTRA_CREDENTIALS);
        try {
            List<Protobufs.Credential> credentialProtos = ProtoListUtil
                    .readMessageList(credentialBytes, Protobufs.Credential.parser());

            return CollectionConverter.toList(
                    credentialProtos,
                    CredentialConverter.PROTO_TO_CREDENTIAL);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to decode credentials from intent", ex);
        }
    }

    private final class CredentialPickerAdapter
            extends RecyclerView.Adapter<AccountViewHolder>
            implements ClickHandler<Credential> {

        List<Credential> mCredentials;

        CredentialPickerAdapter(List<Credential> credentials) {
            mCredentials = credentials;
        }

        @Override
        public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View credentialView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.credential_picker_item_view, parent, false);

            return new AccountViewHolder(credentialView);
        }

        @Override
        public void onBindViewHolder(AccountViewHolder holder, int position) {
            holder.bind(mCredentials.get(position), this);
        }

        @Override
        public int getItemCount() {
            return mCredentials.size();
        }

        @Override
        public void onClick(Credential clicked) {
            CredentialRetrieveResult result = new CredentialRetrieveResult.Builder(
                    CredentialRetrieveResult.RESULT_SUCCESS)
                    .setCredential(clicked)
                    .build();
            setResultAndFinish(result);
        }
    }
}
