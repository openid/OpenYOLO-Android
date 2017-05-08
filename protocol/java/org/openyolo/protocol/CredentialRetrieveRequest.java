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

import static java.util.Collections.EMPTY_SET;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.openyolo.protocol.internal.CustomMatchers.notNullOrEmptyString;
import static org.valid4j.Validation.validate;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openyolo.protocol.internal.AuthenticationMethodConverters;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.ClientVersionUtil;
import org.openyolo.protocol.internal.CollectionConverter;
import org.openyolo.protocol.internal.NoopValueConverter;

/**
 * A request for credentials, to be sent to credential providers on the device.
 */
public class CredentialRetrieveRequest implements Parcelable {

    /**
     * Parcelable reader for {@link CredentialRetrieveRequest} instances.
     *
     * @see android.os.Parcelable
     */
    public static final Creator<CredentialRetrieveRequest> CREATOR = new RetrieveRequestCreator();

    /**
     * Creates a {@link CredentialRetrieveRequest} from the given authentication methods.
     */
    @NonNull
    public static CredentialRetrieveRequest forAuthenticationMethods(
            @NonNull Set<AuthenticationMethod> authenticationMethods) {
        return new CredentialRetrieveRequest.Builder(authenticationMethods).build();
    }

    /**
     * Creates a {@link CredentialRetrieveRequest} from the given authentication methods.
     */
    @NonNull
    public static CredentialRetrieveRequest forAuthenticationMethods(
            @NonNull AuthenticationMethod... authenticationMethods) {
        return new CredentialRetrieveRequest.Builder(authenticationMethods).build();
    }

    /**
     * Creates a {@link CredentialRetrieveRequest} from its protocol buffer equivalent.
     */
    @NonNull
    public static CredentialRetrieveRequest fromProtobuf(
            @NonNull Protobufs.CredentialRetrieveRequest proto) {
        return new CredentialRetrieveRequest.Builder(proto).build();
    }

    /**
     * Creates a {@link CredentialRetrieveRequest} from its protocol buffer byte array equivalent.
     * @throws IOException if the request could not be parsed and validated.
     */
    @NonNull
    public static CredentialRetrieveRequest fromProtobufBytes(
            @NonNull byte[] protoBytes)
            throws IOException {
        return fromProtobuf(Protobufs.CredentialRetrieveRequest.parseFrom(protoBytes));
    }

    /**
     * Extracts a {@link CredentialRetrieveRequest} from the intent extra that is used to carry
     * a request from the client to the provider activity.
     *
     * @throws IOException if the intent does not contain a retrieve request, or the contained
     *     request could not be parsed and validated.
     */
    @NonNull
    public static CredentialRetrieveRequest fromRequestIntent(
            @NonNull Intent requestIntent)
            throws IOException {
        if (!requestIntent.hasExtra(ProtocolConstants.EXTRA_RETRIEVE_REQUEST)) {
            throw new IOException("credential retrieve request missing in intent data");
        }

        return fromProtobufBytes(
                requestIntent.getByteArrayExtra(ProtocolConstants.EXTRA_RETRIEVE_REQUEST));
    }

    @NonNull
    private final Set<AuthenticationMethod> mAuthMethods;

    @NonNull
    private final Map<String, byte[]> mAdditionalProps;

    private CredentialRetrieveRequest(
            @NonNull Set<AuthenticationMethod> authMethods,
            @NonNull Map<String, byte[]> additionalParams) {
        mAuthMethods = authMethods;
        mAdditionalProps = additionalParams;
    }

    /**
     * The set of authentication methods that the requestor supports. This is used to filter the set
     * of credentials saved by the provider. At least one authentication method must be specified.
     */
    @NonNull
    public Set<AuthenticationMethod> getAuthenticationMethods() {
        return mAuthMethods;
    }

    /**
     * The map of additional, non-standard properties included with this request.
     */
    @NonNull
    public Map<String, byte[]> getAdditionalProperties() {
        return mAdditionalProps;
    }

    /**
     * Retrieves the value of the named additional parameter, where the value is a UTF-8 encoded
     * string.
     */
    @Nullable
    public String getAdditionalPropertyAsString(@NonNull String key) {
        validate(key, notNullValue(), IllegalArgumentException.class);

        if (mAdditionalProps.containsKey(key)) {
            return new String(mAdditionalProps.get(key), Charset.forName("UTF-8"));
        }
        return null;
    }

    /**
     * Retrieves the raw, byte-array value of the named additional parameter.
     */
    @Nullable
    public byte[] getAdditionalProperty(@NonNull String key) {
        validate(key, notNullValue(), IllegalArgumentException.class);

        return mAdditionalProps.get(key);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        byte[] protoBytes = toProtocolBuffer().toByteArray();
        dest.writeInt(protoBytes.length);
        dest.writeByteArray(protoBytes);
    }

