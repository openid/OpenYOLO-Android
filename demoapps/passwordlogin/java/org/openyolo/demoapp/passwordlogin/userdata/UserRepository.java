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

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import org.openyolo.demoapp.passwordlogin.OpenYoloDemoApplication;

/**
 * A local implementation of UserDataSource for the purposes of this demo. This could be
 * replaced with a more realistic implementation that makes network requests in order to
 * authenticate the user.
 */
@WorkerThread
public class UserRepository implements UserDataSource {

    private static final String USER_DB_NAME = "userdb";
    private static final String AUTH_INFO_SHARED_PREFS = "authInfo";
    private static final String KEY_CURRENT_USER = "user";

    private final UserDatabase mUserDatabase;
    private final SharedPreferences mSharedPrefs;

    /**
     * Creates the user repository, with the required application reference.
     */
    public UserRepository(@NonNull OpenYoloDemoApplication application) {
        mUserDatabase = Room.databaseBuilder(application, UserDatabase.class, USER_DB_NAME)
                .build();
        mSharedPrefs = application.getSharedPreferences(
                AUTH_INFO_SHARED_PREFS,
                Context.MODE_PRIVATE);
    }

    @Override
    @Nullable
    public User getCurrentUser() {
        String currentUserEmail = getCurrentUserEmail();
        if (currentUserEmail == null) {
            return null;
        }

        return mUserDatabase.userDao().getUserByEmail(currentUserEmail);
    }

    @Override
    public boolean isExistingAccount(String email) {
        return mUserDatabase.userDao().getUserByEmail(email) != null;
    }

    @Override
    public boolean createPasswordAccount(
            @NonNull String email,
            @Nullable String name,
            @Nullable String profilePictureUri,
            @NonNull String password) {
        if (isExistingAccount(email)) {
            return false;
        }

        String salt = HashUtil.generateSalt();
        String hash = HashUtil.hashPassword(salt, password);

        mUserDatabase.userDao().createUser(new User(email, name, salt, hash, profilePictureUri));
        setCurrentUserEmail(email);
        return true;
    }

    @Override
    public boolean authWithPassword(@NonNull String email, @NonNull String password) {
        User user = mUserDatabase.userDao().getUserByEmail(email);
        if (user == null) {
            return false;
        }

        String salt = user.getPasswordSalt();
        String hash = HashUtil.hashPassword(salt, password);

        if (hash.equals(user.getPasswordHash())) {
            setCurrentUserEmail(email);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void signOut() {
        setCurrentUserEmail(null);
    }

    @Nullable
    private String getCurrentUserEmail() {
        return mSharedPrefs.getString(KEY_CURRENT_USER, null);
    }

    private void setCurrentUserEmail(@Nullable String email) {
        Editor editor = mSharedPrefs.edit();
        if (email == null) {
            editor.remove(KEY_CURRENT_USER);
        } else {
            editor.putString(KEY_CURRENT_USER, email);
        }
        editor.apply();
    }
}
