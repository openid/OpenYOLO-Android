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

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A Room data access object for basic data manipulation operations on users.
 */
@Dao
public interface UserDao {

    /**
     * Creates a new user in the database.
     */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    void createUser(@NonNull User user);

    /**
     * Updates an existing user entry in the database.
     */
    @Update
    void updateUser(@NonNull User user);

    /**
     * Deletes a user from the database.
     */
    @Delete
    void deleteUser(@NonNull User user);

    /**
     * Finds a user by their email address.
     */
    @Nullable
    @Query("SELECT * FROM users WHERE email = :email")
    User getUserByEmail(@NonNull String email);
}
