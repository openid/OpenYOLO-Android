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

package org.openyolo.demoapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import com.bumptech.glide.Glide;
import java.util.concurrent.atomic.AtomicReference;
import org.openyolo.api.AuthenticationDomain;
import org.openyolo.api.AuthenticationMethods;
import org.openyolo.api.Credential;
import org.openyolo.api.CredentialClient;
import org.openyolo.api.HintRequest;
import org.openyolo.api.PasswordSpecification;
import org.openyolo.api.RetrieveCallback;
import org.openyolo.api.RetrieveRequest;
import org.openyolo.api.RetrieveResult;

/**
 * Interacts with the OpenYOLO API to retrieve and save credentials.
 */
public class OpenYoloDemoActivity extends AppCompatActivity {

    private static final String LOG_TAG = "OpenYoloDemoActivity";
    private static final int RC_RETRIEVE = 100;
    private static final int RC_SAVE = 200;
    private static final int RC_HINT = 300;

    private CredentialClient mApi;
    private RandomData mRandomData;

    @BindView(android.R.id.content)
    View mRootView;

    @BindView(R.id.id_field)
    EditText mIdField;

    @BindView(R.id.password_field)
    EditText mPasswordField;

    @BindView(R.id.display_name_field)
    EditText mDisplayNameField;

    @BindView(R.id.federation_provider_field)
    EditText mFederationProviderField;

    @BindView(R.id.profile_picture_field)
    EditText mProfilePictureField;

