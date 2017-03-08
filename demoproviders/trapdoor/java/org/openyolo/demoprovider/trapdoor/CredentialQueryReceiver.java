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

package org.openyolo.demoprovider.trapdoor;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.bbq.QueryResponseSender;
import com.google.bbq.proto.BroadcastQuery;
import java.util.Set;
import okio.ByteString;
import org.openyolo.api.AuthenticationDomain;
import org.openyolo.api.AuthenticationMethods;
import org.openyolo.api.RetrieveRequest;
import org.openyolo.api.internal.IntentUtil;
import org.openyolo.proto.CredentialRetrieveResponse;
import org.openyolo.spi.BaseCredentialQueryReceiver;

/**
 * Handles OpenYOLO credential retrieve broadcasts. As trapdoor does not store any credentials,
 * we respond to all requests with an intent as long as the
 * {@link org.openyolo.api.AuthenticationMethods#ID_AND_PASSWORD id and password} authentication
 * method is supported.
 */
public class CredentialQueryReceiver extends BaseCredentialQueryReceiver {

    private static final String LOG_TAG = "CredentialQueryReceiver";

    /**
     * Creates the query receiver.
     */
    public CredentialQueryReceiver() {
        super(LOG_TAG);
    }

    @Override
    protected void processCredentialRequest(
            @NonNull Context context,
            @NonNull BroadcastQuery query,
            @NonNull RetrieveRequest request,
            @NonNull Set<AuthenticationDomain> requestorDomains) {

        Log.i(LOG_TAG, "Processing retrieve query from claimed caller: " + query.requestingApp);

        final Context applicationContext = context.getApplicationContext();
        final QueryResponseSender responseSender = new QueryResponseSender(applicationContext);

        // Ensure the caller supports the Id and Password authentication method
        if (!request.getAuthenticationMethods().contains(AuthenticationMethods.ID_AND_PASSWORD)) {
            responseSender.sendResponse(query,  null /* response */);
            return;
        }

        Intent retrieveIntent = RetrieveActivity.createIntent(applicationContext);
        CredentialRetrieveResponse response = new CredentialRetrieveResponse.Builder()
                .retrieveIntent(ByteString.of(IntentUtil.toBytes(retrieveIntent)))
                .build();

        Log.i(LOG_TAG, "Accepting credential request for claimed caller: " + query.requestingApp);
        responseSender.sendResponse(query, response.encode());
    }
}
