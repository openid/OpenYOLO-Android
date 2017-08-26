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
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.openyolo.demoapp.passwordlogin.userdata.UserRepository;

/**
 * Application instance that holds global state. This is essentially a service locator, to avoid
 * the complexity of adding dependency injection to the demo.
 */
public final class OpenYoloDemoApplication extends Application {

    private final ScheduledExecutorService mExecutorService;
    private UserRepository mUserRepository;

    /**
     * Convenience method for converting a raw Application instance to the appropriate type.
     */
    @NonNull
    public static OpenYoloDemoApplication getInstance(@NonNull Application application) {
        return (OpenYoloDemoApplication) application;
    }

    /**
     * Creates and initializes the necessary global components of the application.
     */
    public OpenYoloDemoApplication() {
        mExecutorService = Executors.newScheduledThreadPool(1);
    }

    /**
     * An application-wide shared executor for background processing.
     */
    @AnyThread
    public ScheduledExecutorService getExecutor() {
        return mExecutorService;
    }

    /**
     * An interface to user account state and authentication related operations.
     */
    @WorkerThread
    public synchronized UserRepository getUserRepository() {
        if (mUserRepository == null) {
            mUserRepository = new UserRepository(this);
        }
        return mUserRepository;
    }
}