    @BindView(R.id.profile_picture_view)
    ImageView mProfilePictureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApi = CredentialClient.getApplicationBoundInstance(this);
        mRandomData = new RandomData();
        setContentView(R.layout.main_layout);
        ButterKnife.bind(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RC_RETRIEVE:
                handleRetrieveResult(resultCode, data);
                break;
            case RC_SAVE:
                handleSaveResult(resultCode);
                break;
            case RC_HINT:
                handleHintResult(resultCode, data);
                break;
            default:
                showSnackbar(R.string.unknown_response);
        }
    }

    private void handleRetrieveResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Credential credential = mApi.getCredentialFromActivityResult(data);
            if (credential == null) {
                showSnackbar(R.string.no_credential_returned);
            } else {
                copyCredentialFields(credential);
            }
        } else if (resultCode == RESULT_CANCELED) {
            showSnackbar(R.string.retrieve_cancelled);
        }
    }

    private void handleSaveResult(int resultCode) {
        if (resultCode == RESULT_OK) {
            showSnackbar(R.string.credential_saved);
        } else {
            showSnackbar(R.string.credential_save_canceled);
        }
    }

    private void handleHintResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Credential credential = mApi.getCredentialFromActivityResult(data);
            if (credential == null) {
                showSnackbar(R.string.no_credential_returned);
            } else {
                copyCredentialFields(credential);
            }
        } else if (resultCode == RESULT_CANCELED) {
            showSnackbar(R.string.hint_cancelled);
        }
    }

    private void copyCredentialFields(Credential credential) {
        copyIfNotNull(credential.getIdentifier(), mIdField);
        copyIfNotNull(credential.getPassword(), mPasswordField);
        copyIfNotNull(credential.getDisplayName(), mDisplayNameField);
        copyIfNotNull(credential.getDisplayPicture(), mProfilePictureField);
        copyIfNotNull(credential.getAuthenticationMethod(), mFederationProviderField);
    }

    @OnClick(R.id.retrieve_button)
    void onRetrieve() {
        clearData();
        mApi.retrieve(RetrieveRequest.forSelf(this), new HandleRetrieveResult());
    }

    @OnClick(R.id.save_button)
    void onSave() {
        String authMethod = mFederationProviderField.getText().toString().trim();
        Uri authMethodUri = authMethod.isEmpty()
                ? AuthenticationMethods.ID_AND_PASSWORD
                : Uri.parse(authMethod);

        Credential.Builder credentialBuilder = new Credential.Builder(
                mIdField.getText().toString(),
                authMethodUri,
                AuthenticationDomain.getSelfAuthDomain(this))
                .setDisplayName(convertEmptyToNull(mDisplayNameField.getText().toString()))
                .setDisplayPicture(convertEmptyToNull(mProfilePictureField.getText().toString()));

        if (authMethodUri.equals(AuthenticationMethods.ID_AND_PASSWORD)) {
            credentialBuilder.setPassword(mPasswordField.getText().toString());
        }

        Credential credential = credentialBuilder.build();

        Intent saveIntent = mApi.getSaveIntent(credential);
        if (saveIntent == null) {
            showSnackbar(R.string.no_available_save_providers);
            return;
        }

        startActivityForResult(saveIntent, RC_SAVE);
    }

    @OnClick(R.id.hint_button)
    void onHint() {
        Intent hintIntent = mApi.getHintRetrieveIntent(new HintRequest.Builder(
                AuthenticationMethods.ID_AND_PASSWORD,
                AuthenticationMethods.GOOGLE,
                AuthenticationMethods.FACEBOOK)
                .build());

        if (hintIntent == null) {
            showSnackbar(R.string.no_available_hint_providers);
            return;
        }

        startActivityForResult(hintIntent, RC_HINT);
    }

    @OnClick(R.id.clear_button)
    void clearDataClicked() {
        clearData();
        showSnackbar(R.string.cleared_fields);
    }

    private void clearData() {
        mIdField.setText("");
        mPasswordField.setText("");
        mDisplayNameField.setText("");
        mFederationProviderField.setText("");
        mProfilePictureField.setText("");
        mIdField.requestFocus();
    }

    @OnClick(R.id.generate_id_button)
    void generateId() {
        mIdField.setText(mRandomData.generateEmailAddress());
    }

    @OnClick(R.id.generate_password_button)
    void generatePassword() {
        mPasswordField.setText(PasswordSpecification.DEFAULT.generate());
    }

    @OnClick(R.id.google_provider_button)
    void setGoogleProvider() {
        mFederationProviderField.setText(AuthenticationMethods.GOOGLE.toString());
    }

    @OnClick(R.id.facebook_provider_button)
    void setFacebookProvider() {
        mFederationProviderField.setText(AuthenticationMethods.FACEBOOK.toString());
    }

    @OnClick(R.id.generate_display_name_button)
    void generateDisplayName() {
        mDisplayNameField.setText(mRandomData.generateDisplayName());
    }

    @OnClick(R.id.generate_profile_picture_button)
    void generateProfilePicture() {
        mProfilePictureField.setText(mRandomData.generateProfilePictureUri());
    }

    @OnTextChanged(R.id.profile_picture_field)
    void loadProfilePicture() {
        Glide.with(this)
                .load(Uri.parse(mProfilePictureField.getText().toString()))
                .fitCenter()
                .into(mProfilePictureView);
    }

    private void showSnackbar(@StringRes int messageId) {
        Snackbar.make(mRootView, messageId, Snackbar.LENGTH_SHORT).show();
    }

    private void copyIfNotNull(@Nullable Object value, @NonNull EditText field) {
        if (value != null) {
            field.setText(value.toString());
        }
    }

    private String convertEmptyToNull(@NonNull String str) {
        if (str.isEmpty()) {
            return null;
        }

        return str;
    }

    private class HandleRetrieveResult implements RetrieveCallback, Runnable {

        private final AtomicReference<RetrieveResult> mResult = new AtomicReference<>();
        private final AtomicReference<Throwable> mError = new AtomicReference<>();

        @Override
        public void onComplete(RetrieveResult result, Throwable error) {
            mResult.set(result);
            mError.set(error);
            runOnUiThread(this);
        }

        @Override
        public void run() {
            if (mError.get() != null) {
                showSnackbar(R.string.request_failed);
                Log.w(LOG_TAG, "OpenYOLO request failed", mError.get());
                return;
            }

            RetrieveResult result = mResult.get();
            if (result.getRetrieveIntent() == null) {
                showSnackbar(R.string.no_credentials);
                Log.i(LOG_TAG, "OpenYOLO retrieve result: No credentials available");
                return;
            }

            Log.i(LOG_TAG, "OpenYOLO retrieve result: Credentials available");
            startActivityForResult(result.getRetrieveIntent(), RC_RETRIEVE);
        }
    }
}
