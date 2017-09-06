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

package org.openyolo.demoapp.passwordlogin;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.net.Uri;
import android.support.annotation.AnyThread;
import android.support.annotation.Keep;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.WorkerThread;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.openyolo.api.CredentialClient;
import org.openyolo.demoapp.passwordlogin.userdata.HashUtil;
import org.openyolo.demoapp.passwordlogin.userdata.UserDataSource;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.AuthenticationMethods;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.CredentialRetrieveRequest;
import org.openyolo.protocol.CredentialRetrieveResult;
import org.openyolo.protocol.CredentialSaveRequest;
import org.openyolo.protocol.CredentialSaveResult;
import org.openyolo.protocol.Hint;
import org.openyolo.protocol.HintRetrieveRequest;
import org.openyolo.protocol.HintRetrieveResult;
import org.openyolo.protocol.RetrieveBbqResponse;

/**
 * The view model for {@link LoginActivity}. Defines all the logic for authenticating a user
 * which an email address and password, including interacting with any available OpenYOLO provider
 * to directly retrieve or generate credentials.
 */
@WorkerThread
public final class LoginViewModel extends ObservableViewModel {

    private static final String TAG = "LoginViewModel";

    private static final int MIN_PASSWORD_LENGTH = 4;

    /**
     * Indicates whether a progress bar should be displayed to the user, while an asynchronous
     * request is made.
     */
    public final ObservableBoolean showLoading = new ObservableBoolean(true);

    /**
     * Describes the asynchronous request that is occurring to the user.
     */
    public final ObservableField<String> loadingLabel = new ObservableField<>("Loading…");

    /**
     * The prompt that is displayed to the user for manual authentication, instructing them what
     * to do next.
     */
    public final ObservableField<String> authPrompt = new ObservableField<>("");

    /**
     * The email address the user has entered, or that was retrieved from their OpenYOLO provider.
     */
    public final ObservableField<String> email = new ObservableField<>("");

    /**
     * The error string displayed below the email text input element. If the string is empty,
     * the error is not displayed.
     */
    public final ObservableField<String> emailError = new ObservableField<>("");

    /**
     * Whether the password entry field should be displayed at this time.
     */
    public final ObservableBoolean showPasswordField = new ObservableBoolean(false);

    /**
     * The password the user has entered.
     */
    public final ObservableField<String> password = new ObservableField<>("");

    /**
     * The error string displayed below the password text input element. If the string is empty,
     * the error is not displayed.
     */
    public final ObservableField<String> passwordError = new ObservableField<>("");

    /**
     * Whether to show a generic error message to the user - this happens when requests fail
     * in a way that we cannot recover from, other than asking the user to potentially try again.
     */
    public final ObservableBoolean showError = new ObservableBoolean(false);

    @SuppressLint("StaticFieldLeak")
    private final OpenYoloDemoApplication mApplication;
    private final CredentialClient mCredentialClient;
    private final UserDataSource mUserDataSource;

    private WeakReference<LoginNavigator> mNavigator;
    private AtomicBoolean mFirstLoad = new AtomicBoolean(true);

    /**
     * Creates the view model, with the required application reference.
     */
    @Keep
    public LoginViewModel(Application application) {
        super(application);
        mApplication = OpenYoloDemoApplication.getInstance(application);
        mUserDataSource = mApplication.getUserRepository();
        mCredentialClient = CredentialClient.getInstance(mApplication);
    }

    /**
     * Specifies the navigator instance used by the view model to interact with the
     * activity environment.
     */
    @MainThread
    void setNavigator(LoginNavigator navigator) {
        mNavigator = new WeakReference<LoginNavigator>(navigator);
    }

    /**
     * The main entry point. If the model has not been previously started, it will attempt to
     * authenticate the user with the help of OpenYOLO, and if this fails, facilitate manual
     * authentication attempts until success.
     */
    void start() {
        if (!mFirstLoad.compareAndSet(true, false)) {
            // already started
            return;
        }

        if (mUserDataSource.getCurrentUser() != null) {
            // already signed in, go straight to the main activity
            mNavigator.get().goToMain();
            return;
        }

        tryRetrieveExistingCredential();
    }

