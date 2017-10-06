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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.openyolo.protocol.internal.AdditionalPropertiesUtil;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.CollectionConverter;

/**
 * Result data which is sent in response to a credential save request. Contains a result code,
 * indicating whether the request was successful or not, and an optional set of additional,
 * non-standard result properties.
 *
 * @see <a href="https://spec.openyolo.org/openyolo-android-spec.html#saving-credentials">
 *     OpenYOLO specification: Saving Credential</a>
 */
public final class CredentialSaveResult implements AdditionalPropertiesContainer {

    /**
     * Indicates that the provider returned a response that could not be interpreted.
     */
    public static final int CODE_UNKNOWN =
            Protobufs.CredentialSaveResult.ResultCode.UNSPECIFIED_VALUE;

    /**
     * Indicates that no provider was available and able to handle the associated request.
     */
    public static final int CODE_NO_PROVIDER_AVAILABLE =
            Protobufs.CredentialSaveResult.ResultCode.NO_PROVIDER_AVAILABLE_VALUE;

    /**
     * Indicates that the credential request sent to the provider was malformed.
     */
    public static final int CODE_BAD_REQUEST =
            Protobufs.CredentialSaveResult.ResultCode.BAD_REQUEST_VALUE;

    /**
     * Indicates that the credential sent to the provider was saved.
     */
    public static final int CODE_SAVED =
            Protobufs.CredentialSaveResult.ResultCode.SAVED_VALUE;

    /**
     * Indicates the provider refused to save the credential, due to some policy restriction. For
     * example, a provider may refuse to update an existing credential if it is stored in a shared
     * keychain. The client SHOULD NOT request to save this credential again.
     */
    public static final int CODE_PROVIDER_REFUSED =
            Protobufs.CredentialSaveResult.ResultCode.PROVIDER_REFUSED_VALUE;

    /**
     * Indicates that the user dismissed the request to save the credential, by either pressing the
     * back button, clicking outside the area of a modal dialog, or some other "soft" cancellation
     * that is not an explicit refusal to delete the credential. The client MAY request to save this
     * credential again at a later time.
     */
    public static final int CODE_USER_CANCELED =
            Protobufs.CredentialSaveResult.ResultCode.USER_CANCELED_VALUE;

    /**
     * Indicates the user refused the request to save this credential. The client SHOULD NOT request
     * to save this credential again.
     */
    public static final int CODE_USER_REFUSED =
            Protobufs.CredentialSaveResult.ResultCode.USER_REFUSED_VALUE;

    /**
     * Pre-built save result for an unspecified outcome. Carries no additional properties.
     */
    public static final CredentialSaveResult UNKNOWN =
            new CredentialSaveResult.Builder(CODE_UNKNOWN).build();

    /**
     * Pre-built save result that indicates that no provider was available and able to handle the
     * associated request.
     */
    public static final CredentialSaveResult NO_PROVIDER_AVAILABLE =
            new CredentialSaveResult.Builder(CODE_NO_PROVIDER_AVAILABLE).build();

    /**
     * Pre-built save result that indicates the credential was saved. Carries no additional
     * properties.
     */
    public static final CredentialSaveResult SAVED =
            new CredentialSaveResult.Builder(CODE_SAVED).build();

    /**
     * Pre-built save result that indicates the request was malformed. Carries no additional
     * properties.
     */
    public static final CredentialSaveResult BAD_REQUEST =
            new CredentialSaveResult.Builder(CODE_BAD_REQUEST).build();

    /**
     * Indicates that the user dismissed the request to save the credential, by either pressing the
     * back button, clicking outside the area of a modal dialog, or some other "soft" cancellation
     * that is not an explicit refusal to save the credential. The client MAY request to save this
     * credential again at a later time.
     */
    public static final CredentialSaveResult USER_CANCELED =
            new CredentialSaveResult.Builder(CODE_USER_CANCELED).build();

    /**
     * Pre-built save result that indicates the request was refused by the user. Carries no
     * additional properties.
     */
    public static final CredentialSaveResult USER_REFUSED =
            new CredentialSaveResult.Builder(CODE_USER_REFUSED).build();

    /**
     * Pre-built save result that indicates the request was refused by the provider. Carries no
     * additional properties.
     */
    public static final CredentialSaveResult PROVIDER_REFUSED =
            new CredentialSaveResult.Builder(CODE_PROVIDER_REFUSED).build();

    private final int mResultCode;

