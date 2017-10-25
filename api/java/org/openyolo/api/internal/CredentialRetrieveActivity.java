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

package org.openyolo.api.internal;

import static org.openyolo.protocol.ProtocolConstants.CREDENTIAL_DATA_TYPE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import com.google.bbq.BroadcastQueryClient;
import com.google.bbq.QueryCallback;
import com.google.bbq.QueryResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openyolo.protocol.CredentialRetrieveRequest;
import org.openyolo.protocol.CredentialRetrieveResult;
import org.openyolo.protocol.Protobufs;
import org.openyolo.protocol.ProtocolConstants;
import org.openyolo.protocol.internal.IntentUtil;

/**
 * An invisible Activity that forwards a given {@link CredentialRetrieveRequest} based on the
 * the set of available credential providers capable of handling it. A
 * {@link CredentialRetrieveResult} will be returned as described by
 * {@link org.openyolo.api.CredentialClient#getCredentialRetrieveIntent(CredentialRetrieveRequest)}.
 */
public final class CredentialRetrieveActivity extends Activity {

    private static final String LOG_TAG = "CredentialRetrieveAct";
    private static final String EXTRA_REQUEST = "Request";

    // The amount of time we will permit providers to "think" about responding to a retrieve
    // BBQ request. The default BBQ timeout of two seconds proved to be too short when the device
    // is actively updating and compiling apps, so we extend it here.
    private static final long RETRIEVE_TIMEOUT_MS = 4000;

    private boolean mIsDestroyed = false;

    /**
     * Returns an Intent for {@link CredentialRetrieveActivity} for the given
     * {@link CredentialRetrieveRequest}.
     */
    public static Intent createIntent(Context context, CredentialRetrieveRequest request) {
        return new Intent()
                .setClass(context, CredentialRetrieveActivity.class)
                .putExtra(EXTRA_REQUEST, request);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(LayoutParams.FLAG_NOT_TOUCHABLE);

        CredentialRetrieveRequest request = getIntent().getParcelableExtra(EXTRA_REQUEST);
        if (null == request) {
            setResult(
                    CredentialRetrieveResult.CODE_UNKNOWN,
                    CredentialRetrieveResult.UNKNOWN.toResultDataIntent());
            finish();
            return;
        }

        BroadcastQueryClient.getInstance(this)
                .queryFor(
                        CREDENTIAL_DATA_TYPE,
                        request.toProtocolBuffer(),
                        RETRIEVE_TIMEOUT_MS,
                        new CredentialRetrieveQueryCallback(request));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsDestroyed = true;
    }


    private class CredentialRetrieveQueryCallback implements QueryCallback {

        private CredentialRetrieveRequest mRetrieveRequest;

        CredentialRetrieveQueryCallback(@NonNull CredentialRetrieveRequest retrieveRequest) {
            mRetrieveRequest = retrieveRequest;
        }

        @Override
        public void onResponse(long queryId, List<QueryResponse> queryResponses) {
            ArrayList<Intent> retrieveIntents = new ArrayList<>();
            for (QueryResponse queryResponse : queryResponses) {
                Protobufs.CredentialRetrieveBbqResponse response;
                try {
                    response = Protobufs.CredentialRetrieveBbqResponse.parseFrom(
                            queryResponse.responseMessage);
                } catch (IOException e) {
                    Log.w(LOG_TAG, "Failed to decode credential retrieve response");
                    continue;
                }

                // a provider indicates that it has credentials via the credentials_available field
                // on the response proto. Prior 0.3.0, the provider would directly return an intent.
                // For backwards compatibility this is currently inspected as a fallback for the
                // absence of the credentials_available field.

                if (response.getCredentialsAvailable()) {
                    Intent retrieveIntent = new Intent(
                            ProtocolConstants.RETRIEVE_CREDENTIAL_ACTION);
                    retrieveIntent.setPackage(queryResponse.responderPackage);
                    retrieveIntent.putExtra(
                            ProtocolConstants.EXTRA_RETRIEVE_REQUEST,
                            mRetrieveRequest.toProtocolBuffer().toByteArray());

                    if (!response.getPreloadedData().isEmpty()) {
                        retrieveIntent.putExtra(ProtocolConstants.EXTRA_RETRIEVE_PRELOADED_DATA,
                                response.getPreloadedData().toByteArray());
                    }

                    retrieveIntents.add(retrieveIntent);
                } else if (!response.getRetrieveIntent().isEmpty()) {
                    // TODO: remove backwards compatibility with retrieve_intent after 0.3.0
                    Intent retrieveIntent =
                            IntentUtil.fromBytes(response.getRetrieveIntent().toByteArray());

                    if (!queryResponse.responderPackage.equals(
                            retrieveIntent.getComponent().getPackageName())) {
                        Log.w(LOG_TAG, "Package mismatch between provider and retrieve intent");
                    } else {
                        retrieveIntents.add(retrieveIntent);
                    }
                }
            }

            if (retrieveIntents.isEmpty()) {
                setResult(
                        CredentialRetrieveResult.CODE_PROVIDER_TIMEOUT,
                        CredentialRetrieveResult.PROVIDER_TIMEOUT.toResultDataIntent());
                finish();
                return;
            }

            if (retrieveIntents.size() == 1) {
                forwardResultFromActivity(retrieveIntents.get(0));
                return;
            }

            Intent intent = ProviderPickerActivity.createRetrieveIntent(
                    CredentialRetrieveActivity.this,
                    retrieveIntents);

            forwardResultFromActivity(intent);
        }
    }

    private void forwardResultFromActivity(Intent intent) {
        if (mIsDestroyed) {
            return;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(intent);
        finish();
    }
}
