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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openyolo.protocol.Protobufs.HintRetrieveResult.ResultCode;
import org.openyolo.protocol.internal.AdditionalPropertiesUtil;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.CollectionConverter;

/**
 * A response to a {@link HintRetrieveRequest hint request}. The status code indicates the outcome
 * of the request, and the response may contain a hint.
 */
public final class HintRetrieveResult {

    /**
     * Indicates that the provider returned a response that could not be interpreted.
     */
    public static final int CODE_UNKNOWN = ResultCode.UNSPECIFIED_VALUE;

    /**
     * Indicates that the hint request sent to the provider was malformed.
     */
    public static final int CODE_BAD_REQUEST = ResultCode.BAD_REQUEST_VALUE;

    /**
     * Indicates that the user selected a hint, that has been returned as part of this response.
     */
    public static final int CODE_HINT_SELECTED = ResultCode.HINT_SELECTED_VALUE;

    /**
     * Indicates that no hints are available that meet the requirements of the hint request.
     */
    public static final int CODE_NO_HINTS_AVAILABLE = ResultCode.NO_HINTS_AVAILABLE_VALUE;

    /**
     * Indicates that the user  canceled the selection of a hint in a manner that indicates they
     * wish to proceed with authentication, but by manually entering their details.
     */
    public static final int CODE_USER_REQUESTS_MANUAL_AUTH =
            ResultCode.USER_REQUESTS_MANUAL_AUTH_VALUE;

    /**
     * Indicates that The user canceled the selection of a hint in a manner that indicates they do
     * not wish to authenticate at this time.
     */
    public static final int CODE_USER_CANCELED = ResultCode.USER_CANCELED_VALUE;

    /**
     * Pre-built hint result for an unspecified outcome. Carries no hint or additional properties.
     */
    public static final HintRetrieveResult UNKNOWN =
            new HintRetrieveResult.Builder(HintRetrieveResult.CODE_UNKNOWN).build();

    /**
     * Pre-built hint result for a bad request. Carries no hint or additional properties.
     */
    public static final HintRetrieveResult BAD_REQUEST =
            new HintRetrieveResult.Builder(HintRetrieveResult.CODE_BAD_REQUEST).build();

    /**
     * Pre-built hint result for when no hints are available. Carries no hint or additional
     * properties.
     */
    public static final HintRetrieveResult NO_HINTS_AVAILABLE =
            new HintRetrieveResult.Builder(HintRetrieveResult.CODE_NO_HINTS_AVAILABLE).build();

    /**
     * Pre-built hint result for when the user requests manual authentication. Carries no hint
     * or additional properties.
     */
    public static final HintRetrieveResult USER_REQUESTS_MANUAL_AUTH =
            new HintRetrieveResult.Builder(
                    HintRetrieveResult.CODE_USER_REQUESTS_MANUAL_AUTH)
                    .build();

    /**
     * Pre-built hint result for when the user canceled the selection of a hint in a manner that
     * indicates they do not wish to authenticate at this time. Carries no hint or additional
     * properties.
     */
    public static final HintRetrieveResult USER_CANCELED =
            new HintRetrieveResult.Builder(HintRetrieveResult.CODE_USER_CANCELED).build();

    /**
     * Creates a hint result from its protocol buffer equivalent.
     * @throws MalformedDataException if the given protocol buffer is not valid.
     */
    public static HintRetrieveResult fromProtobuf(Protobufs.HintRetrieveResult proto)
            throws MalformedDataException {
        return new HintRetrieveResult.Builder(proto).build();
    }

    /**
     * Creates a hint result from its protocol buffer equivalent, in byte array form.
     * @throws MalformedDataException if the given protocol buffer is not valid.
     */
    public static HintRetrieveResult fromProtobufBytes(byte[] protoBytes)
            throws MalformedDataException {
        validate(protoBytes, notNullValue(), MalformedDataException.class);

        try {
            return fromProtobuf(Protobufs.HintRetrieveResult.parseFrom(protoBytes));
        } catch (IOException ex) {
            throw new MalformedDataException(ex);
        }
    }

    private final int mResultCode;

