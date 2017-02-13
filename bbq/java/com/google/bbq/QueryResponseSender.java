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

package com.google.bbq;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.valid4j.Assertive.require;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.google.bbq.proto.BroadcastQuery;
import com.google.bbq.proto.BroadcastQueryResponse;
import okio.ByteString;


/**
 * A utility for data providers that encodes and sends query responses.
 */
public class QueryResponseSender {

    private static final String LOG_TAG = "QueryResponseSender";

    private final Context mContext;

    /**
     * Creates a response sender.
     */
    public QueryResponseSender(@NonNull Context context) {
        mContext = require(context, notNullValue());
    }

    /**
     * Dispatches a query response message for the specified query.
     */
    public void sendResponse(
            @NonNull BroadcastQuery query,
            @Nullable byte[] responseMessage) {
        require(query, notNullValue());

        BroadcastQueryResponse.Builder responseBuilder = createResponseBuilder(query);
        if (responseMessage != null) {
            responseBuilder.responseMessage = ByteString.of(responseMessage);
        }

        Intent responseBroadcast = getIntentForQuery(query, responseBuilder);

        mContext.sendBroadcast(responseBroadcast);
    }

    @VisibleForTesting
    @NonNull
    Intent getIntentForQuery(@NonNull BroadcastQuery query,
                             BroadcastQueryResponse.Builder responseBuilder) {
        Intent responseBroadcast =
                new Intent(QueryUtil.createResponseAction(query.dataType, query.requestId));
        responseBroadcast.addCategory(QueryUtil.BBQ_CATEGORY);
        responseBroadcast.setPackage(query.requestingApp);
        responseBroadcast.putExtra(
                QueryUtil.EXTRA_RESPONSE_MESSAGE,
                responseBuilder.build().encode());
        return responseBroadcast;
    }

    private BroadcastQueryResponse.Builder createResponseBuilder(BroadcastQuery query) {
        return new BroadcastQueryResponse.Builder()
                .requestId(query.requestId)
                .responseId(query.responseId);
    }
}
