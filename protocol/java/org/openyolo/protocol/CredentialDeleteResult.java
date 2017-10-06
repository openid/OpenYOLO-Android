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
import org.openyolo.protocol.internal.AdditionalPropertiesUtil;
import org.openyolo.protocol.internal.IntentProtocolBufferExtractor;

/**
 * A response to a {@link CredentialDeleteRequest credential deletion request}. The status code
 * indicates the outcome of the request.
 *
 * @see <a href="http://spec.openyolo.org/openyolo-android-spec.html#credential-deletion">
 *     OpenYOLO Specification: Credential Deletion</a>
 */
public final class CredentialDeleteResult implements AdditionalPropertiesContainer {

    /**
     * Indicates that the provider returned a response that could not be interpreted.
     */
    public static final int CODE_UNKNOWN =
            Protobufs.CredentialDeleteResult.ResultCode.UNSPECIFIED_VALUE;

    /**
     * Indicates that no provider was available and able to handle the associated request.
     */
    public static final int CODE_NO_PROVIDER_AVAILABLE =
            Protobufs.CredentialDeleteResult.ResultCode.NO_PROVIDER_AVAILABLE_VALUE;

    /**
     * Indicates that the deletion request sent to the provider was malformed.
     */
    public static final int CODE_BAD_REQUEST =
            Protobufs.CredentialDeleteResult.ResultCode.BAD_REQUEST_VALUE;

    /**
     * Indicates that the credential was deleted.
     */
    public static final int CODE_DELETED =
            Protobufs.CredentialDeleteResult.ResultCode.DELETED_VALUE;

    /**
     * Indicates that the credential provider did not have a credential to delete that matched
     * the provided credential.
     */
    public static final int CODE_NO_MATCHING_CREDENTIAL =
            Protobufs.CredentialDeleteResult.ResultCode.NO_MATCHING_CREDENTIAL_VALUE;

    /**
     * Indicates that the provider refused to delete the provided credential, due to some policy
     * restriction it is enforcing.
     */
    public static final int CODE_PROVIDER_REFUSED =
            Protobufs.CredentialDeleteResult.ResultCode.PROVIDER_REFUSED_VALUE;

    /**
     * Indicates that the user dismissed the request to delete the credential, by either pressing
     * the back button, clicking outside the area of a modal dialog, or some other "soft"
     * cancellation that is not an explicit refusal to delete the credential.
     */
    public static final int CODE_USER_CANCELED =
            Protobufs.CredentialDeleteResult.ResultCode.USER_CANCELED_VALUE;

    /**
     * Indicates that The user explicitly refused to delete the credential, by selecting a
     * "do not delete" (or similarly phrased) option in the presented UI.
     */
    public static final int CODE_USER_REFUSED =
            Protobufs.CredentialDeleteResult.ResultCode.USER_REFUSED_VALUE;

    /**
     * Pre-built deletion result for a bad request. Carries no additional properties.
     */
    public static final CredentialDeleteResult UNKNOWN =
            new CredentialDeleteResult.Builder(CODE_UNKNOWN).build();

    /**
     * Pre-built deletion result that indicates that no provider was available and able to handle
     * the associated request.
     */
    public static final CredentialDeleteResult NO_PROVIDER_AVAILABLE =
            new CredentialDeleteResult.Builder(CODE_NO_PROVIDER_AVAILABLE).build();

    /**
     * Pre-built deletion result for a bad request. Carries no additional properties.
     */
    public static final CredentialDeleteResult BAD_REQUEST =
            new CredentialDeleteResult.Builder(CODE_BAD_REQUEST).build();

    /**
     * Pre-built deletion result for a successful deletion. Carries no additional properties.
     */
    public static final CredentialDeleteResult DELETED =
            new CredentialDeleteResult.Builder(CODE_DELETED).build();

    /**
     * Pre-built deletion result for when no matching credential exists to delete.  Carries no
     * additional properties.
     */
    public static final CredentialDeleteResult NO_MATCHING_CREDENTIAL =
            new CredentialDeleteResult.Builder(CODE_NO_MATCHING_CREDENTIAL).build();

    /**
     * Pre-built deletion result for when the provider refuses to delete the credential.  Carries
     * no additional properties.
     */
    public static final CredentialDeleteResult PROVIDER_REFUSED =
            new CredentialDeleteResult.Builder(CODE_PROVIDER_REFUSED).build();

    /**
     * Pre-built deletion result for when the user cancels the deletion operation.  Carries no
     * additional properties.
     */
    public static final CredentialDeleteResult USER_CANCELED =
            new CredentialDeleteResult.Builder(CODE_USER_CANCELED).build();

    /**
     * Pre-built deletion result for when the user refuses to delete the credential.  Carries no
     * additional properties.
     */
    public static final CredentialDeleteResult USER_REFUSED =
            new CredentialDeleteResult.Builder(CODE_USER_REFUSED).build();

    private static final String UNABLE_TO_EXTRACT_RESULT =
            "Unable to extract or parse the credential deletion result";

    /**
     * Creates a credential delete result from its protocol buffer equivalent.
     * @throws MalformedDataException if the given protocol buffer is not valid.
     */
    public static CredentialDeleteResult fromProtobuf(Protobufs.CredentialDeleteResult proto)
            throws MalformedDataException {
        validate(proto, notNullValue(), IllegalArgumentException.class);

        return new CredentialDeleteResult.Builder(proto).build();
    }

