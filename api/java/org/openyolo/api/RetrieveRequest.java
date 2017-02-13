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

package org.openyolo.api;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.openyolo.api.AuthenticationDomain.CONVERTER_DOMAIN_TO_STRING;
import static org.openyolo.api.AuthenticationDomain.CONVERTER_STRING_TO_DOMAIN;
import static org.openyolo.api.internal.CollectionConverter.toList;
import static org.openyolo.api.internal.CollectionConverter.toMap;
import static org.openyolo.api.internal.CollectionConverter.toSet;
import static org.openyolo.api.internal.CustomMatchers.isValidAuthenticationMethod;
import static org.openyolo.api.internal.CustomMatchers.notNullOrEmptyString;
import static org.openyolo.api.internal.KeyValuePairConverters.CONVERTER_ENTRY_TO_KVP;
import static org.openyolo.api.internal.KeyValuePairConverters.CONVERTER_KVP_TO_PAIR;
import static org.openyolo.api.internal.UriConverters.CONVERTER_STRING_TO_URI;
import static org.openyolo.api.internal.UriConverters.CONVERTER_URI_TO_STRING;
import static org.valid4j.Assertive.require;

import android.content.Context;
import android.net.Uri;
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
import org.openyolo.api.internal.NoopValueConverter;
import org.openyolo.proto.CredentialRetrieveRequest;

/**
 * A request for credentials, to be sent to credential providers on the device. A request specifies
 * at least one {@link AuthenticationDomain authentication domain} for which a credential is
 * required.
 */
public class RetrieveRequest implements Parcelable {

    /**
     * Parcelable reader for {@link RetrieveRequest} instances.
     * @see android.os.Parcelable
     */
    public static final Creator<RetrieveRequest> CREATOR = new RetrieveRequestCreator();

    /**
     * Creates a basic retrieve request for the current application.
     */
    public static RetrieveRequest forSelf(Context context) {
        return new RetrieveRequest.Builder(AuthenticationDomain.getSelfAuthDomain(context))
                .build();
    }

    @NonNull
    private final Set<AuthenticationDomain> mAuthDomains;

    @NonNull
    private final Set<Uri> mAuthMethods;

    @NonNull
    private final Map<String, byte[]> mAdditionalParams;

    private RetrieveRequest(
            @NonNull Set<AuthenticationDomain> authDomains,
            @NonNull Set<Uri> authMethods,
            @NonNull Map<String, byte[]> additionalParams) {
        mAuthDomains = authDomains;
        mAuthMethods = authMethods;
        mAdditionalParams = additionalParams;
    }

    /**
     * The set of authentication domains for which a credential is desired. It is the
     * responsibility of the credential provider to validate that the requester is permitted to
     * retrieve credentials for this set of domains, and how to interpret requests for domains
     * which are beyond the provably associated scope of the requester.
     */
    @NonNull
    public Set<AuthenticationDomain> getAuthenticationDomains() {
        return mAuthDomains;
    }

    /**
     * The set of authentication methods that the requestor supports. This is used to filter the
     * set of credentials saved by the provider. If no authentication methods are specified, then
     * no filtering of saved credentials will occur.
     */
    @NonNull
    public Set<Uri> getAuthenticationMethods() {
        return mAuthMethods;
    }

    /**
     * The map of additional, non-standard properties included with this request.
     */
    @NonNull
    public Map<String, byte[]> getAdditionalParameters() {
        return mAdditionalParams;
    }

    /**
     * Retrieves the value of the named additional parameter, where the value is a UTF-8
     * encoded string.
     */
    @Nullable
    public String getAdditionalParameterAsString(@NonNull String key) {
        require(key, notNullValue());
        if (mAdditionalParams.containsKey(key)) {
            return new String(mAdditionalParams.get(key), Charset.forName("UTF-8"));
        }
        return null;
    }

    /**
     * Retrieves the raw, byte-array value of the named additional parameter.
     */
    @Nullable
    public byte[] getAdditionalParameter(@NonNull String key) {
        require(key, notNullValue());
        return mAdditionalParams.get(key);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        byte[] protoBytes = toProtocolBuffer().encode();
        dest.writeInt(protoBytes.length);
        dest.writeByteArray(protoBytes);
    }

    /**
     * Converts the request into a protocol buffer, for storage or transmission.
     */
    public CredentialRetrieveRequest toProtocolBuffer() {
        return new CredentialRetrieveRequest.Builder()
                .authDomains(toList(mAuthDomains, CONVERTER_DOMAIN_TO_STRING))
                .authMethods(toList(mAuthMethods, CONVERTER_URI_TO_STRING))
                .additionalParams(toList(mAdditionalParams.entrySet(), CONVERTER_ENTRY_TO_KVP))
                .build();
    }

    /**
     * Creates {@link RetrieveRequest} instances.
     */
    public static final class Builder {

        private Set<AuthenticationDomain> mAuthDomains = new HashSet<>();
        private Set<Uri> mAuthMethods = new HashSet<>();
        private Map<String, byte[]> mAdditionalParams = new HashMap<>();

        /**
         * Starts the process of creating a retrieve request using the data contained in
         * the provided protocol buffer representation.
         */
        public Builder(@NonNull CredentialRetrieveRequest requestProto) {
            require(requestProto, notNullValue());
            setAuthenticationDomains(toSet(requestProto.authDomains, CONVERTER_STRING_TO_DOMAIN));
            setAuthenticationMethods(toSet(requestProto.authMethods, CONVERTER_STRING_TO_URI));
            setAdditionalParameters(toMap(requestProto.additionalParams, CONVERTER_KVP_TO_PAIR));
        }