    /**
     * Converts the request into a protocol buffer, for storage or transmission.
     */
    public Protobufs.CredentialRetrieveRequest toProtocolBuffer() {
        return Protobufs.CredentialRetrieveRequest.newBuilder()
                .setClientVersion(ClientVersionUtil.getClientVersion())
                .addAllAuthMethods(
                        CollectionConverter.toList(
                                mAuthMethods,
                                AuthenticationMethodConverters.OBJECT_TO_PROTOBUF))
                .putAllAdditionalProps(
                        CollectionConverter.convertMapValues(
                                mAdditionalProps,
                                ByteStringConverters.BYTE_ARRAY_TO_BYTE_STRING))
                .build();
    }

    /**
     * Creates {@link CredentialRetrieveRequest} instances.
     */
    public static final class Builder {

        private Set<AuthenticationMethod> mAuthMethods = new HashSet<>();
        private Map<String, byte[]> mAdditionalParams = new HashMap<>();

        /**
         * Starts the process of creating a retrieve request using the data contained in the
         * provided protocol buffer representation.
         */
        public Builder(@NonNull Protobufs.CredentialRetrieveRequest requestProto) {
            validate(requestProto, notNullValue(), IllegalArgumentException.class);

            setAuthenticationMethods(CollectionConverter.toSet(
                    requestProto.getAuthMethodsList(),
                    AuthenticationMethodConverters.PROTOBUF_TO_OBJECT));
            setAdditionalProperties(
                    CollectionConverter.convertMapValues(
                            requestProto.getAdditionalPropsMap(),
                            ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY));
        }

        /**
         * Starts the process of describing a retrieve request. At least one authentication method
         * must be provided.
         */
        public Builder(@NonNull AuthenticationMethod... authMethods) {
            setAuthenticationMethods(
                    CollectionConverter.toSet(
                            authMethods,
                            NoopValueConverter.<AuthenticationMethod>getInstance()));
        }

        /**
         * Starts the process of describing a retrieve request. At least one authentication method
         * must be provided.
         */
        public Builder(@NonNull Set<AuthenticationMethod> authMethods) {
            setAuthenticationMethods(authMethods);
        }

        /**
         * Specifies the authentication methods supported by the requester. At least one
         * authentication method must be provided.
         */
        private Builder setAuthenticationMethods(@NonNull Set<AuthenticationMethod> authMethods) {
            validate(authMethods, notNullValue(), IllegalArgumentException.class);
            validate(authMethods, not(hasItem(nullValue())), IllegalArgumentException.class);
            validate(authMethods, not(equalTo(EMPTY_SET)), IllegalArgumentException.class);

            mAuthMethods = authMethods;
            return this;
        }

        /**
         * Specifies additional, non-standard retrieve request parameters. The provided map must be
         * non-null, and contain only non-null keys and values.
         */
        public Builder setAdditionalProperties(
                @NonNull Map<String, byte[]> additionalParams) {
            validate(additionalParams, notNullValue(), IllegalArgumentException.class);
            validate(
                    additionalParams.keySet(),
                    everyItem(notNullOrEmptyString()),
                    IllegalArgumentException.class);
            validate(
                    additionalParams.values(),
                    everyItem(notNullValue()),
                    IllegalArgumentException.class);

            mAdditionalParams = additionalParams;
            return this;
        }

        /**
         * Adds an additional parameter, where the value will be encoded as a UTF-8 string. Both the
         * parameter name and value must be non-null.
         *
         * @see CredentialRetrieveRequest#getAdditionalPropertyAsString(String)
         */
        public Builder addAdditionalProperty(
                @NonNull String name,
                @NonNull String value) {
            validate(value, notNullValue(), IllegalArgumentException.class);

            addAdditionalProperty(name, value.getBytes(Charset.forName("UTF-8")));
            return this;
        }

        /**
         * Adds an additional parameter. Both the parameter name and value must be non-null.
         */
        public Builder addAdditionalProperty(String name, byte[] value) {
            validate(name, notNullOrEmptyString(), IllegalArgumentException.class);
            validate(value, notNullValue(), IllegalArgumentException.class);

            mAdditionalParams.put(name, value);
            return this;
        }

        /**
         * Creates a {@link CredentialRetrieveRequest} using the properties set on the builder.
         */
        @NonNull
        public CredentialRetrieveRequest build() {
            return new CredentialRetrieveRequest(
                    unmodifiableSet(mAuthMethods),
                    unmodifiableMap(mAdditionalParams));
        }
    }

    private static final class RetrieveRequestCreator
            implements Creator<CredentialRetrieveRequest> {

        @Override
        public CredentialRetrieveRequest createFromParcel(Parcel in) {

            int protoLength = in.readInt();
            byte[] protoBytes = new byte[protoLength];
            in.readByteArray(protoBytes);

            try {
                return new CredentialRetrieveRequest.Builder(
                        Protobufs.CredentialRetrieveRequest.parseFrom(protoBytes))
                        .build();
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to read proto from parcel", ex);
            }
        }

        @Override
        public CredentialRetrieveRequest[] newArray(int size) {
            return new CredentialRetrieveRequest[size];
        }
    }
}
