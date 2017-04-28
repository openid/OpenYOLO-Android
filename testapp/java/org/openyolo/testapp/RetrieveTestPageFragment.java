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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.openyolo.api.CredentialClient;
import org.openyolo.api.RetrieveCallback;
import org.openyolo.protocol.AuthenticationMethod;
import org.openyolo.protocol.CredentialRetrieveRequest;
import org.openyolo.protocol.CredentialRetrieveResult;
import org.openyolo.protocol.RetrieveBbqResponse;

/**
 * Fragment which contains a method of testing the OpenYolo credential retrieve flow.
 */
public final class RetrieveTestPageFragment extends TestPageFragment {

    private static final int RC_RETRIEVE = 0;

    @BindView(R.id.authentication_methods_input)
    AuthenticationMethodsInputView mAuthenticationMethodsInputView;

    @BindView(R.id.retrieve_credential)
    CredentialView mCredentialView;

    private CredentialClient mApi;

    public String getPageTitle() {
        return "Retrieve";
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
        if (result.getCredential() != null) {
            mCredentialView.setFieldsFromCredential(result.getCredential());
            return;
        }


        int retrieveResultCode = result.getResultCode();
        if (retrieveResultCode == CredentialRetrieveResult.RESULT_REJECTED_BY_USER) {
            showSnackbar(R.string.credential_retrieve_user_rejected);
        } else if (retrieveResultCode == CredentialRetrieveResult.RESULT_REJECTED_BY_PROVIDER) {
            showSnackbar(R.string.credential_retrieve_provider_rejected);
        } else {
            showSnackbar(R.string.unknown_response);
        }
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
        CredentialRetrieveRequest request =
                CredentialRetrieveRequest.forAuthenticationMethods(authenticationMethods);

        mApi.retrieve(request, new HandleRetrieveResult());
    }

    private void showSnackbar(@StringRes int messageId) {
        Snackbar.make(getView(), messageId, Snackbar.LENGTH_SHORT).show();
    }

    private class HandleRetrieveResult implements RetrieveCallback, Runnable {

        private final AtomicReference<RetrieveBbqResponse> mResult = new AtomicReference<>();
        private final AtomicReference<Throwable> mError = new AtomicReference<>();

        @Override
        public void onComplete(RetrieveBbqResponse result, Throwable error) {
            mResult.set(result);
            mError.set(error);
            getActivity().runOnUiThread(this);
        }

        @Override
        public void run() {
            if (mError.get() != null) {
                showSnackbar(R.string.request_failed);
                return;
            }

            RetrieveBbqResponse result = mResult.get();
            if (result.getRetrieveIntent() == null) {
                showSnackbar(R.string.no_credentials);
                return;
            }

            try {
                startActivityForResult(result.getRetrieveIntent(), RC_RETRIEVE);
            } catch (ActivityNotFoundException ex) {
                showSnackbar(R.string.credential_retrieve_activity_does_not_exist);
            }
        }
    }
}

