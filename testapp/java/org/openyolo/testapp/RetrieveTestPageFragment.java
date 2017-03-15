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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
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

import org.openyolo.api.Credential;
import org.openyolo.api.CredentialClient;
import org.openyolo.api.RetrieveCallback;
import org.openyolo.api.RetrieveRequest;
import org.openyolo.api.RetrieveResult;

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

        if (resultCode == RESULT_OK) {
            Credential credential = mApi.getCredentialFromActivityResult(data);
            if (credential == null) {
                showSnackbar(R.string.no_credential_returned);
            } else {
                mCredentialView.setFieldsFromCredential(credential);
            }
        } else if (resultCode == RESULT_CANCELED) {
            showSnackbar(R.string.retrieve_cancelled);
        } else {
            showSnackbar(R.string.unknown_response);
        }
    }

    @OnClick(R.id.retrieve_button)
    void onRetrieve() {
        mCredentialView.clearFields();

        Set<Uri> authenticationMethods =
                mAuthenticationMethodsInputView.getEnabledAuthenticationMethods();

        if (authenticationMethods.isEmpty()) {
            showSnackbar(R.string.authentication_field_required);
            return;
        }
        RetrieveRequest request = RetrieveRequest.forAuthenticationMethods(authenticationMethods);

        mApi.retrieve(request, new HandleRetrieveResult());
    }

    private void showSnackbar(@StringRes int messageId) {
        Snackbar.make(getView().getRootView(), messageId, Snackbar.LENGTH_SHORT).show();
    }

    private class HandleRetrieveResult implements RetrieveCallback, Runnable {

        private final AtomicReference<RetrieveResult> mResult = new AtomicReference<>();
        private final AtomicReference<Throwable> mError = new AtomicReference<>();

        @Override
        public void onComplete(RetrieveResult result, Throwable error) {
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

            RetrieveResult result = mResult.get();
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

