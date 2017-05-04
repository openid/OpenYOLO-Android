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

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.openyolo.protocol.internal.CustomMatchers.notNullOrEmptyString;
import static org.valid4j.Assertive.require;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.CollectionConverter;

/**
 * Result data which is sent in response to a credential retrieve request. Contains a result code,
 * indicating whether the request was successful or not, an optional returned credential, and
 * an optional set of additional, non-standard result properties.
 */
public final class CredentialRetrieveResult {

    /**
     * Indicates that the provider returned a response that could not be interpreted.
     */
    public static final int RESULT_UNKNOWN =
            Protobufs.CredentialRetrieveResult.ResultCode.UNSPECIFIED_VALUE;

    /**
     * Indicates that the credential request completed without any issues.
     */
    public static final int RESULT_SUCCESS =
            Protobufs.CredentialRetrieveResult.ResultCode.SUCCESS_VALUE;

    /**
     * Indicates that the credential request was denied by the provider.
     */
    public static final int RESULT_REJECTED_BY_PROVIDER =
            Protobufs.CredentialRetrieveResult.ResultCode.REJECTED_BY_PROVIDER_VALUE;

    /**
     * Indicates that the credential request was denied by the user.
     */
    public static final int RESULT_REJECTED_BY_USER =
            Protobufs.CredentialRetrieveResult.ResultCode.REJECTED_BY_USER_VALUE;

    /**
     * Indicates that the credential request was rejected by the provider.
     */
    public static final CredentialRetrieveResult REJECTED_BY_PROVIDER =
            new CredentialRetrieveResult.Builder(RESULT_REJECTED_BY_PROVIDER).build();

    /**
     * Indicates that the credential request was rejected by the user.
     */
    public static final CredentialRetrieveResult REJECTED_BY_USER =
            new CredentialRetrieveResult.Builder(RESULT_REJECTED_BY_USER).build();

    /**
     * Creates a credential retrieve request from its protocol buffer byte equivalent.
     * @throws IOException if the request could not be parsed or validated.
     */
    public static CredentialRetrieveResult fromProtobufBytes(byte[] protobufBytes)
            throws IOException {
        return fromProtobuf(Protobufs.CredentialRetrieveResult.parseFrom(protobufBytes));
    }

    /**
     * Creates a credential retrieve request from its protocol buffer equivalent.
     */
    public static CredentialRetrieveResult fromProtobuf(Protobufs.CredentialRetrieveResult proto) {
        return new CredentialRetrieveResult.Builder(proto).build();
    }

    private final int mResultCode;

    @Nullable
    private final Credential mCredential;

    @NonNull
    private Map<String, ByteString> mAdditionalProps;

    private CredentialRetrieveResult(
            int resultCode,
            Credential credential,
            Map<String, ByteString> additionalProps) {
        mResultCode = resultCode;
        mCredential = credential;
        mAdditionalProps = additionalProps;
    }

    /**
     * The result code for the credential retrieve operation.
     */
    public int getResultCode() {
        return mResultCode;
    }

    /**
     * The credential returned by the credential provider, if available.
     */
    @Nullable
    public Credential getCredential() {
        return mCredential;
    }

    /**
     * The additional, non-standard properties returned by the credential provider, if available.
     */
    @NonNull
    public Map<String, byte[]> getAdditionalProps() {
        return CollectionConverter.convertMapValues(
                mAdditionalProps,
                ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY);
    }

    /**
     * Creates a protocol buffer representation of the credential retrieve result, for transmission
     * or storage.
     */
    public Protobufs.CredentialRetrieveResult toProtobuf() {
        Protobufs.CredentialRetrieveResult.Builder builder =
                Protobufs.CredentialRetrieveResult.newBuilder()
                        .setResultCodeValue(mResultCode)
                        .putAllAdditionalProps(mAdditionalProps);

        if (mCredential != null) {
            builder.setCredential(mCredential.toProtobuf());
        }

        return builder.build();
    }

    /**
     * Creates an {@link Intent} containing this credential retrieve result, that a provider should
     * return as its activity result.
     */
    public Intent toResultDataIntent() {
        Intent intent = new Intent();
        intent.putExtra(
                ProtocolConstants.EXTRA_RETRIEVE_RESULT,
                toProtobuf().toByteArray());
        return intent;
    }

    /**
     * Creates validated {@link CredentialRetrieveResult} instances.
     */
    public static final class Builder {

        private int mResultCode;
        private Credential mCredential;
        private Map<String, ByteString> mAdditionalProps = new HashMap<>();

        /**
         * Starts the process of describing a credential retrieve result, and includes the
         * mandatory status code.
         */
        public Builder(int resultCode) {
            setResultCode(resultCode);
        }

        /**
         * Seeds a credential retrieval result from its protocol buffer equivalent.
         */
        public Builder(Protobufs.CredentialRetrieveResult proto) {
            setResultCode(proto.getResultCodeValue());
            setCredentialFromProto(proto.getCredential());
            setAdditionalPropertiesFromProto(proto.getAdditionalPropsMap());
        }

        /**
         * Specifies the result code. It is recommended to use one of the standard values, though
         * any value may be used.
         *
         * @see CredentialRetrieveResult#RESULT_SUCCESS
         * @see CredentialRetrieveResult#RESULT_REJECTED_BY_PROVIDER
         * @see CredentialRetrieveResult#RESULT_REJECTED_BY_USER
         * @see CredentialRetrieveResult#RESULT_UNKNOWN
         */
        public Builder setResultCode(int resultCode) {
            mResultCode = resultCode;
            return this;
        }

        /**
         * Specifies the credential being returned as part of this result. Can be null.
         */
        public Builder setCredential(@Nullable Credential credential) {
            mCredential = credential;
            return this;
        }

        /**
         * Specifies the credential being returned as part of this result, in protocol buffer
         * form. Can be null.
         */
        public Builder setCredentialFromProto(@Nullable Protobufs.Credential credential) {
            mCredential = credential != null ? Credential.fromProtobuf(credential) : null;
            return this;
        }

        /**
         * Specifies the set of additional, non-standard properties to return as part of this
         * result. The keys and values of the provided map must not be null. The keys must not
         * be empty.
         */
        public Builder setAdditionalProperties(Map<String, byte[]> additionalProperties) {
            require(additionalProperties, notNullValue());
            require(additionalProperties.keySet(), everyItem(notNullOrEmptyString()));
            require(additionalProperties.values(), everyItem(notNullValue()));

            mAdditionalProps =
                    CollectionConverter.convertMapValues(
                            additionalProperties,
                            ByteStringConverters.BYTE_ARRAY_TO_BYTE_STRING);
            return this;
        }

        Builder setAdditionalPropertiesFromProto(Map<String, ByteString> additionalProperties) {
            require(additionalProperties, notNullValue());
            require(additionalProperties.keySet(), everyItem(notNullOrEmptyString()));
            require(additionalProperties.values(), everyItem(notNullValue()));

            mAdditionalProps = additionalProperties;
            return this;
        }

        /**
         * Finalizes the creation of the credential retrieval result.
         */
        public CredentialRetrieveResult build() {
            return new CredentialRetrieveResult(mResultCode, mCredential, mAdditionalProps);
        }
    }
}
