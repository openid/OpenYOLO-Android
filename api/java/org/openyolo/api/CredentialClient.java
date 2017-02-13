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

package org.openyolo.api;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.bbq.BroadcastQueryClient;
import com.google.bbq.QueryCallback;
import com.google.bbq.QueryResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.openyolo.api.internal.IntentUtil;
import org.openyolo.api.ui.ProviderPickerActivity;
import org.openyolo.proto.CredentialRetrieveResponse;

/**
 * Interact with credential providers on the device which support OpenYOLO.
 */
public class CredentialClient {

    /**
     * The data type used for credential queries using the BBQ protocol.
     */
    public static final String CREDENTIAL_DATA_TYPE = "org.openyolo.credential";

    /**
     * The extra value key used to store the credential in a retrieve intent response.
     */
    public static final String EXTRA_CREDENTIAL = "org.openyolo.retrieve.credential";

    /**
     * The extra key value used to carry a hint request to a credential provider.
     */
    public static final String EXTRA_HINT_REQUEST = "org.openyolo.hint.request";

    /**
     * The category used for all OpenYOLO-related intents.
     */
    public static final String OPENYOLO_CATEGORY = "org.openyolo";

    /**
     * The action used for hint intents.
     */
    public static final String HINT_CREDENTIAL_ACTION = "org.openyolo.hint";

    /**
     * The action used for save intents.
     */
    public static final String SAVE_CREDENTIAL_ACTION = "org.openyolo.save";

    private static final String LOG_TAG = "CredentialClient";

    private static final AtomicReference<CredentialClient> INSTANCE_REF
            = new AtomicReference<>();

    private final Context mApplicationContext;

    private final BroadcastQueryClient mQueryClient;

    /**
     * Retrieves the singleton credential client instance associated with the application.
     */
    public static CredentialClient getApplicationBoundInstance(Context context) {
        Context applicationContext = context.getApplicationContext();
        CredentialClient client = new CredentialClient(applicationContext);
        if (!INSTANCE_REF.compareAndSet(null, client)) {
            client = INSTANCE_REF.get();
        }

        return client;
    }

    CredentialClient(Context applicationContext) {
        mApplicationContext = applicationContext;
        mQueryClient = BroadcastQueryClient.getApplicationBoundInstance(applicationContext);
    }

    /**
     * Provides an intent to request a login hint. This will target the user's prefered credential
     * provider, if this can be determined. If no providers are available, {@code null} is
     * returned.
     */
    @Nullable
    public Intent getHintRetrieveIntent(final HintRequest request) {
        List<ComponentName> hintProviders = findProviders(HINT_CREDENTIAL_ACTION, null);

        if (hintProviders.isEmpty()) {
            return null;
        }

        byte[] encodedRequest = request.toProtocolBuffer().encode();

        // TODO: if a preferred credential provider is set, directly invoke it.
        ArrayList<Intent> hintIntents = new ArrayList<>();
        for (ComponentName providerActivity : hintProviders) {
            Intent hintIntent = createIntent(providerActivity, HINT_CREDENTIAL_ACTION);
            hintIntent.putExtra(EXTRA_HINT_REQUEST, encodedRequest);
            hintIntents.add(hintIntent);
        }

        return ProviderPickerActivity.createHintIntent(mApplicationContext, hintIntents);
    }

    /**
     * Requests and retrieves any available credentials from the credential providers on the device.
     * @param request  Properties of the credential request
     * @param callback Handler for the result of the credential request.
     */
    public void retrieve(
            final RetrieveRequest request,
            final RetrieveCallback callback) {
        mQueryClient.queryFor(
                CREDENTIAL_DATA_TYPE,
                request.toProtocolBuffer(),
                new CredentialRetrieveQueryCallback(callback));
    }

    /**
     * Provides an intent to save the provided credential. If no compatible credential providers
     * exist on the device, {@code null} will be returned. The intent should be started with a
     * call to
     * {@link android.app.Activity#startActivityForResult(Intent, int) startActivityForResult}.
     *
     * <p>If the credential is successfully saved, {@link android.app.Activity#RESULT_OK} will
     * be returned. Otherwise, {@link android.app.Activity#RESULT_CANCELED} will be returned.
     */
    @Nullable
    public Intent getSaveIntent(Credential credentialToSave) {
        List<ComponentName> saveProviders =
                findProviders(SAVE_CREDENTIAL_ACTION, credentialToSave.getAuthenticationMethod());

        if (saveProviders.isEmpty()) {
            return null;
        }

        // TODO: if a preferred password manager is set, directly invoke it if it is in the list,
        // otherwise return no intent.

        byte[] encodedCredential = credentialToSave.getProto().encode();

        ArrayList<Intent> saveIntents = new ArrayList<>(saveProviders.size());
        for (ComponentName providerActivity : saveProviders) {
            Intent saveIntent = createIntent(providerActivity, SAVE_CREDENTIAL_ACTION);
            saveIntent.setData(credentialToSave.getAuthenticationMethod());
            saveIntent.putExtra(EXTRA_CREDENTIAL, encodedCredential);
            saveIntents.add(saveIntent);
        }

        return ProviderPickerActivity.createSaveIntent(mApplicationContext, saveIntents);
    }

