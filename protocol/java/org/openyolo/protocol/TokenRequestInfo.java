/*
 * Copyright 2017 The OpenYOLO Authors. All Rights Reserved.
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

import static org.hamcrest.core.IsNull.notNullValue;
import static org.openyolo.protocol.internal.AdditionalPropertiesUtil.validateAdditionalProperties;
import static org.openyolo.protocol.internal.AdditionalPropertiesUtil.validateAdditionalPropertiesFromProto;
import static org.openyolo.protocol.internal.StringUtil.nullifyEmptyString;
import static org.valid4j.Validation.validate;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.CollectionConverter;

/**
 * Optional parameters for an token provider, used for requesting an ID token.
 */
public final class TokenRequestInfo {

    /**
     * A default token request info message, containing no defined properties.
     */
    public static final TokenRequestInfo DEFAULT =
            new TokenRequestInfo.Builder().build();

    /**
     * Creates a {@link TokenRequestInfo} from its protocol buffer equivalent.
     * @throws MalformedDataException if the protocol buffer could not be parsed or validated.
     */
    public static TokenRequestInfo fromProtobuf(@NonNull Protobufs.TokenRequestInfo proto)
            throws MalformedDataException {
        validate(proto, notNullValue(), IllegalArgumentException.class);

        return new TokenRequestInfo.Builder(proto).build();
    }

    /**
     * Creates a {@link TokenRequestInfo} from its protocol buffer byte array equivalent.
     * @throws MalformedDataException if the protocol buffer could not be parsed or validated.
     */
    public static TokenRequestInfo fromProtobufBytes(@NonNull byte[] protoBytes)
            throws MalformedDataException {
        validate(protoBytes, notNullValue(), IllegalArgumentException.class);

        try {
            return fromProtobuf(Protobufs.TokenRequestInfo.parseFrom(protoBytes));
        } catch (IOException ex) {
            throw new MalformedDataException("Unable to parse token request info proto", ex);
        }
    }

    @Nullable
    private final String mClientId;

    @Nullable
    private final String mNonce;

    @NonNull
    private final Map<String, ByteString> mAdditionalProps;

    private TokenRequestInfo(Builder builder) {
        mClientId = builder.mClientId;
        mNonce = builder.mNonce;
        mAdditionalProps = Collections.unmodifiableMap(builder.mAdditionalProps);
    }

    /**
     * An optional client ID, used to identify the current application for the purposes of
     * ID token retrieval and generation. Where accepted by the token provider, this should
     * be used as the audience ("aud" claim) in returned ID token.
     */
    @Nullable
    public String getClientId() {
        return mClientId;
    }

    /**
     * An optional nonce, used to protect against ID token replay attacks. Where accepted
     * by the token provider, this should appear as the nonce claim in the ID token.
     */
    @Nullable
    public String getNonce() {
        return mNonce;
    }

    /**
     * A map of additional, non-standard properties for transmission to the token provider.
     */
    public Map<String, byte[]> getAdditionalProperties() {
        return CollectionConverter.convertMapValues(
                mAdditionalProps,
                ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY);
    }

    /**
     * Converts the request into its protocol buffer equivalent, for storage or transmission.
     */
    public Protobufs.TokenRequestInfo toProtobuf() {
        Protobufs.TokenRequestInfo.Builder builder = Protobufs.TokenRequestInfo.newBuilder();

        if (mClientId != null) {
            builder.setClientId(mClientId);
        }

        if (mNonce != null) {
            builder.setNonce(mNonce);
        }

        builder.putAllAdditionalProps(mAdditionalProps);
        return builder.build();
    }

    /**
     * Creates instances of {@link TokenRequestInfo}.
     */
    public static final class Builder {

        private String mClientId;
        private String mNonce;
        private Map<String, ByteString> mAdditionalProps;

        /**
         * Starts the process of describing a token request information message, to pass to
         * a token provider.
         */
        public Builder() {
            mAdditionalProps = new HashMap<>();
        }

        /**
         * Starts the process of describing a token request information message, using
         * the data stored within the provided protocol buffer equivalent.
         * @throws MalformedDataException if the given protocol buffer is not valid.
         */
        private Builder(Protobufs.TokenRequestInfo proto) throws MalformedDataException {
            try {
                mClientId = nullifyEmptyString(proto.getClientId());
                mNonce = nullifyEmptyString(proto.getNonce());
                setAdditionalPropertiesFromProto(proto.getAdditionalPropsMap());
            } catch (IllegalArgumentException ex) {
                throw new MalformedDataException(ex);
            }
        }

        /**
         * Specifies the client ID to pass to the token provider. Empty values are treated
         * as equivalent to null.
         */
        public Builder setClientId(@Nullable String clientId) {
            mClientId = nullifyEmptyString(clientId);
            return this;
        }

        /**
         * Specifies the nonce value to pass to the token provider. Empty values are treated
         * as equivalent to null.
         */
        public Builder setNonce(@Nullable String nonce) {
            mNonce = nullifyEmptyString(nonce);
            return this;
        }

        /**
         * Specifies the set of additional, non-standard properties to pass to the token
         * provider. A null map is treated as equivalent to an empty map. The map must
         * not contain null keys or null values, and must not contain an empty key.
         */
        public Builder setAdditionalProperties(@Nullable Map<String, byte[]> additionalProps) {
            mAdditionalProps = validateAdditionalProperties(additionalProps);
            return this;
        }

        private Builder setAdditionalPropertiesFromProto(
                @NonNull Map<String, ByteString> additionalProps) {
            mAdditionalProps = validateAdditionalPropertiesFromProto(additionalProps);
            return this;
        }

        /**
         * Creates a {@link TokenRequestInfo} instance using the properties described
         * to the builder.
         */
        public TokenRequestInfo build() {
            return new TokenRequestInfo(this);
        }
    }
}
