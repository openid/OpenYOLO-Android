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
import static org.openyolo.protocol.internal.CustomMatchers.isWebUri;
import static org.openyolo.protocol.internal.CustomMatchers.notNullOrEmptyString;
import static org.openyolo.protocol.internal.CustomMatchers.nullOr;
import static org.openyolo.protocol.internal.StringUtil.nullifyEmptyString;
import static org.valid4j.Validation.validate;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openyolo.protocol.internal.AdditionalPropertiesUtil;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.CollectionConverter;

/**
 * A representation of a credential for use in sign-in. This provides a higher-level,
 * data-verifying wrapper for the underlying {@link Protobufs.Credential protocol buffer} that is
 * returned by the provider.
 *
 * <p>Credentials must have an {@link #getIdentifier() identifier},
 * an {@link #getAuthenticationMethod() authentication method} and an
 * {@link #getAuthenticationDomain() authentication domain}, but all other properties are optional.
 *
 * @see <a href="http://spec.openyolo.org/openyolo-android-spec.html#credentials">
 *     OpenYOLO Specification: Credential</a>
 */
public final class Credential implements Parcelable, AdditionalPropertiesContainer {

    /**
     * Parcelable reader for {@link Credential} instances.
     * @see android.os.Parcelable
     */
    public static final Creator<Credential> CREATOR = new CredentialCreator();

    /**
     * Creates a credential from its protocol buffer equivalent, in byte form.
     * @throws MalformedDataException if the given protocol buffer is invalid.
     */
    public static Credential fromProtoBytes(@NonNull byte[] credentialProtoBytes)
            throws MalformedDataException {
        validate(credentialProtoBytes, notNullValue(), MalformedDataException.class);

        try {
            return fromProtobuf(Protobufs.Credential.parseFrom(credentialProtoBytes));
        } catch (InvalidProtocolBufferException ex) {
            throw new MalformedDataException(ex);
        }
    }

    /**
     * Creates a credential from its protocol buffer equivalent.
     * @throws MalformedDataException if the given protocol buffer is invalid.
     */
    public static Credential fromProtobuf(Protobufs.Credential credential)
            throws MalformedDataException {
        return new Credential.Builder(credential).build();
    }

    @NonNull
    private final String mId;

    @NonNull
    private final AuthenticationDomain mAuthDomain;

    @NonNull
    private final AuthenticationMethod mAuthMethod;

    @Nullable
    private final String mDisplayName;

    @Nullable
    private final Uri mDisplayPicture;

    @Nullable
    private final String mPassword;

    @Nullable
    private final String mIdToken;

    @NonNull
    private final Map<String, ByteString> mAdditionalProps;

    private Credential(Builder builder) {
        mId = builder.mId;
        mAuthDomain = builder.mAuthDomain;
        mAuthMethod = builder.mAuthMethod;
        mDisplayName = builder.mDisplayName;
        mDisplayPicture = builder.mDisplayPicture;
        mPassword = builder.mPassword;
        mIdToken = builder.mIdToken;
        mAdditionalProps = Collections.unmodifiableMap(builder.mAdditionalProps);
    }

    /**
     * Creates a protocol buffer representation of the credential, for transmission or storage.
     */
    @NonNull
    public Protobufs.Credential toProtobuf() {
        Protobufs.Credential.Builder builder = Protobufs.Credential.newBuilder()
                .setId(mId)
                .setAuthMethod(mAuthMethod.toProtobuf())
                .setAuthDomain(mAuthDomain.toProtobuf())
                .putAllAdditionalProps(mAdditionalProps);

        if (mDisplayName != null) {
            builder.setDisplayName(mDisplayName);
        }

        if (mDisplayPicture != null) {
            builder.setDisplayPictureUri(mDisplayPicture.toString());
        }

        if (mPassword != null) {
            builder.setPassword(mPassword);
        }

        if (mIdToken != null) {
            builder.setIdToken(mIdToken);
        }

        return builder.build();
    }

