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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A query response received from an eligible provider.
 */
public class QueryResponse implements Parcelable {

    /**
     * Parcelable creator for {@link QueryResponse} instances.
     * @see android.os.Parcelable
     */
    public static final Creator<QueryResponse> CREATOR = new QueryResponseCreator();

    /**
     * The package name of the responder.
     */
    @NonNull
    public final String responderPackage;

    /**
     * The unique response ID associated with this responder.
     */
    public final long responseId;

    /**
     * The data-type specific response message, if available. The semantics of the response message
     * are query data-type specific, however a general convention is that if the response message
     * is not specified, then the provider is unable to provide the requested data.
     */
    @Nullable
    public final byte[] responseMessage;

    /**
     * Creates a query response.
     */
    public QueryResponse(
            @NonNull String responderPackage,
            long responseId,
            @Nullable byte[] responseMessage) {
        require(responderPackage, notNullValue());
        this.responderPackage = responderPackage;
        this.responseId = responseId;
        this.responseMessage = responseMessage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(responderPackage);
        dest.writeLong(responseId);
        dest.writeInt(responseMessage != null ? 1 : 0);

        if (responseMessage != null) {
            dest.writeInt(responseMessage.length);
            dest.writeByteArray(responseMessage);
        }
    }

    private static final class QueryResponseCreator implements Creator<QueryResponse> {

        @Override
        public QueryResponse createFromParcel(Parcel source) {
            String responderPackage = source.readString();
            long responseId = source.readLong();
            byte[] responseMessage = null;
            if (source.readInt() != 0) {
                int responseMessageSize = source.readInt();
                responseMessage = new byte[responseMessageSize];
                source.readByteArray(responseMessage);
            }
            return new QueryResponse(responderPackage, responseId, responseMessage);
        }

        @Override
        public QueryResponse[] newArray(int size) {
            return new QueryResponse[size];
        }
    }
}