    @Nullable
    private final Hint mHint;

    @NonNull
    private final Map<String, ByteString> mAdditionalProps;

    private HintRetrieveResult(Builder builder) {
        mResultCode = builder.mResultCode;
        mHint = builder.mHint;
        mAdditionalProps = Collections.unmodifiableMap(builder.mAdditionalProps);
    }

    /**
     * Creates a protocol buffer representation of the hint retrieve result, for transmission or
     * storage.
     */
    public Protobufs.HintRetrieveResult toProtobuf() {
        Protobufs.HintRetrieveResult.Builder builder =
                Protobufs.HintRetrieveResult.newBuilder()
                        .setResultCodeValue(mResultCode)
                        .putAllAdditionalProps(mAdditionalProps);

        if (mHint != null) {
            builder.setHint(mHint.toProtobuf());
        }

        return builder.build();
    }

    /**
     * Creates an {@link Intent} containing this hint retrieve result, that a provider should
     * return as its hint activity result.
     */
    public Intent toResultDataIntent() {
        Intent intent = new Intent();
        intent.putExtra(
                ProtocolConstants.EXTRA_HINT_RESULT,
                toProtobuf().toByteArray());
        return intent;
    }

    /**
     * The hint retrieve result code.
     */
    public int getResultCode() {
        return mResultCode;
    }

    /**
     * The hint returned as part of the hint result, if available.
     */
    @Nullable
    public Hint getHint() {
        return mHint;
    }

    /**
     * The additional, non-standard properties returned by the credential provider as part of the
     * hint result, if available.
     */
    @NonNull
    public Map<String, byte[]> getAdditionalProperties() {
        return CollectionConverter.convertMapValues(
                mAdditionalProps,
                ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY);
    }

    /**
     * Creates {@link HintRetrieveResult} instances.
     */
    public static final class Builder {

        private int mResultCode;

        @Nullable
        private Hint mHint;

        @NonNull
        private Map<String, ByteString> mAdditionalProps = new HashMap<>();

        /**
         * Starts the process of describing a hint retrieve result, based on the properties
         * contained in the provided protocol buffer.
         */
        private Builder(@NonNull Protobufs.HintRetrieveResult proto) throws MalformedDataException {
            validate(proto, notNullValue(), MalformedDataException.class);

            try {
                setResultCode(proto.getResultCodeValue());
                setAdditionalPropertiesFromProto(proto.getAdditionalPropsMap());
                setHintFromProto(proto.getHint());
            } catch (IllegalArgumentException ex) {
                throw new MalformedDataException(ex);
            }
        }

        /**
         * Starts the process of describing a hint retrieve result, specifying the mandatory
         * result code.
         */
        public Builder(int resultCode) {
            setResultCode(resultCode);
        }

        /**
         * Specifies the result code.
         */
        public Builder setResultCode(int resultCode) {
            mResultCode = resultCode;
            return this;
        }

        /**
         * Specifies the returned hint, if available.
         */
        public Builder setHint(@Nullable Hint hint) {
            mHint = hint;
            return this;
        }

        private Builder setHintFromProto(@Nullable Protobufs.Hint hint)
                throws MalformedDataException {
            if (null == hint) {
                mHint = null;
            } else {
                mHint = Hint.fromProtobuf(hint);
            }

            return this;
        }

        /**
         * Specifies the set of additional, non-standard properties to return as part of this
         * result. A null map is treated as an empty map. For a non-null map, all keys and values
         * must not be null, and the keys must not be empty.
         */
        public Builder setAdditionalProperties(Map<String, byte[]> additionalProps) {
            mAdditionalProps =
                    AdditionalPropertiesUtil.validateAdditionalProperties(additionalProps);
            return this;
        }

        private Builder setAdditionalPropertiesFromProto(Map<String, ByteString> additionalProps) {
            mAdditionalProps =
                    AdditionalPropertiesUtil.validateAdditionalPropertiesFromProto(additionalProps);
            return this;
        }

        /**
         * Creates the hint retrieve result, from the specified properties.
         */
        public HintRetrieveResult build() {
            return new HintRetrieveResult(this);
        }
    }
}
