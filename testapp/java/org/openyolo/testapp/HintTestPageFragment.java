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

package org.openyolo.testapp;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import org.openyolo.api.CredentialClient;
import org.openyolo.protocol.AuthenticationMethods;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.HintRequest;

/**
 * Fragment which contains a method of testing the OpenYolo credential hint flow.
 */
public final class HintTestPageFragment extends TestPageFragment {

    private static final int RC_HINT = 0;

    @BindView(R.id.authentication_method_text_input)
    EditText mAuthenticationMethodInput;

    @BindView(R.id.hint_credential)
    CredentialView mCredentialView;

    private CredentialClient mApi;

    public String getPageTitle() {
        return "Hint";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApi = CredentialClient.getApplicationBoundInstance(getContext());
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hint_test_layout, container, false);
        ButterKnife.bind(this, view);
        mCredentialView.setEnableInputGeneration(false);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != RC_HINT) {
            showSnackbar(R.string.unknown_response);
            return;
        }

        if (resultCode == RESULT_OK) {
            Credential credential = mApi.getCredentialFromActivityResult(data);
            if (credential == null) {
                showSnackbar(R.string.no_credential_returned);
            } else {
                mCredentialView.setFieldsFromCredential(credential);
            }
        } else if (resultCode == RESULT_CANCELED) {
            showSnackbar(R.string.hint_cancelled);
        } else {
            showSnackbar(R.string.unknown_response);
        }
    }

    @OnClick(R.id.hint_button)
    void onHint() {
        mCredentialView.clearFields();

        String uriString = mAuthenticationMethodInput.getText().toString();
        if (uriString.isEmpty()) {
            showSnackbar(R.string.authentication_field_required);
            return;
        }
        Uri authMethodUri = Uri.parse(uriString);

        HintRequest.Builder requestBuilder = new HintRequest.Builder(authMethodUri);

        Intent hintIntent = mApi.getHintRetrieveIntent(requestBuilder.build());
        if (hintIntent == null) {
            showSnackbar(R.string.no_available_hint_providers);
            return;
        }

        startActivityForResult(hintIntent, RC_HINT);
    }

    @OnClick(R.id.openyolo_id_and_password_provider_button)
    void onIdAndPasswordAuthenticationMethod() {
        mAuthenticationMethodInput.setText(AuthenticationMethods.ID_AND_PASSWORD.toString());
    }

    @OnClick(R.id.google_provider_button)
    void onGoogleAuthenticationMethod() {
        mAuthenticationMethodInput.setText(AuthenticationMethods.GOOGLE.toString());
    }

    @OnClick(R.id.facebook_provider_button)
    void onFacebookAuthenticationMethod() {
        mAuthenticationMethodInput.setText(AuthenticationMethods.FACEBOOK.toString());
    }

    private void showSnackbar(@StringRes int messageId) {
        Snackbar.make(getView(), messageId, Snackbar.LENGTH_SHORT).show();
    }
}