    /**
     * Initiates an OpenYOLO credential retrieval request, and forwards the response to
     * {@link #handleRetrieveQueryResult(RetrieveBbqResponse, Throwable)} for processing.
     */
    private void tryRetrieveExistingCredential() {
        setShowLoading(R.string.existing_account_search_prompt);
        mCredentialClient.retrieve(
                CredentialRetrieveRequest.forAuthenticationMethods(AuthenticationMethods.EMAIL),
                (queryResponse, queryError) ->
                    getExecutor().execute(
                            () -> handleRetrieveQueryResult(queryResponse, queryError)));
    }

    /**
     * Inspects the initial response from the OpenYOLO retrieve request. If a retrieve intent
     * is available, this is used to attempt to source a credential from the provider. The result
     * of this is indirectly routed to {@link #handleRetrieveResult(Intent)}.
     *
     * If no intent is returned, an attempt is made to source an account hint instead.
     */
    private void handleRetrieveQueryResult(
            RetrieveBbqResponse queryResponse,
            Throwable queryError) {
        if (queryResponse != null && queryResponse.getRetrieveIntent() != null) {
            Log.i(TAG, "retrieving credential from provider");
            mNavigator.get().startOpenYoloRetrieve(queryResponse.getRetrieveIntent());
            return;
        }

        Log.i(TAG, "no OpenYOLO providers for retrieve");
        tryRetrieveHint();
    }

    /**
     * Invoked by {@link LoginActivity} when an OpenYOLO provider completes its processing of
     * the credential retrieve request, potentially with user input.
     *
     * If a credential is returned, an authentication attempt is made using it. If this fails, the
     * user is prompted to manually authenticate.
     *
     * If no credential is returned, an attempt is made to source an account hint instead.
     */
    public void handleRetrieveResult(Intent data) {
        CredentialRetrieveResult result = mCredentialClient.getCredentialRetrieveResult(data);

        switch (result.getResultCode()) {
            case CredentialRetrieveResult.CODE_CREDENTIAL_SELECTED:
                Log.i(TAG, "Credential was returned by OpenYOLO provider");
                Credential credential = result.getCredential();
                boolean authenticated = mUserDataSource.authWithPassword(
                        credential.getIdentifier(),
                        credential.getPassword());
                if (authenticated) {
                    // save the credential back to the provider, as a signal that it still works.
                    trySaveCredential(credential);
                } else {
                    doManualAuthentication(
                            R.string.incorrect_stored_password,
                            credential.getIdentifier());
                }
                return;

            // you may wish to behave differently in your own application in response to each of
            // the following cases (which are enumerated here to demonstrate the possible
            // responses), but for simplicity in this demo app we treat them all as requiring
            // manual authentication by the user
            case CredentialRetrieveResult.CODE_NO_CREDENTIALS_AVAILABLE:
                Log.i(TAG, "Provider indicated that no credentials are available");
                break;
            case CredentialRetrieveResult.CODE_USER_CANCELED:
                Log.i(TAG, "The user canceled selection of an existing credential");
                break;
            case CredentialRetrieveResult.CODE_USER_REQUESTS_MANUAL_AUTH:
                Log.i(TAG, "The user requests manual authentication");
                break;
            case CredentialRetrieveResult.CODE_BAD_REQUEST:
                Log.i(TAG, "Provider indicated that the request was malformed");
                break;
            case CredentialRetrieveResult.CODE_UNKNOWN:
            default:
                Log.i(TAG, "An unknown error occurred in the credential retrieval flow");
                break;
        }

        doManualAuthentication(
                R.string.enter_email_prompt,
                null); // userEmail
    }

    /**
     * Attempts to retrieve an account hint from the OpenYOLO provider. Hint responses are
     * indirectly routed to {@link #handleHintResult(Intent)}. If no hint provider is
     * available, then the user is prompted to manually authenticate.
     */
    private void tryRetrieveHint() {
        Intent hintIntent = mCredentialClient.getHintRetrieveIntent(
                HintRetrieveRequest.of(AuthenticationMethods.EMAIL));

        if (hintIntent != null) {
            setShowLoading(R.string.requesting_hint_prompt);
            mNavigator.get().startOpenYoloHint(hintIntent);
            return;
        }

        Log.i(TAG, "No OpenYOLO providers for hint");
        doManualAuthentication(
                R.string.enter_email_prompt,
                null); // userEmail
    }

