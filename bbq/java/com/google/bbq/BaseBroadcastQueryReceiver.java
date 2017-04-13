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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.bbq.Protobufs.BroadcastQuery;
import java.io.IOException;

/**
 * Partial BBQ query receiver implementation, that should be extended by providers.
 * This implementation handles some basic validation and decoding of the request before
 * handing the request to {@link #processQuery(Context, BroadcastQuery)}.
 */
public abstract class BaseBroadcastQueryReceiver extends BroadcastReceiver {

    protected final String mLogTag;

    /**
     * Creates a query receiver, using the specified log tag for warning messages in parsing
     * received requests.
     */
    public BaseBroadcastQueryReceiver(String logTag) {
        mLogTag = logTag;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        byte[] queryBytes = intent.getByteArrayExtra(QueryUtil.EXTRA_QUERY_MESSAGE);
        if (queryBytes == null) {
            Log.w(mLogTag, "Received message without query data");
            return;
        }

        final BroadcastQuery query;
        try {
            query = BroadcastQuery.parseFrom(queryBytes);
        } catch (IOException e) {
            Log.w(mLogTag, "Unable to decode query data");
            return;
        }

        try {
            context.getPackageManager().getPackageInfo(query.getRequestingApp(), 0);
        } catch (PackageManager.NameNotFoundException ex) {
            Log.w(mLogTag, "Received query from non-existent app: " + query.getRequestingApp());
            return;
        }

        processQuery(context, query);
    }

    protected abstract void processQuery(@NonNull Context context, @NonNull BroadcastQuery query);
}