    /**
     * The credential identifier, which is typically unique within the authentication domain.
     * Must be non-empty.
     */
    @NonNull
    public String getIdentifier() {
        return mId;
    }

    /**
     * The authentication method for the credential, which describes the mechanism by which
     * it should be verified by the receiving app. This may be any valid URI, but is typically
     * one of the {@link AuthenticationMethods standard values}.
     */
    @NonNull
    public AuthenticationMethod getAuthenticationMethod() {
        return mAuthMethod;
    }

    /**
     * The authentication domain against which this credential can be used, if specified.
     */
    @NonNull
    public AuthenticationDomain getAuthenticationDomain() {
        return mAuthDomain;
    }

    /**
     * The display name for the credential, if defined. This is typically the user's
     * full name or an alias for the account that will be recognizable and distinguishable between
     * multiple credentials that the user may have.
     */
    @Nullable
    public String getDisplayName() {
        return mDisplayName;
    }

    /**
     * The display picture for the credential, if defined. This is typically a picture of the
     * user, or some chosen avatar for the account that will be recognizable and distinguishable
     * between multiple credentials that the user may have.
     */
    @Nullable
    public Uri getDisplayPicture() {
        return mDisplayPicture;
    }

    /**
     * The credential password, if defined. Must be non-empty, but any further restrictions are
     * the responsibility of the authentication domain to define and validate.
     */
    @Nullable
    public String getPassword() {
        return mPassword;
    }

    /**
     * An ID token which provides "proof of access" to the identifier for this credential, if
     * available.
     */
    @Nullable
    public String getIdToken() {
        return mIdToken;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        byte[] encoded = toProtobuf().toByteArray();
        dest.writeInt(encoded.length);
        dest.writeByteArray(encoded);
    }

    /**
     * Creates instances of {@link Credential}.
     */
    public static final class Builder implements AdditionalPropertiesBuilder<Credential, Builder> {

        private String mId;
        private AuthenticationMethod mAuthMethod;
        private AuthenticationDomain mAuthDomain;
        private String mDisplayName;
        private Uri mDisplayPicture;
        private String mPassword;
        private String mIdToken;
        private Map<String, ByteString> mAdditionalProps = new HashMap<>();

        /**
         * Starts the process of creating a credential, based on the properties of the
         * provided protocol buffer representation of the credential. The protocol buffer
         * must contain valid data for a credential.
         * @throws MalformedDataException if the given protocol buffer is invalid.
         */
        private Builder(@NonNull Protobufs.Credential proto) throws MalformedDataException {
            try {
                // required properties
                setIdentifier(proto.getId());
                setAuthenticationMethod(AuthenticationMethod.fromProtobuf(proto.getAuthMethod()));
                setAuthenticationDomain(AuthenticationDomain.fromProtobuf(proto.getAuthDomain()));

                // optional properties

                setIdToken(proto.getIdToken());
                setDisplayName(proto.getDisplayName());
                setDisplayPicture(proto.getDisplayPictureUri());
                setPassword(proto.getPassword());
                setAdditionalProperties(
                        CollectionConverter.convertMapValues(
                                proto.getAdditionalPropsMap(),
                                ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY));
            } catch (IllegalArgumentException ex) {
                throw new MalformedDataException(ex);
            }
        }

        /**
         * Starts the process of creating a credential, with the mandatory identifier,
         * authentication method and authentication domain. To create a credential for your
         * own application, do:
         *
         * <pre>{@code
         * Credential cr = new Credential.Builder(
         *     id,
         *     authMethod,
         *     AuthenticationDomain.getSelfAuthDomain(context))
         *     .setPassword(pass)
         *     ...
         *     .build();
         * }</pre>
         * @param identifier the identifier for the credential. Must not be null or empty.
         */
        public Builder(
                @NonNull String identifier,
                @NonNull AuthenticationMethod authenticationMethod,
                @NonNull AuthenticationDomain authenticationDomain) {
            setIdentifier(identifier);
            setAuthenticationMethod(authenticationMethod);
            setAuthenticationDomain(authenticationDomain);
        }

