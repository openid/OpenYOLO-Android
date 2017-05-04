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

import android.content.ComponentName;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.openyolo.demoprovider.barbican.Protobufs.AccountHint;
import org.openyolo.demoprovider.barbican.R;
import org.openyolo.demoprovider.barbican.provider.AccountViewHolder.ClickHandler;
import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.AuthenticationMethod;
import org.openyolo.protocol.AuthenticationMethods;
import org.openyolo.protocol.Hint;
import org.openyolo.protocol.HintRetrieveRequest;
import org.openyolo.protocol.HintRetrieveResult;
import org.openyolo.protocol.ProtocolConstants;

public class HintPickerActivity
        extends AppCompatActivity
        implements CredentialStorageClient.ConnectedCallback {

    private static final String LOG_TAG = "HintPickerActivity";

    private HintRetrieveRequest mRequest;
    private AuthenticationDomain mCallerAuthDomain;
    private CredentialStorageClient mClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getIntent().hasExtra(ProtocolConstants.EXTRA_HINT_REQUEST)) {
            Log.w(LOG_TAG, "No hint request object found in the intent");
            setResultAndFinish(HintRetrieveResult.BAD_REQUEST);
            return;
        }

        try {
            mRequest = HintRetrieveRequest.fromProtoBytes(
                    getIntent().getByteArrayExtra(ProtocolConstants.EXTRA_HINT_REQUEST));
        } catch (IOException ex) {
            Log.w(LOG_TAG, "Failed to decode hint request from intent", ex);
            setResultAndFinish(HintRetrieveResult.BAD_REQUEST);
            return;
        }

        if (!extractCallerAuthDomain()) {
            Log.w(LOG_TAG, "Unable to identify calling app");
            setResultAndFinish(HintRetrieveResult.BAD_REQUEST);
            return;
        }

        CredentialStorageClient.connect(this, this);
    }

    @Override
    public void onStorageConnected(CredentialStorageClient client) {
        mClient = client;

        if (!mClient.isCreated()) {
            Log.i(LOG_TAG, "Store is not yet initialized, cannot serve hints");
            setResultAndFinish(HintRetrieveResult.UNKNOWN);
            return;
        }

        // some credential provider implementations may require an unlock to retrieve the
        // information required for hints, but for Barbican we instead choose to store basic
        // profile information usable for hints in plain text. With root access, this information
        // would be retrievable by malicious applications on the device, representing a privacy
        // risk. However, full credentials and associations between account identifiers
        // remain encrypted.

        List<AccountHint> hints;
        try {
            hints = mClient.getHints();
        } catch (IOException ex) {
            Log.e(LOG_TAG, "Failed to retrieve hints from the credential store", ex);
            setResultAndFinish(HintRetrieveResult.UNKNOWN);
            return;
        }

        // filter the credentials to just those which are pertinent to the request. For those
        // selected, turn them into credentials. Order the hints by the amount of information
        // they contain (so more complete items are displayed first), and then alphabetically.
        ArrayList<Hint> filteredHints = new ArrayList<>();
        for (AccountHint hint : hints) {
            AuthenticationMethod hintAuthMethod = new AuthenticationMethod(hint.getAuthMethod());
            Set<AuthenticationMethod> authMethods = mRequest.getAuthenticationMethods();
            if (!authMethods.contains(hintAuthMethod)) {
                continue;
            }

            Hint.Builder hintProtoBuilder = new Hint.Builder(
                    hint.getIdentifier(),
                    hintAuthMethod)
                    .setDisplayName(hint.getName())
                    .setDisplayPictureUri(hint.getPictureUri());

            // include a generated password if appropriate
            if (hintAuthMethod.equals(AuthenticationMethods.EMAIL)
                    || hintAuthMethod.equals(AuthenticationMethods.PHONE)
                    || hintAuthMethod.equals(AuthenticationMethods.USER_NAME)) {
                hintProtoBuilder.setGeneratedPassword(
                        mRequest.getPasswordSpecification().generate());
            }

            filteredHints.add(hintProtoBuilder.build());
        }

        // if there are no hints, immediately end the request
        if (filteredHints.isEmpty()) {
            setResultAndFinish(HintRetrieveResult.NO_HINTS_AVAILABLE);
            return;
        }

        // otherwise, display the standard credential picker.
        renderPicker(filteredHints);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mClient.disconnect(this);
    }

    private void renderPicker(List<Hint> hints) {
        setContentView(R.layout.hint_picker_layout);

        RecyclerView hintsView = (RecyclerView) findViewById(R.id.available_hints);

        hintsView.setAdapter(new HintPickerAdapter(hints));
        hintsView.setLayoutManager(new LinearLayoutManager(this));
    }

    private boolean extractCallerAuthDomain() {
        ComponentName callingActivity = getCallingActivity();
        if (callingActivity == null) {
            Log.w(LOG_TAG, "No calling activity found for save call");
            return false;
        }

        String callingPackage = callingActivity.getPackageName();
        List<AuthenticationDomain> authDomains =
                AuthenticationDomain.listForPackage(this, callingPackage);

        if (authDomains.isEmpty()) {
            return false;
        }

        mCallerAuthDomain = authDomains.get(0);
        return true;
    }

    @UiThread
    private void setResultAndFinish(HintRetrieveResult result) {
        setResult(result.getResultCode(), result.toResultDataIntent());
        finish();
    }

    private final class HintPickerAdapter extends RecyclerView.Adapter<AccountViewHolder> implements
            ClickHandler<Hint> {

        List<Hint> mHints;

        HintPickerAdapter(List<Hint> hints) {
            mHints = hints;
        }

        @Override
        public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View credentialView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.credential_picker_item_view, parent, false);

            return new AccountViewHolder(credentialView);
        }

        @Override
        public void onBindViewHolder(AccountViewHolder holder, int position) {
            holder.bind(mHints.get(position), this);
        }

        @Override
        public int getItemCount() {
            return mHints.size();
        }

        @Override
        public void onClick(Hint clicked) {
            HintRetrieveResult result = new HintRetrieveResult.Builder(
                    HintRetrieveResult.CODE_HINT_SELECTED)
                    .setHint(clicked)
                    .build();

            setResultAndFinish(result);
        }
    }
}
