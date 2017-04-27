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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.openyolo.demoprovider.barbican.Protobufs.AccountHint;
import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.AuthenticationMethod;
import org.openyolo.protocol.AuthenticationMethods;
import org.openyolo.protocol.HintRequest;
import org.openyolo.protocol.Protobufs.Credential;
import org.openyolo.protocol.ProtocolConstants;

public class HintActivity
        extends AppCompatActivity
        implements CredentialStorageClient.ConnectedCallback {

    private static final String LOG_TAG = "HintActivity";

    private HintRequest mRequest;
    private AuthenticationDomain mCallerAuthDomain;
    private CredentialStorageClient mClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getIntent().hasExtra(ProtocolConstants.EXTRA_HINT_REQUEST)) {
            Log.w(LOG_TAG, "No hint request object found in the intent");
            finish(RESULT_CANCELED);
            return;
        }

        try {
            mRequest = HintRequest.fromProtoBytes(
                    getIntent().getByteArrayExtra(ProtocolConstants.EXTRA_HINT_REQUEST));
        } catch (IOException ex) {
            Log.w(LOG_TAG, "Failed to decode hint request from intent", ex);
            finish(RESULT_CANCELED);
            return;
        }

        if (!extractCallerAuthDomain()) {
            Log.w(LOG_TAG, "Unable to identify calling app");
            finish(RESULT_CANCELED);
            return;
        }

        CredentialStorageClient.connect(this, this);
    }

    @Override
    public void onStorageConnected(CredentialStorageClient client) {
        mClient = client;

        if (!mClient.isCreated()) {
            Log.i(LOG_TAG, "Store is not yet initialized, cannot serve hints");
            finish(RESULT_CANCELED);
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
            finish(RESULT_CANCELED);
            return;
        }

        // filter the credentials to just those which are pertinent to the request. For those
        // selected, turn them into credentials. Order the hints by the amount of information
        // they contain (so more complete items are displayed first), and then alphabetically.
        ArrayList<Credential> filteredHints = new ArrayList<>();
        for (AccountHint hint : hints) {
            AuthenticationMethod hintAuthMethod = new AuthenticationMethod(hint.getAuthMethod());
            Set<AuthenticationMethod> authMethods = mRequest.getAuthenticationMethods();
            if (!authMethods.contains(hintAuthMethod)) {
                continue;
            }

            Credential.Builder hintCredentialBuilder = Credential.newBuilder()
                    .setId(hint.getIdentifier())
                    .setAuthMethod(hintAuthMethod.toProtobuf())
                    .setAuthDomain(mCallerAuthDomain.toProtobuf())
                    .setDisplayName(hint.getName())
                    .setDisplayPictureUri(hint.getPictureUri());

            // include a generated password if appropriate
            if (hint.getAuthMethod().equals(AuthenticationMethods.EMAIL.toString())) {
                hintCredentialBuilder.setPassword(mRequest.getPasswordSpecification().generate());
            }

            filteredHints.add(hintCredentialBuilder.build());
        }

        // if there are no hints, immediately end the request
        if (filteredHints.isEmpty()) {
            finish(RESULT_CANCELED);
            return;
        }

        // otherwise, display the standard credential picker.
        Intent pickerIntent = CredentialPickerActivity.createIntent(
                this,
                filteredHints);
        pickerIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(pickerIntent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mClient.disconnect(this);
    }

    private boolean extractCallerAuthDomain() {
        ComponentName callingActivity = getCallingActivity();
        if (callingActivity == null) {
            Log.w(LOG_TAG, "No calling activity found for save call");
            return false;
        }

        // TODO: a more complete implementation would need to expand this to the full set of
        // affiliated apps and sites, though it is generally unusual for an app to save a
        // credential for any authentication domain other than those it can directly identify as.
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
    private void finish(int resultCode) {
        setResult(resultCode);
        finish();
    }
}
