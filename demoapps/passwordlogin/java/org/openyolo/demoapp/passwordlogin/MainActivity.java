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
import java.util.concurrent.ScheduledExecutorService;
import org.openyolo.demoapp.passwordlogin.databinding.MainLayoutBinding;

public final class MainActivity extends AppCompatActivity implements MainNavigator {

    private MainLayoutBinding mViewDataBinding;
    private MainViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        if (mViewDataBinding == null) {
            mViewDataBinding = MainLayoutBinding.bind(findViewById(R.id.contentFrame));
        }

        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mViewModel.setNavigator(this);
        mViewDataBinding.setViewmodel(mViewModel);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getExecutorService().execute(mViewModel::start);
    }

    @AnyThread
    @Override
    public void goToLogin() {
        runOnUiThread(() -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @AnyThread
    private ScheduledExecutorService getExecutorService() {
        return ((OpenYoloDemoApplication) getApplication()).getExecutor();
    }
}
