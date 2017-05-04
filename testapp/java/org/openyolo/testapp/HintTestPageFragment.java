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

import android.content.Intent;
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
import org.openyolo.protocol.AuthenticationMethod;
import org.openyolo.protocol.AuthenticationMethods;
import org.openyolo.protocol.Hint;
import org.openyolo.protocol.HintRetrieveRequest;
import org.openyolo.protocol.HintRetrieveResult;
import org.valid4j.errors.RequireViolation;

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

        // CredentialRetrieveResult result = mApi.getCredentialRetrieveResult(data);
        HintRetrieveResult result = mApi.getHintRetrieveResult(data);
        Hint hint = result.getHint();
        if (hint != null) {
            mCredentialView.setFieldsFromHint(hint);
            return;
        }

        int errorStringResId;
        switch (result.getResultCode()) {
            case HintRetrieveResult.CODE_BAD_REQUEST:
                errorStringResId = R.string.hint_bad_request;
                break;
            case HintRetrieveResult.CODE_NO_HINTS_AVAILABLE:
                errorStringResId = R.string.hint_none_available;
                break;
            case HintRetrieveResult.CODE_USER_CANCELED:
                errorStringResId = R.string.hint_user_canceled;
                break;
            case HintRetrieveResult.CODE_USER_REQUESTS_MANUAL_AUTH:
                errorStringResId = R.string.hint_manual_auth;
                break;
            default:
                errorStringResId = R.string.hint_unknown_response;
        }
        showSnackbar(errorStringResId);
    }

    @OnClick(R.id.hint_button)
    void onHint() {
        mCredentialView.clearFields();

        String authMethodStr = mAuthenticationMethodInput.getText().toString();
        AuthenticationMethod authMethod;
        try {
            authMethod = new AuthenticationMethod(authMethodStr);
        } catch (RequireViolation ex) {
            showSnackbar(R.string.authentication_field_required);
            return;
        }

        HintRetrieveRequest.Builder requestBuilder = new HintRetrieveRequest.Builder(authMethod);

        Intent hintIntent = mApi.getHintRetrieveIntent(requestBuilder.build());
        if (hintIntent == null) {
            showSnackbar(R.string.no_available_hint_providers);
            return;
        }

        startActivityForResult(hintIntent, RC_HINT);
    }

    @OnClick(R.id.openyolo_email_provider_button)
    void onEmailAuthenticationMethod() {
        mAuthenticationMethodInput.setText(AuthenticationMethods.EMAIL.toString());
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

