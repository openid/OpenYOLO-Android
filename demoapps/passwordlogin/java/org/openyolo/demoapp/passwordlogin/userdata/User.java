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

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A simple model of a user, with information to attempt authentication.
 */
@Entity(tableName = "users", indices = {@Index(value = "email", unique = true)})
public final class User {

    @PrimaryKey
    @ColumnInfo(name = "email")
    private final String mEmail;

    @ColumnInfo(name = "first_name")
    private final String mName;

    @ColumnInfo(name = "passwordSalt")
    private final String mPasswordSalt;

    @ColumnInfo(name = "passwordHash")
    private final String mPasswordHash;

    @ColumnInfo(name = "profile_picture_uri")
    private final String mProfilePictureUri;

    /**
     * Creates a user from their core attributes.
     */
    public User(
            @NonNull String email,
            @Nullable String name,
            @NonNull String passwordSalt,
            @NonNull String passwordHash,
            @Nullable String profilePictureUri) {
        mName = name;
        mEmail = email;
        mPasswordSalt = passwordSalt;
        mPasswordHash = passwordHash;
        mProfilePictureUri = profilePictureUri;
    }

    @NonNull
    public String getEmail() {
        return mEmail;
    }

    @Nullable
    public String getName() {
        return mName;
    }

    @NonNull
    public String getPasswordSalt() {
        return mPasswordSalt;
    }

    @NonNull
    public String getPasswordHash() {
        return mPasswordHash;
    }

    @Nullable
    public String getProfilePictureUri() {
        return mProfilePictureUri;
    }
}
