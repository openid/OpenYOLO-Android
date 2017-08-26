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

import android.content.Intent;
import android.support.annotation.AnyThread;

/**
 * Hides the details of activity transitions from {@link LoginViewModel}.
 */
@AnyThread
public interface LoginNavigator {

    /**
     * Requests the retrieval of a credential from an OpenYOLO provider using the supplied intent.
     */
    void startOpenYoloRetrieve(Intent retrieveIntent);

    /**
     * Requests the retrieval of a hint from an OpenYOLO provider using the supplied intent.
     */
    void startOpenYoloHint(Intent hintIntent);

    /**
     * Requests the storage of a credential to an OpenYOLO provider using the supplied intent.
     */
    void startSave(Intent saveIntent);

    /**
     * Transitions to the main activity.
     */
    void goToMain();
}
