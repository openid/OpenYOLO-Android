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
import android.widget.CheckBox;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java.util.Set;
import org.openyolo.api.CredentialClient;
import org.openyolo.protocol.AuthenticationMethod;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.CredentialRetrieveRequest;
import org.openyolo.protocol.CredentialRetrieveResult;

/**
 * Fragment which contains a method of testing the OpenYolo credential retrieve flow.
 */
public final class RetrieveTestPageFragment extends TestPageFragment {

    private static final int RC_RETRIEVE = 0;

    @BindView(R.id.authentication_methods_input)
    AuthenticationMethodsInputView mAuthenticationMethodsInputView;

    @BindView(R.id.token_providers_input)
    TokenProviderInputView mTokenProviderInputView;

    @BindView(R.id.retrieve_credential)
    CredentialView mCredentialView;

    @BindView(R.id.require_user_mediation_check_box)
    CheckBox mRequireUserMediationCheckBox;

    @BindView(R.id.id_token_view)
    IdTokenView mIdTokenView;

    private CredentialClient mApi;

    public String getPageTitle() {
        return "Retrieve";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApi = CredentialClient.getInstance(getContext());
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.retrieve_test_layout, container, false);

        ButterKnife.bind(this, view);
        mCredentialView.setEnableInputGeneration(false);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != RC_RETRIEVE) {
            showSnackbar(R.string.unknown_response);
            return;
        }

        CredentialRetrieveResult result = mApi.getCredentialRetrieveResult(data);
        Credential credential = result.getCredential();
        if (credential != null) {
            mCredentialView.setFieldsFromCredential(credential);
            mIdTokenView.setIdToken(credential.getIdToken());
            return;
        }

        int resultMessageId;
        switch (result.getResultCode()) {
            case CredentialRetrieveResult.CODE_NO_CREDENTIALS_AVAILABLE:
                resultMessageId = R.string.retrieve_no_credentials;
                break;
            case CredentialRetrieveResult.CODE_NO_PROVIDER_AVAILABLE:
                resultMessageId = R.string.retrieve_no_provider_available;
                break;
            case CredentialRetrieveResult.CODE_BAD_REQUEST:
                resultMessageId = R.string.retrieve_bad_request;
                break;
            case CredentialRetrieveResult.CODE_USER_CANCELED:
                resultMessageId = R.string.retrieve_user_canceled;
                break;
            case CredentialRetrieveResult.CODE_USER_REQUESTS_MANUAL_AUTH:
                resultMessageId = R.string.retrieve_manual_auth;
                break;
            default:
                resultMessageId = R.string.retrieve_unknown_response;
        }

        showSnackbar(resultMessageId);
    }

    @OnClick(R.id.retrieve_button)
    void onRetrieve() {
        mCredentialView.clearFields();

        Set<AuthenticationMethod> authenticationMethods =
                mAuthenticationMethodsInputView.getEnabledAuthenticationMethods();

        if (authenticationMethods.isEmpty()) {
            showSnackbar(R.string.authentication_field_required);
            return;
        }
        CredentialRetrieveRequest.Builder requestBuilder =
                new CredentialRetrieveRequest.Builder(authenticationMethods)
                        .setTokenProviders(mTokenProviderInputView.getTokenProviders());

        if (mRequireUserMediationCheckBox.isChecked()) {
            requestBuilder.setRequireUserMediation(true);
        }

        startActivityForResult(
                mApi.getCredentialRetrieveIntent(requestBuilder.build()),
                RC_RETRIEVE);
    }

    private void showSnackbar(@StringRes int messageId) {
        Snackbar.make(getView(), messageId, Snackbar.LENGTH_SHORT).show();
    }
}

