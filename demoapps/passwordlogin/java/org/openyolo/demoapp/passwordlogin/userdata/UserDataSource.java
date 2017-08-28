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

package org.openyolo.demoapp.passwordlogin.userdata;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A service which can authenticate users, and remember who the current user is.
 */
public interface UserDataSource {

    /**
     * The currently authenticated user, if any.
     */
    @Nullable
    User getCurrentUser();

    /**
     * Whether an account exists for the specified email address.
     */
    boolean isExistingAccount(@NonNull String email);

    /**
     * Attempts to create and persist a new user account account with the specified properties;
     * returns true if a new account is created.
     */
    boolean createPasswordAccount(
            @NonNull String email,
            @NonNull String name,
            @NonNull String profilePictureUri,
            @NonNull String password);

    /**
     * Attempts to authenticate an existing user with the provided credentials; returns true if
     * authentication succeeds.
     */
    boolean authWithPassword(@NonNull String email, @NonNull String password);

    /**
     * De-authenticates the current user.
     */
    void signOut();
}
