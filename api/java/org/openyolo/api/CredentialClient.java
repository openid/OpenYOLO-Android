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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.openyolo.protocol.ProtocolConstants.DELETE_CREDENTIAL_ACTION;
import static org.openyolo.protocol.ProtocolConstants.EXTRA_DELETE_REQUEST;
import static org.openyolo.protocol.ProtocolConstants.EXTRA_HINT_REQUEST;
import static org.openyolo.protocol.ProtocolConstants.EXTRA_HINT_RESULT;
import static org.openyolo.protocol.ProtocolConstants.EXTRA_RETRIEVE_RESULT;
import static org.openyolo.protocol.ProtocolConstants.EXTRA_SAVE_REQUEST;
import static org.openyolo.protocol.ProtocolConstants.EXTRA_SAVE_RESULT;
import static org.openyolo.protocol.ProtocolConstants.HINT_CREDENTIAL_ACTION;
import static org.openyolo.protocol.ProtocolConstants.OPENYOLO_CATEGORY;
import static org.openyolo.protocol.ProtocolConstants.SAVE_CREDENTIAL_ACTION;
import static org.valid4j.Assertive.require;
import static org.valid4j.Validation.validate;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openyolo.api.internal.ActivityResult;
import org.openyolo.api.internal.CredentialRetrieveActivity;
import org.openyolo.api.internal.FinishWithResultActivity;
import org.openyolo.api.internal.KnownProviders;
import org.openyolo.api.internal.ProviderPickerActivity;
import org.openyolo.api.internal.ProviderResolver;
import org.openyolo.api.persistence.AppSettings;
import org.openyolo.api.persistence.internal.AppSettingsImpl;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.CredentialDeleteRequest;
import org.openyolo.protocol.CredentialDeleteResult;
import org.openyolo.protocol.CredentialRetrieveRequest;
import org.openyolo.protocol.CredentialRetrieveResult;
import org.openyolo.protocol.CredentialSaveRequest;
import org.openyolo.protocol.CredentialSaveResult;
import org.openyolo.protocol.HintRetrieveRequest;
import org.openyolo.protocol.HintRetrieveResult;
import org.openyolo.protocol.MalformedDataException;


/**
 * The primary way of interacting with OpenYOLO credential providers. The client is light weight
 * allowing new instances to be created as needed.
 *
 * Requests are executed by starting an Activity based intent and its result returned via
 * {@link android.app.Activity#onActivityResult(int, int, Intent)}. For each request/response pair
 * there exist two client methods:
 * 1) Takes a given request and returns the Activity Intent which sends the request.
 * 2) Takes an Intent returned via {@link android.app.Activity#onActivityResult(int, int, Intent)}
 *    and returns the associated result.
 *
 * For example using the retrieve credential flow:
 * <pre>{@code
 * CredentialClient client = CredentialClient.getInstance(getContext());
 * // ...
 *
 * CredentialRetrieveRequest request  =
 *     CredentialRetrieveRequest.fromAuthMethods(
 *         AuthenticationMethods.EMAIL,
 *         AuthenticationMethods.GOOGLE);
 * Intent retrieveCredentialIntent = client.getCredentialRetrieveIntent(request);
 * startActivityForResult(retrieveCredentialIntent, RC_RETRIEVE_CREDENTIAL);
 *
 * // ...
 * @Override
 * public void onActivityResult(int requestCode, int resultCode, Intent data) {
 *     super.onActivityResult(requestCode, resultCode, data);
 *     if (RC_RETRIEVE_CREDENTIAL == requestCode) {
 *        CredentialRetrieveResult result = client.getCredentialRetrieveResult(data);
 *        // handle result ...
 *     }
 * }
 * }</pre>
 *
 *
 * @see <a href="http://spec.openyolo.org/openyolo-android-spec.html#operations">
 *     OpenYOLO Specification: Operations</a>
 */
public class CredentialClient {

