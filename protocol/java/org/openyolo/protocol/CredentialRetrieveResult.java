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
import static org.valid4j.Validation.validate;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openyolo.protocol.Protobufs.CredentialRetrieveResult.ResultCode;
import org.openyolo.protocol.internal.AdditionalPropertiesUtil;

/**
 * Result data which is sent in response to a credential retrieve request. Contains a result code,
 * indicating whether the request was successful or not, an optional returned credential, and
 * an optional set of additional, non-standard result properties.
 *
 * @see <a href="http://spec.openyolo.org/openyolo-android-spec.html#credential-retrieval">
 *     OpenYOLO Specification: Credential Retrieval</a>
 */
public final class CredentialRetrieveResult implements AdditionalPropertiesContainer {

    /**
     * Indicates that the provider returned a response that could not be interpreted.
     */
    public static final int CODE_UNKNOWN =
            Protobufs.CredentialRetrieveResult.ResultCode.UNSPECIFIED_VALUE;

    /**
     * Indicates that no provider was available and able to handle the associated request.
     */
    public static final int CODE_NO_PROVIDER_AVAILABLE = ResultCode.NO_PROVIDER_AVAILABLE_VALUE;

    /**
     * Indicates that the credential request sent to the provider was malformed.
     */
    public static final int CODE_BAD_REQUEST =
            Protobufs.CredentialRetrieveResult.ResultCode.BAD_REQUEST_VALUE;

    /**
     * Indicates that a credential was successfully chosen and has been returned as part of this
     * response.
     */
    public static final int CODE_CREDENTIAL_SELECTED =
            Protobufs.CredentialRetrieveResult.ResultCode.CREDENTIAL_SELECTED_VALUE;

    /**
     * Indicates that no credentials are available that meet the requirements of the credential
     * request.
     */
    public static final int CODE_NO_CREDENTIALS_AVAILABLE =
            Protobufs.CredentialRetrieveResult.ResultCode.NO_CREDENTIALS_AVAILABLE_VALUE;

    /**
     * Indicates that the user canceled the selection of a credential in a manner that indicates
     * they wish to proceed with authentication, but by manually entering their details.
     */
    public static final int CODE_USER_REQUESTS_MANUAL_AUTH =
            Protobufs.CredentialRetrieveResult.ResultCode.USER_REQUESTS_MANUAL_AUTH_VALUE;

    /**
     * Indicates that the user canceled the selection of a credential in a manner that indicates
     * they do not wish to authenticate at this time.
     */
    public static final int CODE_USER_CANCELED =
            Protobufs.CredentialRetrieveResult.ResultCode.USER_CANCELED_VALUE;

    /**
     * Indicates that the provider(s) failed to respond in a timely manner. This error is usually
     * transient, and due to the device being presently CPU-bound.
     */
    public static final int CODE_PROVIDER_TIMEOUT =
            Protobufs.CredentialRetrieveResult.ResultCode.PROVIDER_TIMEOUT_VALUE;

    /**
     * Pre-built result that indicates that the provider returned a response that could not be
     * interpreted.
     */
    public static final CredentialRetrieveResult UNKNOWN =
            new CredentialRetrieveResult.Builder(CODE_UNKNOWN).build();

    /**
     * Pre-built result that indicates that no provider was available and able to handle the
     * associated request.
     */
    public static final CredentialRetrieveResult NO_PROVIDER_AVAILABLE =
            new CredentialRetrieveResult.Builder(CODE_NO_PROVIDER_AVAILABLE).build();

    /**
     * Pre-built result that indicates the request was malformed. Carries no
     * credential or additional params.
     */
    public static final CredentialRetrieveResult BAD_REQUEST =
            new CredentialRetrieveResult.Builder(CODE_BAD_REQUEST).build();

    /**
     * Pre-built result that indicates that no credentials are available. Carries no credential or
     * additional params.
     */
    public static final CredentialRetrieveResult NO_CREDENTIALS_AVAILABLE =
            new CredentialRetrieveResult.Builder(CODE_NO_CREDENTIALS_AVAILABLE).build();

    /**
     * Pre-built result that indicates the user wishes to manually authenticate.
     */
    public static final CredentialRetrieveResult USER_REQUESTS_MANUAL_AUTH =
            new CredentialRetrieveResult.Builder(CODE_USER_REQUESTS_MANUAL_AUTH).build();

    /**
     * Pre-built result that indicates that the user does not want to authenticate at this time.
     */
    public static final CredentialRetrieveResult USER_CANCELED =
            new CredentialRetrieveResult.Builder(CODE_USER_CANCELED).build();