    private Intent createIntent(ComponentName component, String action) {
        Intent intent = new Intent(action);
        intent.setClassName(
                component.getPackageName(),
                component.getClassName());
        intent.addCategory(OPENYOLO_CATEGORY);
        return intent;
    }

    /**
     * Extracts a credential from the data returned via
     * {@link android.app.Activity#onActivityResult(int, int, Intent) onActivityResult},
     * after a credential retrieve intent completes.
     */
    @Nullable
    public Credential getCredentialFromActivityResult(@NonNull Intent resultData) {
        if (resultData == null) {
            Log.e(LOG_TAG, "resultData is null, exiting (returning null)");
            return null;
        }

        if (!resultData.hasExtra(EXTRA_CREDENTIAL)) {
            Log.i(LOG_TAG, "credential data missing from response");
            return null;
        }

        byte[] credentialBytes = resultData.getByteArrayExtra(EXTRA_CREDENTIAL);
        if (credentialBytes == null) {
            Log.i(LOG_TAG, "No credential found in response");
            return null;
        }

        org.openyolo.proto.Credential credentialProto;
        try {
            credentialProto = org.openyolo.proto.Credential.ADAPTER.decode(credentialBytes);
        } catch (IOException ex) {
            Log.e(LOG_TAG, "failed to decode credential from response data");
            return null;
        }

        try {
            return new Credential.Builder(credentialProto).build();
        } catch (IllegalArgumentException ex) {
            Log.e(LOG_TAG, "validation of received credential failed", ex);
            return null;
        }
    }

    private List<ComponentName> findProviders(
            @NonNull String action,
            @Nullable Uri data) {
        Intent saveIntent = new Intent(action);
        saveIntent.addCategory(OPENYOLO_CATEGORY);
        if (data != null) {
            saveIntent.setData(data);
        }

        List<ResolveInfo> resolveInfos =
                mApplicationContext.getPackageManager().queryIntentActivities(saveIntent, 0);

        ArrayList<ComponentName> responders = new ArrayList<>();
        for (ResolveInfo info : resolveInfos) {
            responders.add(new ComponentName(
                    info.activityInfo.packageName,
                    info.activityInfo.name));
        }

        return responders;
    }

    private class CredentialRetrieveQueryCallback implements QueryCallback {

        private final RetrieveCallback mCallback;

        CredentialRetrieveQueryCallback(RetrieveCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onResponse(long queryId, List<QueryResponse> queryResponses) {
            ArrayList<Intent> retrieveIntents = new ArrayList<>();
            Map<String, CredentialRetrieveResponse> protoResponses = new HashMap<>();
            for (QueryResponse queryResponse : queryResponses) {
                CredentialRetrieveResponse response;
                try {
                    response = CredentialRetrieveResponse.ADAPTER.decode(
                            queryResponse.responseMessage);
                } catch (IOException e) {
                    Log.w(LOG_TAG, "Failed to decode credential retrieve response");
                    continue;
                }

                protoResponses.put(queryResponse.responderPackage, response);

                if (response.retrieveIntent != null) {
                    // TODO: handle failure to decode retrieve intent gracefully
                    // (i.e. ignore the response)
                    Intent retrieveIntent =
                            IntentUtil.fromBytes(response.retrieveIntent.toByteArray());

                    if (!queryResponse.responderPackage.equals(
                            retrieveIntent.getComponent().getPackageName())) {
                        Log.w(LOG_TAG, "Package mismatch between provider and retrieve intent");
                        continue;
                    }

                    retrieveIntents.add(retrieveIntent);
                }
            }

            Intent retrieveIntent = null;
            if (retrieveIntents.size() == 1) {
                retrieveIntent = retrieveIntents.get(0);
            } else if (retrieveIntents.size() > 1) {
                retrieveIntent = ProviderPickerActivity.createRetrieveIntent(
                        mApplicationContext,
                        retrieveIntents);
            }

            mCallback.onComplete(
                    new RetrieveResult.Builder()
                            .setProtoResponses(protoResponses)
                            .setRetrieveIntent(retrieveIntent)
                            .build(),
                    null);
        }
    }
}