    @NonNull
    private Map<String, ByteString> mAdditionalProperties;

    /**
     * Creates a credential save result from its protocol buffer byte equivalent.
     * @throws MalformedDataException if the result could not be parsed or validated.
     */
    public static CredentialSaveResult fromProtobufBytes(byte[] protobufBytes)
            throws MalformedDataException {
        validate(protobufBytes, notNullValue(), MalformedDataException.class);

        try {
            return fromProtobuf(Protobufs.CredentialSaveResult.parseFrom(protobufBytes));
        } catch (IOException ex) {
            throw new MalformedDataException(ex);
        }
    }

    /**
     * Creates a credential save result from its protocol buffer equivalent.
     * @throws MalformedDataException if the result could not be parsed or validated.
     */
    public static CredentialSaveResult fromProtobuf(Protobufs.CredentialSaveResult proto)
            throws MalformedDataException {
        validate(proto, notNullValue(), MalformedDataException.class);

        return new CredentialSaveResult.Builder(proto).build();
    }

    /**
     * Creates a protocol buffer representation of the credential save result, for transmission
     * or storage.
     */
    public Protobufs.CredentialSaveResult toProtobuf() {
        Protobufs.CredentialSaveResult.Builder builder =
                Protobufs.CredentialSaveResult.newBuilder()
                        .setResultCodeValue(mResultCode)
                        .putAllAdditionalProps(mAdditionalProperties);

        return builder.build();
    }

    /**
     * Creates an {@link Intent} containing this credential save result, that a provider should
     * return as its activity result.
     */
    public Intent toResultDataIntent() {
        Intent intent = new Intent();
        intent.putExtra(
                ProtocolConstants.EXTRA_SAVE_RESULT,
                toProtobuf().toByteArray());
        return intent;
    }

    private CredentialSaveResult(Builder builder) {
        mResultCode = builder.mResultCode;
        mAdditionalProperties = builder.mAdditionalProps;
    }

    /**
     * Returns {@code true} if the request was successful.
     */
    public boolean isSuccessful() {
        return CODE_SAVED == mResultCode;
    }


    /**
     * The result code for the credential save operation.
     */
    public int getResultCode() {
        return mResultCode;
    }

    @Override
    @NonNull
    public Map<String, byte[]> getAdditionalProperties() {
        return AdditionalPropertiesUtil.convertValuesToByteArrays(mAdditionalProperties);
    }

    @Nullable
    @Override
    public byte[] getAdditionalProperty(String key) {
        return AdditionalPropertiesUtil.getPropertyValue(mAdditionalProperties, key);
    }

    @Nullable
    @Override
    public String getAdditionalPropertyAsString(String key) {
        return AdditionalPropertiesUtil.getPropertyValueAsString(mAdditionalProperties, key);
    }

    /**
     * Creates {@link CredentialSaveResult} instances.
     */
    public static final class Builder
            implements AdditionalPropertiesBuilder<CredentialSaveResult, Builder> {
        private int mResultCode = CODE_UNKNOWN;
        private Map<String, ByteString> mAdditionalProps = new HashMap<>();

        /**
         * Starts the process of describing a credential retrieve result given the mandatory status
         * code.
         * @throws MalformedDataException if the given protocol buffer was not valid.
         */
        private Builder(Protobufs.CredentialSaveResult proto) throws MalformedDataException {
            validate(proto, notNullValue(), MalformedDataException.class);

            try {
                setResultCode(proto.getResultCodeValue());
                setAdditionalProperties(
                        CollectionConverter.convertMapValues(
                                proto.getAdditionalPropsMap(),
                                ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY));
            } catch (IllegalArgumentException ex) {
                throw new MalformedDataException(ex);
            }
        }

        /**
         * Seeds a credential save result from its protocol buffer equivalent.
         */
        public Builder(int resultCode) {
            setResultCode(resultCode);
        }

        /**
         * Specifies the result code. It is recommended to use one of the standard values, though
         * any value may be used.
         */
        public Builder setResultCode(int resultCode) {
            mResultCode = resultCode;

            return this;
        }

        @Override
        @NonNull
        public Builder setAdditionalProperties(@Nullable Map<String, byte[]> additionalProps) {
            mAdditionalProps =
                    AdditionalPropertiesUtil.validateAdditionalProperties(additionalProps);
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
         * Finalizes the creation of the credential save result.
         */
        @Override
        @NonNull
        public CredentialSaveResult build() {
            return new CredentialSaveResult(this);
        }
    }
}