    /**
     * Handles the hint retrieval result from the OpenYOLO provider. If a hint is returned, a
     * check is made to determine whether an account already exists for the returned identifier.
     * If it is, the user is prompted to enter their password for that account. Otherwise, an
     * account is automatically created using the returned hint details.
     *
     * If no hint is returned, the user is prompted to manually authenticate.
     */
    public void handleHintResult(Intent data) {
        HintRetrieveResult result = mCredentialClient.getHintRetrieveResult(data);

        switch (result.getResultCode()) {
            case HintRetrieveResult.CODE_HINT_SELECTED:
                Log.i(TAG, "User selected a hint from the OpenYOLO provider");
                Hint hint = result.getHint();

                if (mUserDataSource.isExistingAccount(hint.getIdentifier())) {
                    doManualAuthentication(
                            R.string.existing_account_enter_password,
                            hint.getIdentifier());
                    return;
                }

                String hintEmail = hint.getIdentifier();
                String displayName = normalizeDisplayName(hint.getDisplayName());
                String displayPicture = normalizeDisplayPictureUri(
                        hint.getIdentifier(),
                        hint.getDisplayPictureUri());
                String hintPassword = hint.getGeneratedPassword();


                // attempt to create an account with the returned hint. If this fails, ask the
                // user to manually authenticate.
                boolean authenticated = false;

                if (hintPassword != null) {
                    authenticated = mUserDataSource.createPasswordAccount(
                            hintEmail, displayName, displayPicture, hintPassword);

                    if (authenticated) {
                        // account created, attempt to save it back to the OpenYOLO provider
                        trySaveCredential(
                                new Credential.Builder(
                                        hintEmail,
                                        AuthenticationMethods.EMAIL,
                                        AuthenticationDomain.getSelfAuthDomain(mApplication))
                                        .setDisplayName(displayName)
                                        .setDisplayPicture(displayPicture)
                                        .setPassword(hintPassword)
                                        .build());
                    }
                }

                if (!authenticated) {
                    doManualAuthentication(
                            R.string.new_account_enter_password,
                            hint.getIdentifier());
                }

                return;

            // you may wish to behave differently in your own application in response to each of
            // the following cases (which are enumerated here to demonstrate the possible
            // responses), but for simplicity in this demo app we treat them all as requiring
            // manual authentication by the user
            case HintRetrieveResult.CODE_NO_HINTS_AVAILABLE:
                Log.i(TAG, "Provider indicated there are no hints available");
                break;
            case HintRetrieveResult.CODE_USER_CANCELED:
                Log.i(TAG, "User canceled the hint selection flow");
                break;
            case HintRetrieveResult.CODE_USER_REQUESTS_MANUAL_AUTH:
                Log.i(TAG, "User requests manual authentication");
                break;
            case HintRetrieveResult.CODE_BAD_REQUEST:
                Log.i(TAG, "Proivder indicates that the request was malformed");
                break;
            case HintRetrieveResult.CODE_UNKNOWN:
            default:
                Log.i(TAG, "An unknown error occurred in the hint retrieval flow");
                break;
        }

        doManualAuthentication(
                R.string.enter_email_prompt,
                null); // userEmail
    }

    /**
     * Prompts the user to manually authenticate, with a context-specific message.
     */
    private void doManualAuthentication(
            @StringRes int authPromptId,
            @Nullable String userEmail) {
        this.authPrompt.set(getResourceString(authPromptId));
        this.email.set(userEmail != null ? userEmail : "");
        this.showPasswordField.set(userEmail != null);
        this.password.set("");
        setLoaded();
    }

    /**
     * Pushes handling of a sign in button click on the form to a background thread for processing.
     */
    @MainThread
    public void signInButtonClicked(View view) {
        getExecutor().execute(this::tryManualAuthentication);
    }

    /**
     * Processes an attempt to manually authenticate.
     */
    private void tryManualAuthentication() {
        String userEmail = this.email.get().trim();
        String userPassword = this.password.get();

        if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            emailError.set(getResourceString(R.string.error_invalid_email));
            return;
        } else {
            emailError.set("");
        }

