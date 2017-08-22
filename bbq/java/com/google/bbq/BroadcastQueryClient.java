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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.google.bbq.Protobufs.BroadcastQuery;
import com.google.bbq.Protobufs.BroadcastQueryResponse;
import com.google.bbq.internal.ClientVersionUtil;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Dispatches broadcast queries to available data providers.
 */
public class BroadcastQueryClient {

    /**
     * The default amount of time that this client will wait for responses from providers, before
     * ignoring them.
     */
    public static final long DEFAULT_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(2);

    private static final String LOG_TAG = "BroadcastQueryClient";

    private static final AtomicReference<BroadcastQueryClient> INSTANCE =
            new AtomicReference<>();

    @NonNull
    private final Context mContext;

    @NonNull
    private final SecureRandom mSecureRandom;

    @NonNull
    private final ConcurrentHashMap<Long, PendingQuery> mPendingQueries;

    @NonNull
    private final ScheduledExecutorService mExecutorService;

    @NonNull
    private final AtomicBoolean mDisposed;

    /**
     * Retrieves the global instance of the broadcast query client for the application
     * associated to the provided context.
     */
    @NonNull
    public static BroadcastQueryClient getInstance(Context context) {
        Context applicationContext = context.getApplicationContext();
        BroadcastQueryClient client = new BroadcastQueryClient(applicationContext);
        if (!INSTANCE.compareAndSet(null, client)) {
            client.dispose();
            client = INSTANCE.get();
        }

        return client;
    }

    BroadcastQueryClient(@NonNull Context context) {
        mContext = context;
        mSecureRandom = new SecureRandom();
        mPendingQueries = new ConcurrentHashMap<>();
        mExecutorService = Executors.newSingleThreadScheduledExecutor();
        mDisposed = new AtomicBoolean(false);
    }

    /**
     * Dispatches a query for the specified data type, carrying the specified protocol buffer
     * message (if required). The response to this query will be provided to the specified callback.
     * A {@link #DEFAULT_TIMEOUT_MS default timeout} will be used.
     */
    public void queryFor(
            @NonNull String dataType,
            @Nullable MessageLite queryMessage,
            @NonNull QueryCallback callback) {
        queryFor(dataType,
                queryMessage,
                DEFAULT_TIMEOUT_MS,
                callback);
    }

    /**
     * Dispatches a query for the specified data type, carrying the specified protocol buffer
     * message (if required). The response to this query will be provided to the specified callback.
     */
    public void queryFor(
            @NonNull String dataType,
            @Nullable MessageLite queryMessage,
            long timeoutInMs,
            @NonNull QueryCallback callback) {
        queryFor(dataType,
                queryMessage != null ? queryMessage.toByteArray() : null,
                timeoutInMs,
                callback);
    }

    /**
     * Dispatches a query for the specified data type, carrying the specified message (if required).
     * The response to this query will be provided to the specified callback.
     */
    public void queryFor(
            @NonNull String dataType,
            @Nullable byte[] queryMessage,
            long timeoutInMs,
            @NonNull QueryCallback callback) {
        require(!TextUtils.isEmpty(dataType), "dataType must not be null or empty");
        require(timeoutInMs > 0, "Timeout must be greater than zero");
        require(callback, notNullValue());
        require(!isDisposed(), "BroadcastQueryClient has been disposed");

        PendingQuery pq = new PendingQuery(
                dataType,
                queryMessage,
                timeoutInMs,
                callback);

        long queryId;
        do {
            queryId = mSecureRandom.nextLong();
        } while (mPendingQueries.putIfAbsent(queryId, pq) != null);
        pq.dispatch(queryId);
    }

    /**
     * Disposes all leakable resources associated with this client.
     */
    private void dispose() {
        if (!mDisposed.compareAndSet(false, true)) {
            return;
        }

        mExecutorService.shutdownNow();
        for (PendingQuery pq : mPendingQueries.values()) {
            mContext.unregisterReceiver(pq.mResponseReceiver);
        }
    }

    /**
     * Determines whether this client has been disposed, and therefore should no longer be used.
     */
    private boolean isDisposed() {
        return mDisposed.get();
    }

    private Intent createQueryIntent(
            PendingQuery pendingQuery,
            String responderPackage,
            long responseId) {
        Intent queryIntent = QueryUtil.createEmptyQueryIntent(pendingQuery.mDataType);
        queryIntent.setPackage(responderPackage);
        queryIntent.putExtra(QueryUtil.EXTRA_QUERY_MESSAGE,
                BroadcastQuery.newBuilder()
                        .setClientVersion(ClientVersionUtil.getClientVersion())
                        .setRequestingApp(mContext.getPackageName())
                        .setDataType(pendingQuery.mDataType)
                        .setRequestId(pendingQuery.mQueryId)
                        .setResponseId(responseId)
                        .setQueryMessage(pendingQuery.mQueryMessage != null
                                ? ByteString.copyFrom(pendingQuery.mQueryMessage)
                                : null)
                        .build()
                        .toByteArray());
        return queryIntent;
    }

