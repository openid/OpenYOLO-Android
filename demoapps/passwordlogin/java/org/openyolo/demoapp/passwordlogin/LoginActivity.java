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

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.AnyThread;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import java.util.concurrent.ScheduledExecutorService;
import org.openyolo.demoapp.passwordlogin.databinding.LoginLayoutBinding;

public final class LoginActivity extends AppCompatActivity implements LoginNavigator {

    private static final String TAG = "LoginActivity";

    private static final int RC_RETRIEVE = 100;
    private static final int RC_HINT = 101;
    private static final int RC_SAVE = 102;

    private LoginViewModel mViewModel;
    private LoginLayoutBinding mViewDataBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        if (mViewDataBinding == null) {
            mViewDataBinding = LoginLayoutBinding.bind(findViewById(R.id.contentFrame));
        }

        mViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        mViewModel.setNavigator(this);
        mViewDataBinding.setViewmodel(mViewModel);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getExecutorService().execute(mViewModel::start);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RC_RETRIEVE:
                getExecutorService().execute(() -> mViewModel.handleRetrieveResult(data));
                break;
            case RC_HINT:
                getExecutorService().execute(() -> mViewModel.handleHintResult(data));
                break;
            case RC_SAVE:
                getExecutorService().execute(() -> mViewModel.handleSaveResult(data));
                break;
            default:
                Log.w(TAG, "Unexpected request code, ignoring");
        }
    }

    @AnyThread
    @Override
    public void startOpenYoloRetrieve(Intent retrieveIntent) {
        runOnUiThread(() -> startActivityForResult(retrieveIntent, RC_RETRIEVE));
    }

    @AnyThread
    @Override
    public void startOpenYoloHint(Intent hintIntent) {
        runOnUiThread(() -> startActivityForResult(hintIntent, RC_HINT));
    }

    @AnyThread
    @Override
    public void startSave(Intent saveIntent) {
        runOnUiThread(() -> startActivityForResult(saveIntent, RC_SAVE));
    }

    @AnyThread
    @Override
    public void goToMain() {
        runOnUiThread(() -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    @AnyThread
    private ScheduledExecutorService getExecutorService() {
        return ((OpenYoloDemoApplication) getApplication()).getExecutor();
    }
}
