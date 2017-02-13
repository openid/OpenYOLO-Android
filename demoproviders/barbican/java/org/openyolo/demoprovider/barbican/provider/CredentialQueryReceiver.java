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

package org.openyolo.demoprovider.barbican.provider;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.bbq.QueryResponseSender;
import com.google.bbq.proto.BroadcastQuery;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import okio.ByteString;
import org.openyolo.api.AuthenticationDomain;
import org.openyolo.api.RetrieveRequest;
import org.openyolo.api.internal.IntentUtil;
import org.openyolo.demoprovider.barbican.storage.CredentialStorage;
import org.openyolo.proto.CredentialRetrieveResponse;
import org.openyolo.spi.BaseCredentialQueryReceiver;

/**
 * Implements the OpenYOLO credential query receiver. This receiver is able to determine whether
 * a credential exists for the claimed set of authentication domains without unlocking the
 * password store.
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
            @NonNull final Context context,
            @NonNull final BroadcastQuery query,
            @NonNull RetrieveRequest request,
            @NonNull Set<AuthenticationDomain> requestorDomains) {

        final Context applicationContext = context.getApplicationContext();
        final QueryResponseSender responseSender = new QueryResponseSender(applicationContext);

        // NOTE: in a more complete implementation, this is where we would expand the set of
        // authentication domains listed in requestorDomains to all equivalent authentication
        // domains (such as affiliated apps and sites). This potentially larger list would then be
        // matched against the requested set of domains.

        final Set<AuthenticationDomain> matchingDomains = new HashSet<>();
        for (AuthenticationDomain domain : request.getAuthenticationDomains()) {
            if (requestorDomains.contains(domain)) {
                matchingDomains.add(domain);
            } else {
                Log.i(LOG_TAG, "Access to credentials in domain " + domain
                        + " denied to " + query.requestingApp);
            }
        }

        if (matchingDomains.isEmpty()) {
            Log.w(LOG_TAG, "Credential request from " + query.requestingApp
                    + " does not match any permitted domains");
            responseSender.sendResponse(query, null);
            return;
        }

        Log.i(LOG_TAG, "Processing query for " + query.requestingApp);

        CredentialStorage storage;
        try {
            storage = new CredentialStorage(context);
        }  catch (IOException ex) {
            Log.w(LOG_TAG, "Failed to open credential storage", ex);
            responseSender.sendResponse(query, null);
            return;
        }

        boolean credentialsFound = false;
        byte[] responseBytes = null;

        Iterator<AuthenticationDomain> authDomainIter = matchingDomains.iterator();
        while (!credentialsFound && authDomainIter.hasNext()) {
            credentialsFound = storage.hasCredentialFor(authDomainIter.next().toString());
        }

        Log.d(LOG_TAG, "Credentials found for " + query.requestingApp + ": " + credentialsFound);

        if (credentialsFound) {
            Intent retrieveIntent = RetrieveCredentialActivity.createIntent(context);
            CredentialRetrieveResponse response = new CredentialRetrieveResponse.Builder()
                    .retrieveIntent(ByteString.of(IntentUtil.toBytes(retrieveIntent)))
                    .build();
            responseBytes = response.encode();
        }

        responseSender.sendResponse(query, responseBytes);
    }
}
