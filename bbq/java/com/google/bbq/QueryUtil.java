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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility methods related to query message construction and dispatch.
 */
public final class QueryUtil {

    /**
     * The category attached to all broadcast intents sent as part of the BBQ protocol.
     */
    public static final String BBQ_CATEGORY = "com.google.bbq.QUERY";

    /**
     * The extra field used to carry query messages in broadcast intents.
     */
    public static final String EXTRA_QUERY_MESSAGE = "com.google.bbq.message";

    /**
     * The extra field used to carry query response messages in broadcast intents.
     */
    public static final String EXTRA_RESPONSE_MESSAGE = "com.google.bbq.response";

    private static final int HALF_BYTE_WIDTH = 4;
    private static final int HALF_BYTE_MASK = 0xF;

    private static final char[] HEX_DIGITS =
            { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };


    private QueryUtil() {}

    /**
     * Creates the basic structure of an intent used to dispatch a query.
     */
    public static Intent createEmptyQueryIntent(@NonNull String dataType) {
        require(!TextUtils.isEmpty(dataType), "dataType must not be null or empty");
        Intent queryIntent = new Intent(dataType);
        queryIntent.addCategory(QueryUtil.BBQ_CATEGORY);
        return queryIntent;
    }

    /**
     * Creates the {@code ACTION} string that is used for registering query response broadcast
     * receivers, based on the data type of the query and the unique request ID.
     */
    public static String createResponseAction(@NonNull String dataType, long requestId) {
        require(dataType, notNullValue());
        return dataType + ":" + longAsHex(requestId);
    }

    /**
     * Determines the set of providers who may be able to provide data of the specified type,
     * based on their declared broadcast query receivers.
     */
    public static Set<String> getRespondersForDataType(@NonNull Context context,
                                                       @NonNull String dataType) {
        require(context, notNullValue());
        require(dataType, notNullValue());

        PackageManager packageManager = context.getPackageManager();

        Intent intent = new Intent(dataType);
        intent.addCategory(QueryUtil.BBQ_CATEGORY);
        List<ResolveInfo> responderInfos =
                packageManager.queryBroadcastReceivers(intent, PackageManager.GET_RESOLVED_FILTER);

        HashSet<String> responders = new HashSet<>();
        for (ResolveInfo info : responderInfos) {
            responders.add(info.activityInfo.packageName);
        }

        return responders;
    }

    @VisibleForTesting
    static String longAsHex(long val) {
        char[] result = new char[Long.SIZE / Byte.SIZE * 2];
        int index = result.length - 1;
        while (index >= 0) {
            result[index--] = HEX_DIGITS[(int)(val & HALF_BYTE_MASK)];
            val >>>= HALF_BYTE_WIDTH;
            result[index--] = HEX_DIGITS[(int)(val & HALF_BYTE_MASK)];
            val >>>= HALF_BYTE_WIDTH;
        }

        return new String(result);
    }
}