        if (userPassword.length() < MIN_PASSWORD_LENGTH) {
            passwordError.set(getResourceString(R.string.error_password_too_short));
            return;
        } else {
            emailError.set("");
        }

        if (mUserDataSource.isExistingAccount(userEmail)) {
            if (userPassword.isEmpty()) {
                authPrompt.set(getResourceString(R.string.existing_account_enter_password));
                showPasswordField.set(true);
            } else if (mUserDataSource.authWithPassword(userEmail, userPassword)) {
                trySaveCredential(
                        new Credential.Builder(
                                userEmail,
                                AuthenticationMethods.EMAIL,
                                AuthenticationDomain.getSelfAuthDomain(mApplication))
                                .setPassword(userPassword)
                                .build());
            } else {
                passwordError.set(getResourceString(R.string.error_incorrect_password));
            }
        } else {
            if (userPassword.isEmpty()) {
                authPrompt.set(getResourceString(R.string.new_account_enter_password));
                showPasswordField.set(true);
            } else if (mUserDataSource.createPasswordAccount(
                    userEmail,
                    null, // name
                    null, // profilePictureUri
                    userPassword)) {
                trySaveCredential(
                        new Credential.Builder(
                                userEmail,
                                AuthenticationMethods.EMAIL,
                                AuthenticationDomain.getSelfAuthDomain(mApplication))
                                .setPassword(userPassword)
                                .build());
            } else {
                showError.set(true);
            }
        }
    }

    private void trySaveCredential(Credential credential) {
        Intent saveIntent =
                mCredentialClient.getSaveIntent(CredentialSaveRequest.fromCredential(credential));

        if (saveIntent != null) {
            Log.i(TAG, "Attempting to save credential to OpenYOLO provider");
            mNavigator.get().startSave(saveIntent);
            return;
        }

        Log.i(TAG, "No OpenYOLO providers to save credential");
        mNavigator.get().goToMain();
    }

    /**
     * Handles the credential save result from the OpenYOLO provider, returned via
     * {@link LoginActivity}.
     */
    public void handleSaveResult(Intent data) {
        CredentialSaveResult result = mCredentialClient.getCredentialSaveResult(data);

        // you may wish to handle the outcomes of save differently in your own application; for this
        // demo, the outcome of the save request is mostly irrelevant.
        switch (result.getResultCode()) {
            case CredentialSaveResult.CODE_SAVED:
                Log.i(TAG, "User saved credential");
                break;
            case CredentialSaveResult.CODE_USER_CANCELED:
                Log.i(TAG, "User canceled credential save");
                break;
            case CredentialSaveResult.CODE_USER_REFUSED:
                Log.i(TAG, "User refused to save credential");
                break;
            case CredentialSaveResult.CODE_PROVIDER_REFUSED:
                Log.i(TAG, "Provider refused to save credential");
                break;
            case CredentialSaveResult.CODE_BAD_REQUEST:
                Log.i(TAG, "Provider indicated the save request was malformed");
                break;
            case CredentialSaveResult.CODE_UNKNOWN:
            default:
                Log.i(TAG, "An unknown error occurred while saving the credential");
                break;
        }

        mNavigator.get().goToMain();
    }

    private void setShowLoading(@StringRes int loadingLabelId) {
        setShowLoading(getApplication().getResources().getString(loadingLabelId));
    }

    private void setShowLoading(String reason) {
        this.loadingLabel.set(reason);
        showLoading.set(true);
    }

    private void setLoaded() {
        loadingLabel.set("");
        showLoading.set(false);
    }

    @AnyThread
    private String getResourceString(@StringRes int stringId, Object... formatArgs) {
        return getApplication().getResources().getString(stringId, formatArgs);
    }

    @AnyThread
    private ScheduledExecutorService getExecutor() {
        return ((OpenYoloDemoApplication)getApplication()).getExecutor();
    }

    @NonNull
    private String normalizeDisplayName(@Nullable String displayName) {
        return displayName != null ? displayName : "J Doe";
    }

    @NonNull
    private String normalizeDisplayPictureUri(
            @NonNull String userEmail,
            @Nullable Uri displayPictureUri) {
        if (displayPictureUri != null) {
            return displayPictureUri.toString();
        }

        return "https://robohash.org/" + HashUtil.base64Hash(userEmail) + "?set=set3";
    }
}
