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
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import org.openyolo.api.CredentialClient;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.CredentialDeleteResult;

/**
 * Fragment which contains a method of testing the OpenYolo credential deletion flow.
 */
public class DeleteTestPageFragment extends TestPageFragment  {

    private static final int RC_DELETE = 100;

    @BindView(R.id.credential_to_delete)
    CredentialView mCredentialView;

    private CredentialClient mApi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApi = CredentialClient.getInstance(getActivity());
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.delete_test_layout, container, false);

        ButterKnife.bind(this, view);
        mCredentialView.setEnableInputGeneration(true);

        return view;
    }

    @OnClick(R.id.delete_button)
    void onDelete() {
        Credential credential = mCredentialView.makeCredentialFromFields();
        if (credential == null) {
            showSnackbar(R.string.invalid_credential_data);
            return;
        }

        startActivityForResult(mApi.getDeleteIntent(credential), RC_DELETE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != RC_DELETE) {
            showSnackbar(R.string.unknown_response);
            return;
        }

        CredentialDeleteResult deleteResult = mApi.getCredentialDeleteResult(data);

        @StringRes
        int resultMessageId;
        switch (deleteResult.getResultCode()) {
            case CredentialDeleteResult.CODE_DELETED:
                resultMessageId = R.string.delete_result_deleted;
                break;
            case CredentialDeleteResult.CODE_NO_PROVIDER_AVAILABLE:
                resultMessageId = R.string.delete_no_provider_available;
                break;
            case CredentialDeleteResult.CODE_NO_MATCHING_CREDENTIAL:
                resultMessageId = R.string.delete_result_no_matching_credential;
                break;
            case CredentialDeleteResult.CODE_PROVIDER_REFUSED:
                resultMessageId = R.string.delete_result_provider_refused;
                break;
            case CredentialDeleteResult.CODE_USER_CANCELED:
                resultMessageId = R.string.delete_result_user_canceled;
                break;
            case CredentialDeleteResult.CODE_USER_REFUSED:
                resultMessageId = R.string.delete_result_user_refused;
                break;
            case CredentialDeleteResult.CODE_BAD_REQUEST:
                resultMessageId = R.string.delete_result_bad_request;
                break;
            default:
                resultMessageId = R.string.delete_result_unknown_response;
        }

        showSnackbar(resultMessageId);
    }

    @Override
    public String getPageTitle() {
        return "Delete";
    }

    private void showSnackbar(@StringRes int messageId) {
        Snackbar.make(getView(), messageId, Snackbar.LENGTH_SHORT).show();
    }
}
