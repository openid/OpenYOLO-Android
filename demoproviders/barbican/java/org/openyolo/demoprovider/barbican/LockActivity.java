/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
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

package org.openyolo.demoprovider.barbican;

import android.app.Activity;
import android.os.Bundle;
import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient;

/**
 * Locks the credential store in response to an action from the persistent "unlocked" notification.
 */
public class LockActivity extends Activity implements CredentialStorageClient.ConnectedCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CredentialStorageClient.connect(this, this);
    }

    @Override
    public void onStorageConnected(CredentialStorageClient client) {
        client.lock();
        finish();
        client.disconnect(this);
    }
}
