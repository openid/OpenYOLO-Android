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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import org.openyolo.api.CredentialClient;

/**
 * Test page for actions that do not cleanly fit into another category.
 */
public class OtherTestPageFragment extends TestPageFragment {

    private CredentialClient mApi;

    @Override
    public String getPageTitle() {
        return "Other";
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
        View view = inflater.inflate(R.layout.other_test_layout, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @OnClick(R.id.disable_auto_sign_in_button)
    void onDisableAutoSignIn() {
        mApi.disableAutoSignIn();
    }
}