    private static final String LOG_TAG = "CredentialClient";

    private final Context mApplicationContext;
    private final AppSettings mDeviceState;

    /**
     * Returns a new credential client instance configured with the default options.
     */
    public static CredentialClient getInstance(@NonNull Context context) {
        CredentialClientOptions options =
                new CredentialClientOptions.Builder(AppSettingsImpl.getInstance(context)).build();

        return getInstance(context, options);
    }

    /**
     * Returns a new credential client instance configured with the given
     * {@link CredentialClientOptions}.
     */
    public static CredentialClient getInstance(
            @NonNull Context context,
            @NonNull CredentialClientOptions options) {
        return new CredentialClient(context, options);
    }

    @VisibleForTesting
    CredentialClient(
            @NonNull Context context,
            @NonNull CredentialClientOptions options) {
        validate(context, notNullValue(), NullPointerException.class);
        validate(options, notNullValue(), NullPointerException.class);

        mApplicationContext = context.getApplicationContext();
        mDeviceState = options.getDeviceState();
    }

    /**
     * Provides an Activity intent to request any available {@link Credential credentials} from the
     * credential providers on the device.
     *
     * Launch the returned intent via
     * {@link android.app.Activity#startActivityForResult(Intent, int)} and extract the result via
     * {@link #getCredentialRetrieveResult(Intent)} using the Intent data received in the associated
     * {@link android.app.Activity#onActivityResult(int, int, Intent)} callback.
     *
     * <pre>{@code
     * CredentialClient client = CredentialClient.getInstance(getContext());
     * // ...
     *
     * CredentialRetrieveRequest request  =
     *     CredentialRetrieveRequest.fromAuthMethods(
     *         AuthenticationMethods.EMAIL,
     *         AuthenticationMethods.GOOGLE);
     * Intent retrieveCredentialIntent = client.getCredentialRetrieveIntent(request);
     * startActivityForResult(retrieveCredentialIntent, RC_RETRIEVE_CREDENTIAL);
     *
     * // ...
     * @Override
     * public void onActivityResult(int requestCode, int resultCode, Intent data) {
     *     super.onActivityResult(requestCode, resultCode, data);
     *     if (RC_RETRIEVE_CREDENTIAL == requestCode) {
     *        CredentialRetrieveResult result = client.getCredentialRetrieveResult(data);
     *        // handle result ...
     *     }
     * }
     * }</pre>
     *
     * @see #getCredentialRetrieveResult(Intent)
     */
    @NonNull
    public Intent getCredentialRetrieveIntent(CredentialRetrieveRequest request) {
        if (mDeviceState.isAutoSignInDisabled()) {
            request = new CredentialRetrieveRequest.Builder(request)
                    .setRequireUserMediation(true)
                    .build();
        }

        return CredentialRetrieveActivity.createIntent(mApplicationContext, request);
    }

