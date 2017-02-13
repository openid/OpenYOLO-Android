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

package org.openyolo.spi;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.bbq.BaseBroadcastQueryReceiver;
import com.google.bbq.QueryResponseSender;
import com.google.bbq.proto.BroadcastQuery;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.openyolo.api.AuthenticationDomain;
import org.openyolo.api.RetrieveRequest;
import org.openyolo.proto.CredentialRetrieveRequest;

/**
 * Partial implementation of an OpenYOLO request receiver, that should be extended by providers.
 * This implementation handles some basic validation and decoding of the request before
 * handing the request to
 * {@link #processCredentialRequest(Context, BroadcastQuery, RetrieveRequest, Set)}.
 */
public abstract class BaseCredentialQueryReceiver extends BaseBroadcastQueryReceiver {

    /**
     * Creates a receiver that will log errors to the specified log tag.
     */
    public BaseCredentialQueryReceiver(String logTag) {
        super(logTag);
    }

    @Override
    protected void processQuery(@NonNull Context context, @NonNull BroadcastQuery query) {
        CredentialRetrieveRequest requestProto;
        try {
            requestProto = CredentialRetrieveRequest.ADAPTER.decode(query.queryMessage);
        } catch (NullPointerException | IOException ex) {
            Log.w(mLogTag, "Failed to parse credential request message", ex);
            new QueryResponseSender(context).sendResponse(query, null);
            return;
        }

        RetrieveRequest request;
        try {
            request = new RetrieveRequest.Builder(requestProto).build();
        } catch (NullPointerException | IllegalArgumentException ex) {
            // respond with no result
            Log.w(mLogTag, "Credential request message failed field validation", ex);
            new QueryResponseSender(context).sendResponse(query, null);
            return;
        }

        // TODO: validate claimed authentication domains
        Set<AuthenticationDomain> requestorDomains =
                new HashSet<>(AuthenticationDomain.listForPackage(context, query.requestingApp));

        processCredentialRequest(context, query, request, requestorDomains);
    }

    protected abstract void processCredentialRequest(
            @NonNull Context context,
            @NonNull BroadcastQuery query,
            @NonNull RetrieveRequest request,
            @NonNull Set<AuthenticationDomain> requestorDomains);
}