        /**
         * Starts the process of creating a credential, with the mandatory identifier,
         * authentication method (as a string) and authentication domain (as a string). To create
         * a credential for your own application.
         *
         * @see #Builder(String, AuthenticationMethod, AuthenticationDomain)
         */
        public Builder(
                @NonNull String identifier,
                @NonNull String authenticationMethod,
                @NonNull String authenticationDomain) {
            this(identifier,
                    new AuthenticationMethod(authenticationMethod),
                    new AuthenticationDomain(authenticationDomain));
        }

        /**
         * Specifies the identifier for the credential. The provided value must be non-null
         * and non-empty.
         */
        @NonNull
        public Builder setIdentifier(@NonNull String identifier) {
            validate(identifier, notNullOrEmptyString(), IllegalArgumentException.class);
            mId = identifier;
            return this;
        }

        /**
         * Specifies the authentication domain against which this credential is valid. The provided
         * value must be non-null.
         */
        @NonNull
        public Builder setAuthenticationDomain(@NonNull AuthenticationDomain domain) {
            validate(domain, notNullValue(), IllegalArgumentException.class);
            mAuthDomain = domain;
            return this;
        }

        /**
         * Specifies the authentication method for verifying the credential. This may be any
         * URI of form {@code scheme://authority}, but is typically one of the
         * {@link AuthenticationMethods standard values}.
         */
        @NonNull
        public Builder setAuthenticationMethod(@NonNull AuthenticationMethod authMethod) {
            validate(authMethod, notNullValue(), IllegalArgumentException.class);
            mAuthMethod = authMethod;
            return this;
        }

        /**
         * Specifies the password for the credential. If a null or empty value is provided, this
         * is equivalent to removing the password from the credential.
         */
        @NonNull
        public Builder setPassword(@Nullable String password) {
            mPassword = nullifyEmptyString(password);
            return this;
        }

        /**
         * Specifies the ID token for the credential. If a null or empty value is provided, this
         * is equivalent to remove the ID token from the credential.
         */
        @NonNull
        public Builder setIdToken(String idToken) {
            this.mIdToken = nullifyEmptyString(idToken);
            return this;
        }

        /**
         * Specifies the display name for the credential. If a null or empty value is provided,
         * this is equivalent to removing the display name from the credential.
         */
        @NonNull
        public Builder setDisplayName(@Nullable String displayName) {
            mDisplayName = nullifyEmptyString(displayName);
            return this;
        }

        /**
         * Specifies the display picture for the credential. If a null value is provided, this
         * is equivalent to removing the display picture from the credential. If a non-null
         * value is provided, it must be an HTTP or HTTPS URI to an image resource.
         */
        @NonNull
        public Builder setDisplayPicture(@Nullable Uri displayPicture) {
            validate(displayPicture, nullOr(isWebUri()), IllegalArgumentException.class);
            mDisplayPicture = displayPicture;
            return this;
        }

        /**
         * Specifies the display picture for the credential, in string form for convenience.
         * @see #setDisplayPicture(Uri)
         */
        @NonNull
        public Builder setDisplayPicture(@Nullable String displayPicture) {
            return setDisplayPicture(
                    nullifyEmptyString(displayPicture) != null ? Uri.parse(displayPicture) : null);
        }

        /**
         * Specifies any additional, non-standard properties associated with the credential.
         */
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
         * Creates the {@link Credential credential} instance with all properties specified.
         */
        @Override
        @NonNull
        public Credential build() {
            return new Credential(this);
        }
    }

    private static final class CredentialCreator implements Creator<Credential> {
        @Override
        public Credential createFromParcel(Parcel source) {
            int encodedLength = source.readInt();
            byte[] encodedBytes = new byte[encodedLength];
            source.readByteArray(encodedBytes);

            try {
                return Credential.fromProtoBytes(encodedBytes);
            } catch (MalformedDataException ex) {
                throw new IllegalStateException("Unable to decode credential proto", ex);
            }
        }

        @Override
        public Credential[] newArray(int size) {
            return new Credential[0];
        }
    }
}
