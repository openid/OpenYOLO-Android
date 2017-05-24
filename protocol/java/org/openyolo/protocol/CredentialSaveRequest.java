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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openyolo.protocol.internal.AdditionalPropertiesUtil;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.ClientVersionUtil;
import org.openyolo.protocol.internal.CollectionConverter;

/**
 * A request to save a given {@link Credential credential}. To make the request the client
 * encodes it in its associated protocol buffer byte array form in an activity intent
 * via the {@link ProtocolConstants#EXTRA_SAVE_REQUEST} extra.
 *
 * @see <a href="https://spec.openyolo.org/openyolo-android-spec.html#saving-credentials">
 *     OpenYOLO specification: Saving Credentials</a>
 */
public final class CredentialSaveRequest implements Parcelable {

    /**
     * Parcelable reader for {@link CredentialSaveRequest} instances.
     * @see android.os.Parcelable
     */
    public static final Creator<CredentialSaveRequest> CREATOR = new CredentialSaveRequestCreator();

    @NonNull
    private final Credential mCredential;

    @NonNull
    private final Map<String, ByteString> mAdditionalProperties;

    /**
     * Creates a credential save request from its protocol buffer byte array form.
     * @throws MalformedDataException if the given protocol buffer was invalid.
     *
     * @see #toProtocolBuffer()
     */
    public static CredentialSaveRequest fromProtoBytes(byte[] protoBytes)
            throws MalformedDataException {
        validate(protoBytes, notNullValue(), MalformedDataException.class);

        try {
            Protobufs.CredentialSaveRequest request =
                    Protobufs.CredentialSaveRequest.parseFrom(protoBytes);
            return new CredentialSaveRequest.Builder(request).build();
        } catch (IOException ex) {
            throw new MalformedDataException(ex);
        }
    }

    /**
     * Creates a credential save request from its protocol buffer form.
     * @throws MalformedDataException if the given protocol buffer was invalid.
     *
     * @see #toProtocolBuffer()
     */
    public static CredentialSaveRequest fromProtobuf(Protobufs.CredentialSaveRequest request)
            throws MalformedDataException {
        validate(request, notNullValue(), MalformedDataException.class);

        return new CredentialSaveRequest.Builder(request).build();
    }

    /**
     * Creates a {@link CredentialSaveRequest} from the given {@link Credential}.
     */
    public static CredentialSaveRequest fromCredential(Credential credential) {
        return new Builder(credential).build();
    }

    private CredentialSaveRequest(Builder builder) {
        mCredential = builder.mCredential;
        mAdditionalProperties = Collections.unmodifiableMap(builder.mAdditionalProps);
    }

    /**
     * The credential to be saved.
     */
    @NonNull
    public Credential getCredential() {
        return mCredential;
    }

    /**
     * The map of additional, non-standard properties included with this request.
     */
    @NonNull
    public Map<String, byte[]> getAdditionalProperties() {
        return CollectionConverter.convertMapValues(
            mAdditionalProperties,
            ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY);
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
    public Protobufs.CredentialSaveRequest toProtocolBuffer() {
        return Protobufs.CredentialSaveRequest.newBuilder()
                .setClientVersion(ClientVersionUtil.getClientVersion())
                .setCredential(mCredential.toProtobuf())
                .putAllAdditionalProps(mAdditionalProperties)
                .build();
    }

    /**
     * Creates {@link CredentialSaveRequest} instances.
     */
    public static final class Builder {

        private Credential mCredential;
        private Map<String, ByteString> mAdditionalProps = new HashMap<>();

        /**
         * Recreates a save request from its protocol buffer form.
         *
         * @see CredentialSaveRequest#toProtocolBuffer()
         */
        private Builder(@NonNull Protobufs.CredentialSaveRequest request)
                throws MalformedDataException {
            validate(request, notNullValue(), MalformedDataException.class);

            try {
                setCredential(Credential.fromProtobuf(request.getCredential()));
                setAdditionalProperties(
                        CollectionConverter.convertMapValues(
                                request.getAdditionalPropsMap(),
                                ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY));
            } catch (IllegalArgumentException ex) {
                throw new MalformedDataException(ex);
            }
        }

        /**
         * Starts the process of defining a save request, specifying the credential to be saved.
         *
         * @see Credential
         */
        public Builder(@NonNull Credential credential) {
            setCredential(credential);
        }

        /**
         * Specifies the credential to be saved. The provided credential must be non-null.
         *
         * @see Credential
         */
        public Builder setCredential(@NonNull Credential credential) {
            validate(credential, notNullValue(), IllegalArgumentException.class);

            mCredential = credential;
            return this;
        }

        /**
         * Specifies the set of additional, non-standard properties to carry with the credential
         * save request. A null map is treated as an empty map.
         */
        @NonNull
        public Builder setAdditionalProperties(@Nullable Map<String, byte[]> additionalParams) {
            mAdditionalProps =
                    AdditionalPropertiesUtil.validateAdditionalProperties(additionalParams);
            return this;
        }

        /**
         * Finalizes the creation of the credential save request.
         */
        public CredentialSaveRequest build() {
            return new CredentialSaveRequest(this);
        }
    }

    private static final class CredentialSaveRequestCreator
            implements Creator<CredentialSaveRequest> {
        @Override
        public CredentialSaveRequest createFromParcel(Parcel source) {
            int length = source.readInt();
            byte[] encodedRequest = new byte[length];
            source.readByteArray(encodedRequest);

            try {
                return CredentialSaveRequest.fromProtoBytes(encodedRequest);
            } catch (MalformedDataException ex) {
                throw new IllegalStateException("Unable to read proto from parcel", ex);
            }
        }

        @Override
        public CredentialSaveRequest[] newArray(int size) {
            return new CredentialSaveRequest[0];
        }
    }
}
