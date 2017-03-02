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
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java.util.concurrent.atomic.AtomicReference;
import org.openyolo.api.AuthenticationDomain;
import org.openyolo.api.AuthenticationMethods;
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

    @BindView(R.id.authentication_method)
    EditText mAuthenticationMethod;

    private CredentialFragment mCredentialFragment;
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

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        FragmentManager fm = getChildFragmentManager();
        mCredentialFragment = (CredentialFragment) fm.findFragmentById(R.id.retrieve_credential);
        if (null == mCredentialFragment) {
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            mCredentialFragment = CredentialFragment.newInstance(false /* enableInputGeneration */);
            fragmentTransaction.add(R.id.retrieve_credential, mCredentialFragment);
            fragmentTransaction.commit();
        }
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
                mCredentialFragment.setFieldsFromCredential(credential);
            }
        } else if (resultCode == RESULT_CANCELED) {
            showSnackbar(R.string.retrieve_cancelled);
        } else {
            showSnackbar(R.string.unknown_response);
        }
    }

    @OnClick(R.id.retrieve_button)
    void onRetrieve() {
        mCredentialFragment.clearFields();

        Uri authenticationMethod = Uri.parse(mAuthenticationMethod.getText().toString());
        AuthenticationDomain authenticationDomain =
                AuthenticationDomain.getSelfAuthDomain(getContext());

        RetrieveRequest request = new RetrieveRequest.Builder(authenticationDomain)
                .setAuthenticationMethods(authenticationMethod)
                .build();
        mApi.retrieve(request, new HandleRetrieveResult());
    }

    @OnClick(R.id.openyolo_id_and_password_provider_button)
    void onIdAndPasswordAuthenticationMethod() {
        mAuthenticationMethod.setText(AuthenticationMethods.ID_AND_PASSWORD.toString());
    }

    @OnClick(R.id.google_provider_button)
    void onGoogleAuthenticationMethod() {
        mAuthenticationMethod.setText(AuthenticationMethods.GOOGLE.toString());
    }

    @OnClick(R.id.facebook_provider_button)
    void onFacebookAuthenticationMethod() {
        mAuthenticationMethod.setText(AuthenticationMethods.FACEBOOK.toString());
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

