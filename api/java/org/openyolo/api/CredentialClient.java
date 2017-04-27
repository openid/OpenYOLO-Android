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

import static org.openyolo.protocol.ProtocolConstants.CREDENTIAL_DATA_TYPE;
import static org.openyolo.protocol.ProtocolConstants.EXTRA_CREDENTIAL;
import static org.openyolo.protocol.ProtocolConstants.EXTRA_HINT_REQUEST;
import static org.openyolo.protocol.ProtocolConstants.HINT_CREDENTIAL_ACTION;
import static org.openyolo.protocol.ProtocolConstants.OPENYOLO_CATEGORY;
import static org.openyolo.protocol.ProtocolConstants.SAVE_CREDENTIAL_ACTION;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.bbq.BroadcastQueryClient;
import com.google.bbq.QueryCallback;
import com.google.bbq.QueryResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.openyolo.api.ui.ProviderPickerActivity;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.HintRequest;
import org.openyolo.protocol.Protobufs;
import org.openyolo.protocol.Protobufs.CredentialRetrieveBbqResponse;
import org.openyolo.protocol.RetrieveRequest;
import org.openyolo.protocol.RetrieveResult;
import org.openyolo.protocol.internal.IntentUtil;

/**
 * Interact with credential providers on the device which support OpenYOLO.
 */
public class CredentialClient {

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
        List<ComponentName> hintProviders = findProviders(HINT_CREDENTIAL_ACTION);

        if (hintProviders.isEmpty()) {
            return null;
        }

        byte[] encodedRequest = request.toProtocolBuffer().toByteArray();

        // if there is a preferred provider, directly invoke it.
        ComponentName preferredProviderActivity = getPreferredProvider(hintProviders);
        if (preferredProviderActivity != null) {
            return createHintIntent(preferredProviderActivity, encodedRequest);
        }

        // otherwise, display a picker for all the providers.
        ArrayList<Intent> hintIntents = new ArrayList<>();
        for (ComponentName providerActivity : hintProviders) {
            hintIntents.add(createHintIntent(providerActivity, encodedRequest));
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
                findProviders(SAVE_CREDENTIAL_ACTION);

        if (saveProviders.isEmpty()) {
            return null;
        }

        byte[] encodedCredential = credentialToSave.toProtobuf().toByteArray();

        // if there is a preferred provider, directly invoke it.
        ComponentName preferredSaveActivity = getPreferredProvider(saveProviders);
        if (preferredSaveActivity != null) {
            return createSaveIntent(
                    preferredSaveActivity,
                    encodedCredential);
        }

        // otherwise, display a picker for all the providers.
        ArrayList<Intent> saveIntents = new ArrayList<>(saveProviders.size());
        for (ComponentName providerActivity : saveProviders) {
            saveIntents.add(createSaveIntent(
                    providerActivity,
                    encodedCredential));
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

    private Intent createHintIntent(ComponentName providerActivity, byte[] hintRequest) {
        Intent hintIntent = createIntent(providerActivity, HINT_CREDENTIAL_ACTION);
        hintIntent.putExtra(EXTRA_HINT_REQUEST, hintRequest);
        return hintIntent;
    }

    private Intent createSaveIntent(
            ComponentName providerActivity,
            byte[] saveRequest) {
        Intent saveIntent = createIntent(providerActivity, SAVE_CREDENTIAL_ACTION);
        saveIntent.putExtra(EXTRA_CREDENTIAL, saveRequest);
        return saveIntent;
    }

    /**
     * Extracts a credential from the data returned via
     * {@link android.app.Activity#onActivityResult(int, int, Intent) onActivityResult},
     * after a credential retrieve intent completes.
     */
    @Nullable
    public Credential getCredentialFromActivityResult(@Nullable Intent resultData) {
        if (resultData == null) {
            Log.i(LOG_TAG, "resultData is null, exiting (returning null)");
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

        Protobufs.Credential credentialProto;
        try {
            credentialProto = Protobufs.Credential.parseFrom(credentialBytes);
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

    private List<ComponentName> findProviders(@NonNull String action) {
        Intent saveIntent = new Intent(action);
        saveIntent.addCategory(OPENYOLO_CATEGORY);

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

    @Nullable
    private ComponentName getPreferredProvider(@NonNull List<ComponentName> providers) {
        // In the future, the user will be able to explicitly set their preferred provider in
        // their device settings. For now, we heuristically determine the preferred provider based
        // on the following rules:

        // 1. If there are any unknown providers on the device, there is no preferred provider.
        List<ComponentName> knownProviders = filterToKnownProviders(providers);
        if (knownProviders.size() != providers.size()) {
            return null;
        }

        // 2. If there are no known providers, then there is no preferred provider. Unknown
        // providers are not trusted for automatic usage.
        if (knownProviders.isEmpty()) {
            return null;
        }

        // 3. If there is exactly one provider on the device and it is known,
        // it is the preferred provider.
        if (knownProviders.size() == 1) {
            return knownProviders.get(0);
        }

        // 3. If there are exactly two known providers on the device, and one of them is Google,
        // choose the other one.
        // This reflects the reality that Google is pre-installed on most devices, whereas a second
        // provider would likely have been explicitly installed by the user - it is likely that
        // their intent is to use this other provider.
        providers = filterOutGoogle(knownProviders);
        if (providers.size() == 1) {
            return knownProviders.get(0);
        }

        // 4. Otherwise, there is no preferred provider.
        return null;
    }

    private List<ComponentName> filterToKnownProviders(List<ComponentName> providers) {
        KnownProviders knownProviders =
                KnownProviders.getApplicationBoundInstance(mApplicationContext);

        ArrayList<ComponentName> filteredProviders = new ArrayList<>();
        for (ComponentName provider : providers) {
            if (knownProviders.isKnown(provider.getPackageName())) {
                filteredProviders.add(provider);
            }
        }

        return filteredProviders;
    }

    private List<ComponentName> filterOutGoogle(List<ComponentName> providers) {
        Iterator<ComponentName> providerIter = providers.iterator();
        while (providerIter.hasNext()) {
            ComponentName provider = providerIter.next();
            if (provider.getPackageName().equals(
                    KnownProviders.GOOGLE_PROVIDER.getAndroidPackageName())) {
                providerIter.remove();
            }
        }

        return providers;
    }

    private class CredentialRetrieveQueryCallback implements QueryCallback {

        private final RetrieveCallback mCallback;

        CredentialRetrieveQueryCallback(RetrieveCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onResponse(long queryId, List<QueryResponse> queryResponses) {
            ArrayList<Intent> retrieveIntents = new ArrayList<>();
            Map<String, CredentialRetrieveBbqResponse> protoResponses = new HashMap<>();
            for (QueryResponse queryResponse : queryResponses) {
                Protobufs.CredentialRetrieveBbqResponse response;
                try {
                    response = Protobufs.CredentialRetrieveBbqResponse.parseFrom(
                            queryResponse.responseMessage);
                } catch (IOException e) {
                    Log.w(LOG_TAG, "Failed to decode credential retrieve response");
                    continue;
                }

                protoResponses.put(queryResponse.responderPackage, response);

                if (!response.getRetrieveIntent().isEmpty()) {
                    // TODO: handle failure to decode retrieve intent gracefully
                    // (i.e. ignore the response)
                    Intent retrieveIntent =
                            IntentUtil.fromBytes(response.getRetrieveIntent().toByteArray());

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
