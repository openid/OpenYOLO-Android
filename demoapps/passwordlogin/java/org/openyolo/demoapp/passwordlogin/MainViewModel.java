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

import android.app.Application;
import android.databinding.ObservableField;
import android.support.annotation.AnyThread;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.view.View;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.openyolo.demoapp.passwordlogin.userdata.User;
import org.openyolo.demoapp.passwordlogin.userdata.UserDataSource;

/**
 * The view model for the main activity, which is displayed when the user has authenticated
 * successfully.
 */
@WorkerThread
public final class MainViewModel extends ObservableViewModel {

    /**
     * The user's display name for display on screen, if known.
     */
    public final ObservableField<String> displayName = new ObservableField<>("");

    /**
     * The user's email address for display on screen.
     */
    public final ObservableField<String> email = new ObservableField<>("");

    /**
     * The user's display picture for display on screen, if known.
     */
    public final ObservableField<String> displayPicture = new ObservableField<>("");

    private final AtomicBoolean mFirstLoad = new AtomicBoolean(true);
    private final UserDataSource mUserDataSource;
    private MainNavigator mNavigator;

    /**
     * Creates the view model, with the required application reference.
     */
    public MainViewModel(@NonNull Application application) {
        super(application);
        mUserDataSource = ((OpenYoloDemoApplication)application).getUserRepository();
    }

    /**
     * Specifies the navigator for use in communicating with the activity environment.
     */
    @MainThread
    public void setNavigator(@NonNull MainNavigator navigator) {
        mNavigator = navigator;
    }

    /**
     * The entry point to the view model's behavior. Determines whether a user is currently
     * authenticated, and if not, redirects to the login activity. Otherwise, the currently
     * authenticated user is displayed on screen.
     */
    public void start() {
        if (!mFirstLoad.compareAndSet(true, false)) {
            // already started
            return;
        }

        User currentUser = mUserDataSource.getCurrentUser();
        if (currentUser == null) {
            // no user available, go to login screen
            mNavigator.goToLogin();
            return;
        }

        this.email.set(currentUser.getEmail());
        this.displayName.set(currentUser.getName());
        this.displayPicture.set(currentUser.getProfilePictureUri());
    }

    /**
     * Delegates handling of sign-out to a background thread, which de-authenticates the current
     * user and redirects to the login activity.
     */
    @MainThread
    public void onSignOutClicked(View view) {
        getExecutor().execute(() -> {
            mUserDataSource.signOut();
            mNavigator.goToLogin();
        });
    }

    @AnyThread
    private ScheduledExecutorService getExecutor() {
        return OpenYoloDemoApplication.getInstance(getApplication()).getExecutor();
    }
}