    /**
     * Creates a credential delete result from its protocol buffer equivalent, in byte array form.
     * @throws MalformedDataException if the given protocol buffer is not valid.
     */
    public static CredentialDeleteResult fromProtobufBytes(@Nullable byte[] protoBytes)
            throws MalformedDataException {
        validate(protoBytes, notNullValue(), MalformedDataException.class);

        try {
            return fromProtobuf(Protobufs.CredentialDeleteResult.parseFrom(protoBytes));
        } catch (InvalidProtocolBufferException ex) {
            throw new MalformedDataException(ex);
        }
    }

    /**
     * Extracts a credential deletion result from the result intent data a provider returns
     * on completion of their deletion activity.
     * @throws MalformedDataException if the protocol buffer could not be parsed from the intent.
     */
    public static CredentialDeleteResult fromResultIntentData(
            @Nullable Intent intent)
            throws MalformedDataException {
        return fromProtobuf(
                IntentProtocolBufferExtractor.extract(
                        ProtocolConstants.EXTRA_DELETE_RESULT,
                        Protobufs.CredentialDeleteResult.parser(),
                        UNABLE_TO_EXTRACT_RESULT,
                        intent));
    }

    private final int mResultCode;

    @NonNull
    private final Map<String, ByteString> mAdditionalProps;

    private CredentialDeleteResult(Builder builder) {
        mResultCode = builder.mResultCode;
        mAdditionalProps = Collections.unmodifiableMap(builder.mAdditionalProps);
    }

    /**
     * Returns {@code true} if the request was successful.
     */
    public boolean isSuccessful() {
        return CODE_DELETED == mResultCode;
    }

    /**
     * The result code for the credential deletion operation.
     */
    public int getResultCode() {
        return mResultCode;
    }

    @Override
    @NonNull
    public Map<String, byte[]> getAdditionalProperties() {
        return AdditionalPropertiesUtil.convertValuesToByteArrays(mAdditionalProps);
    }

    /**
     * Returns the additional, non-standard property identified by the specified key. If this
     * additional property does not exist, then `null` is returned.
     */
    @Nullable
    public byte[] getAdditionalProperty(String key) {
        return AdditionalPropertiesUtil.getPropertyValue(mAdditionalProps, key);
    }

    /**
     * Returns the additional, non-standard property identified by the specified key, where the
     * value is assumed to be a UTF-8 encoded string. If this additional property does not exist,
     * then `null` is returned.
     */
    @Nullable
    public String getAdditionalPropertyAsString(String key) {
        return AdditionalPropertiesUtil.getPropertyValueAsString(mAdditionalProps, key);
    }

    /**
     * Creates the protocol buffer equivalent to this credential deletion result.
     */
    public Protobufs.CredentialDeleteResult toProtobuf() {
        return Protobufs.CredentialDeleteResult.newBuilder()
                .setResultCodeValue(mResultCode)
                .putAllAdditionalProps(mAdditionalProps)
                .build();
    }

    /**
     * Extracts a deletion result from the intent returned to the requester by the provider.
     */
    public Intent toResultDataIntent() {
        Intent resultData = new Intent();
        resultData.putExtra(
                ProtocolConstants.EXTRA_DELETE_RESULT,
                toProtobuf().toByteArray());
        return resultData;
    }

    /**
     * Creates instances of {@link CredentialDeleteResult}.
     */
    public static final class Builder
            implements AdditionalPropertiesBuilder<CredentialDeleteResult, Builder> {

        private int mResultCode;

        @NonNull
        private Map<String, ByteString> mAdditionalProps = new HashMap<>();

        /**
         * Starts the process of describing a credential deletion result, based on the properties
         * contained in the provided protocol buffer.
         * @throws MalformedDataException if the protocol buffer could not be parsed from the
         *     intent.
         */
        private Builder(Protobufs.CredentialDeleteResult proto) throws MalformedDataException {
            try {
                setResultCode(proto.getResultCodeValue());
                setAdditionalPropertiesFromProto(proto.getAdditionalPropsMap());
            } catch (IllegalArgumentException ex) {
                throw new MalformedDataException(ex);
            }
        }

        /**
         * Starts the process of describing a credential deletion request, specifying the mandatory
         * result code.
         */
        public Builder(int resultCode) {
            setResultCode(resultCode);
        }

        /**
         * Specifies the result code for the deletion operation.
         */
        public Builder setResultCode(int resultCode) {
            mResultCode = resultCode;
            return this;
        }

        @Override
        @NonNull
        public Builder setAdditionalProperties(@Nullable Map<String, byte[]> additionalProperties) {
            mAdditionalProps = AdditionalPropertiesUtil.validateAdditionalProperties(
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

        private Builder setAdditionalPropertiesFromProto(
                Map<String, ByteString> additionalProperties) {
            mAdditionalProps = AdditionalPropertiesUtil.validateAdditionalPropertiesFromProto(
                    additionalProperties);
            return this;
        }

        /**
         * Creates the credential deletion result, from the specified properties.
         */
        @Override
        @NonNull
        public CredentialDeleteResult build() {
            return new CredentialDeleteResult(this);
        }
    }
}