    private final class PendingQuery {
        final String mDataType;
        final byte[] mQueryMessage;
        final Map<Long, String> mRespondersById;
        final CopyOnWriteArraySet<Long> mPendingResponses;
        final ConcurrentHashMap<String, QueryResponse> mResponses;
        final QueryCallback mQueryCallback;
        final long mTimeoutInMs;

        long mQueryId;
        ScheduledFuture<Void> mTimeoutFuture;
        BroadcastReceiver mResponseReceiver;

        PendingQuery(
                String dataType,
                byte[] queryMessage,
                long timeoutInMs,
                QueryCallback queryCallback) {
            mDataType = dataType;
            mQueryMessage = queryMessage;
            mTimeoutInMs = timeoutInMs;

            mRespondersById = buildRespondersById();
            mPendingResponses = new CopyOnWriteArraySet<>();
            for (long responderId : mRespondersById.keySet()) {
                mPendingResponses.add(responderId);
            }
            mResponses = new ConcurrentHashMap<>();
            mQueryCallback = queryCallback;
        }

        Map<Long, String> buildRespondersById() {
            Set<String> responders = QueryUtil.getRespondersForDataType(mContext, mDataType);
            HashMap<Long, String> tempRespondersById = new HashMap<>();
            for (String responderPackage : responders) {
                long responderId;
                do {
                    responderId = mSecureRandom.nextLong();
                } while (tempRespondersById.containsKey(responderId));

                tempRespondersById.put(responderId, responderPackage);
            }
            return tempRespondersById;
        }

        void dispatch(long queryId) {
            mQueryId = queryId;

            if (mRespondersById.isEmpty()) {
                complete();
                return;
            }

            mResponseReceiver = new ResponseHandler(this);
            mContext.registerReceiver(mResponseReceiver, getResponseFilter());

            for (Map.Entry<Long, String> responderEntry : mRespondersById.entrySet()) {
                long responseId = responderEntry.getKey();
                String responderPackage = responderEntry.getValue();
                mContext.sendBroadcast(createQueryIntent(this, responderPackage, responseId));
            }

            mTimeoutFuture = mExecutorService.schedule(
                    new QueryTimeoutHandler(this),
                    mTimeoutInMs,
                    TimeUnit.MILLISECONDS);
        }

        void complete() {
            if (!mPendingQueries.remove(mQueryId, this)) {
                // response already delivered
                return;
            }

            if (mTimeoutFuture != null) {
                mTimeoutFuture.cancel(false);
            }

            if (mResponseReceiver != null) {
                mContext.unregisterReceiver(mResponseReceiver);
            }

            mQueryCallback.onResponse(mQueryId, new ArrayList<>(mResponses.values()));
        }

        IntentFilter getResponseFilter() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(QueryUtil.createResponseAction(mDataType, mQueryId));
            filter.addCategory(QueryUtil.BBQ_CATEGORY);
            return filter;
        }
    }

    /**
     * Forcibly completes a pending query when a timeout is reached.
     */
    private final class QueryTimeoutHandler implements Callable<Void> {

        final PendingQuery mPendingQuery;

        QueryTimeoutHandler(PendingQuery pendingQuery) {
            mPendingQuery = pendingQuery;
        }

        @Override
        public Void call() throws Exception {
            mPendingQuery.complete();
            return null;
        }
    }

    /**
     * Captures broadcast responses for queries.
     */
    private final class ResponseHandler extends BroadcastReceiver {

        final PendingQuery mPendingQuery;

        ResponseHandler(PendingQuery pendingQuery) {
            mPendingQuery = pendingQuery;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] responseBytes = intent.getByteArrayExtra(QueryUtil.EXTRA_RESPONSE_MESSAGE);
            if (responseBytes == null) {
                Log.w(LOG_TAG, "Received query response without a defined message");
                return;
            }

            BroadcastQueryResponse response;
            try {
                response = BroadcastQueryResponse.parseFrom(responseBytes);
            } catch (IOException e) {
                Log.w(LOG_TAG, "Unable to parse query response message");
                return;
            }

            String responder = mPendingQuery.mRespondersById.get(response.getResponseId());
            if (responder == null) {
                Log.w(LOG_TAG, "Received response from unknown responder");
                return;
            }

            if (!mPendingQuery.mPendingResponses.remove(response.getResponseId())) {
                Log.w(LOG_TAG, "Duplicate response received; ignoring");
                return;
            }

            if (response.getResponseMessage() != null) {
                QueryResponse queryResponse = new QueryResponse(
                        responder,
                        response.getResponseId(),
                        response.getResponseMessage().toByteArray());
                mPendingQuery.mResponses.put(responder, queryResponse);
            }

            if (mPendingQuery.mPendingResponses.isEmpty()) {
                mPendingQuery.complete();
            }
        }
    }
}
