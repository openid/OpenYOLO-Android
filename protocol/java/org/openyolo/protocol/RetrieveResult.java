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

package org.openyolo.protocol;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.valid4j.Assertive.require;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.openyolo.protocol.Protobufs.CredentialRetrieveBbqResponse;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.CollectionConverter;
import org.openyolo.protocol.internal.IntentUtil;

/**
 * A response to a {@link RetrieveRequest credential retrieve request}.
 */
public class RetrieveResult implements Parcelable {

    /**
     * Parcelable reader for {@link RetrieveResult} instances.
     * @see android.os.Parcelable
     */
    public static final Creator<RetrieveResult> CREATOR = new RetrieveResultCreator();

    @Nullable
    private final Intent mRetrieveIntent;

    @NonNull
    private final Map<String, CredentialRetrieveBbqResponse> mResponses;

    private RetrieveResult(
            @Nullable Intent retrieveIntent,
            @NonNull Map<String, CredentialRetrieveBbqResponse> responses) {
        mRetrieveIntent = retrieveIntent;
        mResponses = responses;
    }

    /**
     * An intent that can be used to retrieve a credential for the calling app. If no credentials
     * are available, this will be {@code null}. User input may or may not be required to retrieve
     * the credential, at the discretion of the credential provider. The retrieve intent
     * should be fired when the app is ready to retrieve the credential - typically immediately
     * after the retrieve result is received.
     */
    @Nullable
    public Intent getRetrieveIntent() {
        return mRetrieveIntent;
    }

    /**
     * The package names of responding credential providers that indicated they may have a usable
     * credential.
     */
    @NonNull
    public Set<String> getResponderPackageNames() {
        return mResponses.keySet();
    }

    /**
     * Retrieves the intent for the specified responder package name, if available.
     */
    @NonNull
    public Intent getRetrieveIntentForResponder(@NonNull String responderPackageName) {
        require(responderPackageName, notNullValue());
        CredentialRetrieveBbqResponse response = mResponses.get(responderPackageName);
        if (response == null) {
            throw new IllegalArgumentException(responderPackageName + " is not a responder");
        }

        return IntentUtil.fromBytes(response.getRetrieveIntent().toByteArray());
    }

    /**
     * Retrieves the additional, non-standard response parameters for the specified responder
     * package name, if available.
     */
    @NonNull
    public Map<String, byte[]> getAdditionalPropsForResponder(String responderPackageName) {
        CredentialRetrieveBbqResponse response = mResponses.get(responderPackageName);
        if (response == null) {
            throw new IllegalArgumentException(responderPackageName + " is not a responder");
        }

        return CollectionConverter.convertMapValues(
                response.getAdditionalPropsMap(),
            ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY);
    }

    /**
     * Creates {@link RetrieveResult} instances.
     */
    public static final class Builder {

        @Nullable
        private Intent mIntent;

        @NonNull
        private Map<String, CredentialRetrieveBbqResponse> mProtoResponses;

        /**
         * Starts the process of describing a credential retrieve result.
         */
        public Builder() {
            mIntent = null;
            mProtoResponses = Collections.emptyMap();
        }

        /**
         * Specifies the map of protocol buffer responses received for the credential request,
         * keyed by the package name of the responder.
         */
        @NonNull
        public Builder setProtoResponses(
                @NonNull Map<String, CredentialRetrieveBbqResponse> protoResponses) {
            require(protoResponses, notNullValue());
            for (CredentialRetrieveBbqResponse value : protoResponses.values()) {
                require(value, notNullValue());
            }
            mProtoResponses = protoResponses;
            return this;
        }

        /**
         * Specifies the retrieve intent, that can be used to retrieve existing credentials from
         * the provider, if available.
         */
        @NonNull
        public Builder setRetrieveIntent(@Nullable Intent intent) {
            mIntent = intent;
            return this;
        }

        /**
         * Creates a {@link RetrieveResult} with the properties described to the builder.
         */
        @NonNull
        public RetrieveResult build() {
            return new RetrieveResult(mIntent, mProtoResponses);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mRetrieveIntent, 0);
        dest.writeInt(mResponses.size());

        for (String key : mResponses.keySet()) {
            byte[] responseBytes = mResponses.get(key).toByteArray();
            dest.writeString(key);
            dest.writeInt(responseBytes.length);
            dest.writeByteArray(responseBytes);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    static final class RetrieveResultCreator implements Creator<RetrieveResult> {
        @Override
        public RetrieveResult createFromParcel(Parcel in) {
            Intent retrieveIntent = in.readParcelable(Intent.class.getClassLoader());
            int numResponses = in.readInt();

            Map<String, CredentialRetrieveBbqResponse> responses = new HashMap<>(numResponses);
            for (int i = 0; i < numResponses; i++) {
                String key = in.readString();
                int responseSize = in.readInt();
                byte[] responseBytes = new byte[responseSize];
                in.readByteArray(responseBytes);

                CredentialRetrieveBbqResponse response;
                try {
                    response = CredentialRetrieveBbqResponse.parseFrom(responseBytes);
                } catch (InvalidProtocolBufferException ex) {
                    throw new IllegalArgumentException("Unable to parse response proto");
                }

                responses.put(key, response);
            }

            return new RetrieveResult.Builder()
                    .setRetrieveIntent(retrieveIntent)
                    .setProtoResponses(responses)
                    .build();
        }

        @Override
        public RetrieveResult[] newArray(int size) {
            return new RetrieveResult[size];
        }
    }

}