    /**
     * Provides an Activity intent to request any available {@link org.openyolo.protocol.Hint hints}
     * from the credential providers on the device.
     *
     * Launch the returned intent via
     * {@link android.app.Activity#startActivityForResult(Intent, int)} and extract the result via
     * {@link #getHintRetrieveResult(Intent)} using the Intent data received in the associated
     * {@link android.app.Activity#onActivityResult(int, int, Intent)} callback.
     *
     * <pre>{@code
     * CredentialClient client = CredentialClient.getInstance(getContext());
     * // ...
     *
     * HintRetrieveRequest request  =
     *     HintRetrieveRequest.fromAuthMethods(
     *         AuthenticationMethods.EMAIL,
     *         AuthenticationMethods.GOOGLE);
     * Intent retrieveHintIntent = client.getHintRetrieveIntent(request);
     * startActivityForResult(retrieveCredentialIntent, RC_RETRIEVE_HINT);
     *
     * // ...
     * @Override
     * public void onActivityResult(int requestCode, int resultCode, Intent data) {
     *     super.onActivityResult(requestCode, resultCode, data);
     *     if (RC_RETRIEVE_HINT == requestCode) {
     *        HintRetrieveResult result = client.getHintRetrieveResult(data);
     *        // handle result ...
     *     }
     * }
     * }</pre>
     *
     * @see #getHintRetrieveResult(Intent)
     */
    @NonNull
    public Intent getHintRetrieveIntent(final HintRetrieveRequest request) {
        List<ComponentName> hintProviders =
                ProviderResolver.findProviders(mApplicationContext, HINT_CREDENTIAL_ACTION);

        if (hintProviders.isEmpty()) {
            ActivityResult result = ActivityResult.of(
                    HintRetrieveResult.CODE_NO_PROVIDER_AVAILABLE,
                    HintRetrieveResult.NO_PROVIDER_AVAILABLE.toResultDataIntent());

            return FinishWithResultActivity.createIntent(mApplicationContext, result);
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
     * Creates a {@link CredentialSaveRequest request} from the given credential.
     *
     * @see #getSaveIntent(Credential)
     */
    @NonNull
    public Intent getSaveIntent(final Credential credential) {
        return getSaveIntent(CredentialSaveRequest.fromCredential(credential));
    }

    /**
     * Provides an Activity intent to save the provided credential.
     *
     * Launch the returned intent via
     * {@link android.app.Activity#startActivityForResult(Intent, int)} and extract the result via
     * {@link #getCredentialSaveResult(Intent)} using the Intent data received in the associated
     * {@link android.app.Activity#onActivityResult(int, int, Intent)} callback.
     *
     * <pre>{@code
     * CredentialClient client = CredentialClient.getInstance(getContext());
     * Credential credential = <Valid Credential>;
     * // ...
     *
     * CredentialSaveRequest request  = CredentialSaveRequest.fromCredential(credential);
     * Intent saveCredentialIntent = client.getSaveIntent(request);
     * startActivityForResult(saveCredentialIntent, RC_SAVE_CREDENTIAL);
     *
     * // ...
     * @Override
     * public void onActivityResult(int requestCode, int resultCode, Intent data) {
     *     super.onActivityResult(requestCode, resultCode, data);
     *     if (RC_SAVE_CREDENTIAL == requestCode) {
     *        CredentialSaveResult result = client.getCredentialSaveResult(data);
     *        // handle result ...
     *     }
     * }
     * }</pre>
     *
     * @see #getCredentialSaveResult(Intent)
     */
    @NonNull
    public Intent getSaveIntent(final CredentialSaveRequest saveRequest) {
        List<ComponentName> saveProviders =
                ProviderResolver.findProviders(mApplicationContext, SAVE_CREDENTIAL_ACTION);

        if (saveProviders.isEmpty()) {
            ActivityResult result = ActivityResult.of(
                    CredentialSaveResult.CODE_NO_PROVIDER_AVAILABLE,
                    CredentialSaveResult.NO_PROVIDER_AVAILABLE.toResultDataIntent());

            return FinishWithResultActivity.createIntent(mApplicationContext, result);
        }

        byte[] encodedSaveRequest = saveRequest.toProtocolBuffer().toByteArray();

        // if there is a preferred provider, directly invoke it.
        ComponentName preferredSaveActivity = getPreferredProvider(saveProviders);
        if (preferredSaveActivity != null) {
            return createSaveIntent(
                    preferredSaveActivity,
                    encodedSaveRequest);
        }

        // otherwise, display a picker for all the providers.
        ArrayList<Intent> saveIntents = new ArrayList<>(saveProviders.size());
        for (ComponentName providerActivity : saveProviders) {
            saveIntents.add(createSaveIntent(
                    providerActivity,
                    encodedSaveRequest));
        }

        return ProviderPickerActivity.createSaveIntent(mApplicationContext, saveIntents);
    }

    /**
     * Creates a {@link CredentialDeleteRequest request} from the given credential.
     *
     * @see #getDeleteIntent(CredentialDeleteRequest)
     */
    @NonNull
    public Intent getDeleteIntent(@NonNull Credential credential) {
        require(credential, notNullValue());

        return getDeleteIntent(CredentialDeleteRequest.fromCredential(credential));
    }

    /**
     * Provides an Activity intent to delete the given {@link Credential credential} from the
     * credential provider on the device.
     *
     * Launch the returned intent via
     * {@link android.app.Activity#startActivityForResult(Intent, int)} and extract the result via
     * {@link #getCredentialDeleteResult(Intent)} using the Intent data received in the associated
     * {@link android.app.Activity#onActivityResult(int, int, Intent)} callback.
     *
     * <pre>{@code
     * CredentialClient client = CredentialClient.getInstance(getContext());
     * Credential credential = <Credential that is no longer valid>;
     * // ...
     *
     * Intent deleteCredentialIntent = client.getDeleteIntent(request);
     * startActivityForResult(deleteCredentialIntent, RC_DELETE_CREDENTIAL);
     *
     * // ...
     * @Override
     * public void onActivityResult(int requestCode, int resultCode, Intent data) {
     *     super.onActivityResult(requestCode, resultCode, data);
     *     if (RC_DELETE_CREDENTIAL == requestCode) {
     *         result = client.getCredentialDeleteResult(data);
     *        // handle result ...
     *     }
     * }
     * }</pre>
     *
     * @see #getCredentialDeleteResult(Intent)
     */
    @NonNull
    public Intent getDeleteIntent(@NonNull CredentialDeleteRequest request) {
        require(request, notNullValue());

        List<ComponentName> deleteProviders =
                ProviderResolver.findProviders(mApplicationContext, DELETE_CREDENTIAL_ACTION);

        if (deleteProviders.isEmpty()) {
            ActivityResult result = ActivityResult.of(
                    CredentialDeleteResult.CODE_NO_PROVIDER_AVAILABLE,
                    CredentialDeleteResult.NO_PROVIDER_AVAILABLE.toResultDataIntent());

            return FinishWithResultActivity.createIntent(mApplicationContext, result);
        }

        byte[] encodedRequest = request.toProtobuf().toByteArray();

        // if there is a preferred provider, directly invoke it.
        ComponentName preferredDeleteActivity = getPreferredProvider(deleteProviders);
        if (preferredDeleteActivity != null) {
            return createDeleteIntent(preferredDeleteActivity, encodedRequest);
        }

        // otherwise, display a picker for all the providers
        ArrayList<Intent> deleteIntents = new ArrayList<>(deleteProviders.size());
        for (ComponentName providerActivity : deleteProviders) {
            deleteIntents.add(createDeleteIntent(
                    providerActivity,
                    encodedRequest));
        }

        return ProviderPickerActivity.createDeleteIntent(mApplicationContext, deleteIntents);
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
            byte[] encodedSaveRequest) {
        Intent saveIntent = createIntent(providerActivity, SAVE_CREDENTIAL_ACTION);
        saveIntent.putExtra(EXTRA_SAVE_REQUEST, encodedSaveRequest);
        return saveIntent;
    }

    private Intent createDeleteIntent(
            ComponentName providerActivity,
            byte[] deleteRequest) {
        Intent deleteIntent = createIntent(providerActivity, DELETE_CREDENTIAL_ACTION);
        deleteIntent.putExtra(EXTRA_DELETE_REQUEST, deleteRequest);
        return deleteIntent;
    }

    /**
     * Returns the result of a {@link CredentialRetrieveResult}.
     *
     * <pre>{@code
     * CredentialClient client = CredentialClient.getInstance(getContext());
     *
     * // ...
     * @Override
     * public void onActivityResult(int requestCode, int resultCode, Intent data) {
     *     super.onActivityResult(requestCode, resultCode, data);
     *     if (RC_RETRIEVE_CREDENTIAL == requestCode) {
     *        CredentialRetrieveResult result = client.getCredentialRetrieveResult(data);
     *        if (result.isSuccessful()) {
     *          // A credential was retrieved, you may automatically sign the user in.
     *          result.getCredential();
     *        } else {
     *          // A credential was not retrieved, you may look at the result code to determine why
     *          // and decide what step to take next. For example, result code may inform you of the
     *          // user's intent such as CredentialRetrieveResult.CODE_USER_CANCELED.
     *          result.getResultCode();
     *        }
     *     }
     * }
     * }</pre>
     *
     *
     * @see #getCredentialRetrieveIntent(CredentialRetrieveRequest)
     */
    @NonNull
    public CredentialRetrieveResult getCredentialRetrieveResult(
            @Nullable Intent resultData) {
        if (resultData == null) {
            Log.i(LOG_TAG, "resultData is null, returning default response");
            return CredentialRetrieveResult.UNKNOWN;
        }

        if (!resultData.hasExtra(EXTRA_RETRIEVE_RESULT)) {
            Log.i(LOG_TAG, "retrieve result missing from response, returning default response");
            return CredentialRetrieveResult.UNKNOWN;
        }

        byte[] resultBytes = resultData.getByteArrayExtra(EXTRA_RETRIEVE_RESULT);
        if (resultBytes == null) {
            Log.i(LOG_TAG, "No retrieve result found in result data, returning default response");
            return CredentialRetrieveResult.UNKNOWN;
        }

        try {
            CredentialRetrieveResult result =
                    CredentialRetrieveResult.fromProtobufBytes(resultBytes);

            // Once a successfully retrieve result has been handled, re-enable auto sign-in.
            if (CredentialRetrieveResult.CODE_CREDENTIAL_SELECTED == result.getResultCode()) {
                mDeviceState.setIsAutoSignInDisabled(false);
            }

            return result;
        } catch (MalformedDataException ex) {
            Log.e(LOG_TAG, "validation of result proto failed, returning default response", ex);
            return CredentialRetrieveResult.UNKNOWN;
        }
    }

    /**
     * Returns the result of a {@link HintRetrieveResult}.
     *
     * <pre>{@code
     * CredentialClient client = CredentialClient.getInstance(getContext());
     *
     * // ...
     * @Override
     * public void onActivityResult(int requestCode, int resultCode, Intent data) {
     *     super.onActivityResult(requestCode, resultCode, data);
     *     if (RC_RETRIEVE_HINT == requestCode) {
     *        HintRetrieveResult result = client.getHintRetrieveResult(data);
     *        if (result.isSuccessful()) {
     *          // A hint was retrieved, you may be able to automatically create an account for the
     *          // user, or offer the user to sign in if an existing account matches the hint.
     *          result.getHint();
     *        } else {
     *          // A credential was not retrieved, you may look at the result code to determine why
     *          // and decide what step to take next. For example, result code may inform you of the
     *          // user's intent such as HintRetrieveResult.CODE_USER_CANCELED.
     *          result.getResultCode();
     *        }
     *     }
     * }
     * }</pre>
     *
     * @see #getHintRetrieveIntent(HintRetrieveRequest)
     */
    @NonNull
    public HintRetrieveResult getHintRetrieveResult(Intent resultData) {
        if (resultData == null) {
            Log.i(LOG_TAG, "resultData is null, returning default response");
            return HintRetrieveResult.UNKNOWN;
        }

        if (!resultData.hasExtra(EXTRA_HINT_RESULT)) {
            Log.i(LOG_TAG, "hint result missing from response, returning default response");
            return HintRetrieveResult.UNKNOWN;
        }

        byte[] resultBytes = resultData.getByteArrayExtra(EXTRA_HINT_RESULT);
        if (resultBytes == null) {
            Log.i(LOG_TAG, "No hint result found in result data, returning default response");
            return HintRetrieveResult.UNKNOWN;
        }

        try {
            return HintRetrieveResult.fromProtobufBytes(resultBytes);
        } catch (MalformedDataException ex) {
            Log.e(LOG_TAG, "hint result is malformed, returning default response", ex);
            return HintRetrieveResult.UNKNOWN;
        }
    }

    /**
     * Returns the result of a {@link CredentialSaveRequest}.
     *
     * <pre>{@code
     * CredentialClient client = CredentialClient.getInstance(getContext());
     *
     * // ...
     * @Override
     * public void onActivityResult(int requestCode, int resultCode, Intent data) {
     *     super.onActivityResult(requestCode, resultCode, data);
     *     if (RC_SAVE_CREDENTIAL == requestCode) {
     *        CredentialSaveResult result = client.getCredentialSaveResult(data);
     *        // Most people will not need to check the result of a save request. Simply fire and
     *        // forget.
     *     }
     * }
     * }</pre>
     *
     * @see #getSaveIntent(CredentialSaveRequest)
     */
    @NonNull
    public CredentialSaveResult getCredentialSaveResult(Intent resultData) {
        if (resultData == null) {
            Log.i(LOG_TAG, "resultData is null, returning default response");
            return CredentialSaveResult.UNKNOWN;
        }

        final byte[] resultBytes = resultData.getByteArrayExtra(EXTRA_SAVE_RESULT);
        if (resultBytes == null) {
            Log.i(LOG_TAG, "No save result found in result data, returning default response");
            return CredentialSaveResult.UNKNOWN;
        }

        try {
            return CredentialSaveResult.fromProtobufBytes(resultBytes);
        } catch (MalformedDataException ex) {
            Log.e(LOG_TAG, "save result is malformed, returning default response", ex);
            return CredentialSaveResult.UNKNOWN;
        }
    }

    /**
     * Returns the result of a {@link CredentialDeleteRequest}.
     *
     * <pre>{@code
     * CredentialClient client = CredentialClient.getInstance(getContext());
     *
     * // ...
     * @Override
     * public void onActivityResult(int requestCode, int resultCode, Intent data) {
     *     super.onActivityResult(requestCode, resultCode, data);
     *     if (RC_DELETE_CREDENTIAL == requestCode) {
     *        CredentialSaveResult result = client.getCredentialSaveResult(data);
     *        // Most people will not need to check the result of a delete request. Simply fire and
     *        // forget.
     *     }
     * }
     * }</pre>
     *
     * @see #getDeleteIntent(CredentialDeleteRequest)
     */
    @NonNull
    public CredentialDeleteResult getCredentialDeleteResult(Intent resultData) {
        try {
            return CredentialDeleteResult.fromResultIntentData(resultData);
        } catch (MalformedDataException ex) {
            Log.w(LOG_TAG, "delete result is missing or malformed, returning default response", ex);
            return CredentialDeleteResult.UNKNOWN;
        }
    }

    /**
     * Disables automatically signing in a user until a successful response is given to
     * {@link CredentialClient#getCredentialRetrieveResult(Intent)}. Instead a user is required to
     * interact with the providers (e.g. click the credential they would like to sign in) before a
     * credential is returned.
     *
     * This feature allows clients to easily avoid a common case where a user signs out and is then
     * automatically signed back in. By calling this method when a user is signed out no additional
     * logic to track the user's intention is required.
     * <pre>{@code
     *
     * // ...
     * public void signOutUser() {
     *     // Your application's custom sign out logic.
     *     // ...
     *     CredentialClient client = CredentialClient.getInstance(getContext());
     *     client.disableAutoSignIn();
     * }
     * }</pre>
     */
    public void disableAutoSignIn() {
        mDeviceState.setIsAutoSignInDisabled(true);
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
                KnownProviders.getInstance(mApplicationContext);

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
}
