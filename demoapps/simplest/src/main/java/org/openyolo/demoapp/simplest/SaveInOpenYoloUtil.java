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
package org.openyolo.demoapp.simplest;

import android.app.Activity;
import android.content.Intent;
import android.util.Patterns;

import org.openyolo.api.CredentialClient;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.AuthenticationMethod;
import org.openyolo.protocol.AuthenticationMethods;
import org.openyolo.protocol.Credential;

class SaveInOpenYoloUtil {

    private static final int RC_SAVE_CREDENTIAL = 754;

    void save(Activity activity, String identifier, String password) {
        // Craft the valid credential.
        AuthenticationMethod authMethod = isEmail(identifier) ?
                AuthenticationMethods.EMAIL :  AuthenticationMethods.USER_NAME;
        AuthenticationDomain authDomain = AuthenticationDomain.getSelfAuthDomain(activity);
        Credential credential = new Credential.Builder(identifier, authMethod, authDomain)
                .setPassword(password)
                .build();

        // Send the request. It is safe to ignore the result.
        CredentialClient client = CredentialClient.getInstance(activity);
        Intent saveCredentialIntent = client.getSaveIntent(credential);
        activity.startActivityForResult(saveCredentialIntent, RC_SAVE_CREDENTIAL);
    }

    private boolean isEmail(String identifier) {
        return Patterns.EMAIL_ADDRESS.matcher(identifier).find();
    }
}