    /**
     * Pre-built result that indicates that the provider(s) failed to respond in a timely manner.
     * This error is usually transient, and due to the device being presently CPU-bound.
     */
    public static final CredentialRetrieveResult PROVIDER_TIMEOUT =
            new CredentialRetrieveResult.Builder(CODE_PROVIDER_TIMEOUT).build();

    /**
     * Creates a credential retrieve result from its protocol buffer byte equivalent.
     * @throws MalformedDataException if the given protocol buffer form of the request was invalid.
     */
    public static CredentialRetrieveResult fromProtobufBytes(byte[] protobufBytes)
            throws MalformedDataException {
        validate(protobufBytes, notNullValue(), MalformedDataException.class);

        try {
            return fromProtobuf(Protobufs.CredentialRetrieveResult.parseFrom(protobufBytes));
        } catch (InvalidProtocolBufferException ex) {
            throw new MalformedDataException(ex);
        }
    }

    /**
     * Creates a credential retrieve result from its protocol buffer equivalent.
     * @throws MalformedDataException if the given protocol buffer form of the request was invalid.
     */
    public static CredentialRetrieveResult fromProtobuf(Protobufs.CredentialRetrieveResult proto)
            throws MalformedDataException {
        validate(proto, notNullValue(), MalformedDataException.class);

        return new CredentialRetrieveResult.Builder(proto).build();
    }

    private final int mResultCode;

    @Nullable
    private final Credential mCredential;

    @NonNull
    private Map<String, ByteString> mAdditionalProps;

    private CredentialRetrieveResult(Builder builder) {
        mResultCode = builder.mResultCode;
        mCredential = builder.mCredential;
        mAdditionalProps = Collections.unmodifiableMap(builder.mAdditionalProps);
    }

    /**
     * Returns {@code true} if the request was successful.
     */
    public boolean isSuccessful() {
        return CODE_CREDENTIAL_SELECTED == mResultCode;
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

    @Override
    @NonNull
    public Map<String, byte[]> getAdditionalProperties() {
        return AdditionalPropertiesUtil.convertValuesToByteArrays(mAdditionalProps);
    }

    @Override
    @Nullable
    public byte[] getAdditionalProperty(String key) {
        return AdditionalPropertiesUtil.getPropertyValue(mAdditionalProps, key);
    }

    @Override
    @Nullable
    public String getAdditionalPropertyAsString(String key) {
        return AdditionalPropertiesUtil.getPropertyValueAsString(mAdditionalProps, key);
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
    public static final class Builder
            implements AdditionalPropertiesBuilder<CredentialRetrieveResult, Builder> {

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
         * @throws MalformedDataException if the given protocol buffer was invalid.
         */
        private Builder(Protobufs.CredentialRetrieveResult proto) throws MalformedDataException {
            validate(proto, notNullValue(), MalformedDataException.class);

            try {
                setResultCode(proto.getResultCodeValue());
                setCredentialFromProto(proto.getCredential());
                setAdditionalPropertiesFromProto(proto.getAdditionalPropsMap());
            } catch (IllegalArgumentException ex) {
                throw new MalformedDataException(ex);
            }
        }

        /**
         * Specifies the result code. It is recommended to use one of the standard values, though
         * any value may be used.
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
        private Builder setCredentialFromProto(@Nullable Protobufs.Credential credential)
                throws MalformedDataException {
            if (null == credential
                    || Protobufs.Credential.getDefaultInstance().equals(credential)) {
                mCredential = null;
            } else {
                mCredential = Credential.fromProtobuf(credential);
            }
            return this;
        }

        @Override
        @NonNull
        public Builder setAdditionalProperties(Map<String, byte[]> additionalProperties) {
            mAdditionalProps =
                    AdditionalPropertiesUtil.validateAdditionalProperties(additionalProperties);
            return this;
        }

        private Builder setAdditionalPropertiesFromProto(
                Map<String, ByteString> additionalProperties) {
            mAdditionalProps =
                    AdditionalPropertiesUtil.validateAdditionalPropertiesFromProto(
                            additionalProperties);
            return this;
        }

        @Override
        @NonNull
        public Builder setAdditionalProperty(@NonNull String key, @Nullable byte[] value) {
            AdditionalPropertiesUtil.setPropertyValue(mAdditionalProps, key, value);
            return this;
        }

        @Override
        @NonNull
        public Builder setAdditionalPropertyAsString(@NonNull String key, @Nullable String value) {
            AdditionalPropertiesUtil.setPropertyValueAsString(mAdditionalProps, key, value);
            return this;
        }

        /**
         * Finalizes the creation of the credential retrieval result.
         */
        @Override
        @NonNull
        public CredentialRetrieveResult build() {
            return new CredentialRetrieveResult(this);
        }
    }
}