        /**
         * Starts the process of describing a retrieve request, providing the set of authentication
         * domains for which a credential is desired. At least one authentication domain is
         * required.
         */
        public Builder(
                @NonNull AuthenticationDomain authDomain,
                @NonNull AuthenticationDomain... additionalAuthDomains) {
            setAuthenticationDomains(authDomain, additionalAuthDomains);
        }

        /**
         * Starts the process of describing a retrieve request, providing the set of authentication
         * domains (in the form of URI strings) for which a credential is desired. At least one
         * authentication domain is required.
         */
        public Builder(
                @NonNull String authDomain,
                @NonNull String... additionalAuthDomains) {
            setAuthenticationDomains(authDomain, additionalAuthDomains);
        }

        /**
         * Starts the process of describing a retrieve request, with the mandatory list
         * of authentication domains specified. The list must be non-null, with non-null entries.
         */
        public Builder(@NonNull Set<AuthenticationDomain> authDomains) {
            setAuthenticationDomains(authDomains);
        }

        /**
         * Specifies the authentication domain(s) for which a credential is desired.
         * At least one must be specified. All values specified must be non-null.
         */
        @NonNull
        public Builder setAuthenticationDomains(
                @NonNull AuthenticationDomain authDomain,
                @NonNull AuthenticationDomain... additionalAuthDomains) {
            setAuthenticationDomains(toSet(
                    authDomain,
                    additionalAuthDomains,
                    NoopValueConverter.<AuthenticationDomain>getInstance()));
            return this;
        }

        /**
         * Specifies the authentication domain(s) for which a credential is desired, in the
         * form of URI strings. At least one value must be specified. All values specified must be
         * non-null.
         */
        @NonNull
        public Builder setAuthenticationDomains(
                @NonNull String authDomain,
                @NonNull String... authDomainStrs) {
            setAuthenticationDomains(toSet(
                    authDomain,
                    authDomainStrs,
                    CONVERTER_STRING_TO_DOMAIN));
            return this;
        }

        /**
         * Specifies the authentication domains from which a credential is desired.
         * Must not be null. Must contain at least one value.
         */
        @NonNull
        public Builder setAuthenticationDomains(@NonNull Set<AuthenticationDomain> authDomains) {
            require(authDomains, notNullValue());
            require(!authDomains.isEmpty(), "at least one authentication domain must be specified");
            require(authDomains, everyItem(notNullValue()));
            mAuthDomains = authDomains;
            return this;
        }

        /**
         * Specifies the authentication methods supported by the requester. If no values are
         * specified, then any stored credential matching the set of specified authentication
         * domains may be returned. All provided values must be non-null.
         */
        @NonNull
        public Builder setAuthenticationMethods(@NonNull Uri... authMethods) {
            setAuthenticationMethods(
                    toSet(null, authMethods, NoopValueConverter.<Uri>getInstance()));
            return this;
        }

        /**
         * Specifies the authentication methods supported by the requester. If no values are
         * specified, then any stored credential matching the set of specified authentication
         * domains may be returned. The provided set must be non-null, and contain only
         * non-null values.
         */
        public Builder setAuthenticationMethods(@NonNull Set<Uri> authMethods) {
            require(authMethods, notNullValue());
            require(authMethods, everyItem(isValidAuthenticationMethod()));
            mAuthMethods = authMethods;
            return this;
        }

        /**
         * Specifies additional, non-standard retrieve request parameters. The provided map
         * must be non-null, and contain only non-null keys and values.
         */
        public Builder setAdditionalParameters(
                @NonNull Map<String, byte[]> additionalParams) {
            require(additionalParams, notNullValue());
            require(additionalParams.keySet(), everyItem(notNullOrEmptyString()));
            require(additionalParams.values(), everyItem(notNullValue()));
            mAdditionalParams = additionalParams;
            return this;
        }

        /**
         * Adds an additional parameter, where the value will be encoded as a UTF-8 string.
         * Both the parameter name and value must be non-null.
         * @see {@link RetrieveRequest#getAdditionalParameterAsString(String)}
         */
        public Builder addAdditionalParameter(
                @NonNull String name,
                @NonNull String value) {
            require(value, notNullValue());
            addAdditionalParameter(name, value.getBytes(Charset.forName("UTF-8")));
            return this;
        }

        /**
         * Adds an additional parameter. Both the parameter name and value must be non-null.
         */
        public Builder addAdditionalParameter(String name, byte[] value) {
            require(name, notNullOrEmptyString());
            require(value, notNullValue());
            mAdditionalParams.put(name, value);
            return this;
        }

        /**
         * Creates a {@link RetrieveRequest} using the properties set on the builder.
         */
        @NonNull
        public RetrieveRequest build() {
            return new RetrieveRequest(
                    unmodifiableSet(mAuthDomains),
                    unmodifiableSet(mAuthMethods),
                    unmodifiableMap(mAdditionalParams));
        }
    }

    private static final class RetrieveRequestCreator implements Creator<RetrieveRequest> {
        @Override
        public RetrieveRequest createFromParcel(Parcel in) {

            int protoLength = in.readInt();
            byte[] protoBytes = new byte[protoLength];
            in.readByteArray(protoBytes);

            try {
                return new RetrieveRequest.Builder(
                        CredentialRetrieveRequest.ADAPTER.decode(protoBytes))
                        .build();
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to read proto from parcel", ex);
            }
        }

        @Override
        public RetrieveRequest[] newArray(int size) {
            return new RetrieveRequest[size];
        }
    }
}
