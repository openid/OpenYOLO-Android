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
import static org.openyolo.protocol.internal.CustomMatchers.isValidAuthenticationMethod;
import static org.openyolo.protocol.internal.CustomMatchers.isWebUri;
import static org.openyolo.protocol.internal.CustomMatchers.nullOr;
import static org.openyolo.protocol.internal.StringUtil.nullifyEmptyString;
import static org.valid4j.Assertive.require;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import java.io.IOException;
import java.util.Map;
import org.openyolo.protocol.internal.ByteStringConverters;
import org.openyolo.protocol.internal.CollectionConverter;

/**
 * A representation of a credential for use in sign-in or sign-up. This provides a
 * higher-level, data-verifying wrapper for the underlying {@link Protobufs.Credential
 * protocol buffer} that is returned by the app.
 *
 * <p>Credentials must have an {@link #getIdentifier() identifier},
 * an {@link #getAuthenticationMethod() authentication method} and an
 * {@link #getAuthenticationDomain() authentication domain}, but all other properties are optional.
 */
public final class Credential implements Parcelable {

    /**
     * Parcelable reader for {@link Credential} instances.
     * @see android.os.Parcelable
     */
    public static final Creator<Credential> CREATOR = new CredentialCreator();

    @NonNull
    private final Protobufs.Credential mProto;

    /**
     * Deserializes and validates a credential protocol buffer.
     * @throws IOException if the credential is invalid.
     */
    public static Credential fromProtoBytes(byte[] credentialProtoBytes) throws IOException {
        return new Credential.Builder(
                Protobufs.Credential.parseFrom(credentialProtoBytes))
                .build();
    }

    private Credential(@NonNull Protobufs.Credential proto) {
        mProto = proto;
    }

    /**
     * The underlying protocol buffer form of the credential.
     */
    @NonNull
    public Protobufs.Credential getProto() {
        return mProto;
    }

    /**
     * The credential identifier, which is typically unique within the authentication domain.
     * Must be non-empty.
     */
    @NonNull
    public String getIdentifier() {
        return mProto.getId();
    }

    /**
     * The authentication domain against which this credential can be used, if specified.
     */
    @Nullable
    public AuthenticationDomain getAuthenticationDomain() {
        String authDomainStr = nullifyEmptyString(mProto.getAuthDomain());
        if (authDomainStr == null) {
            return null;
        }

        return new AuthenticationDomain(mProto.getAuthDomain());
    }

    /**
     * The credential password, if defined. Must be non-empty, but any further restrictions are
     * the responsibility of the authentication domain to define and validate.
     */
    @Nullable
    public String getPassword() {
        return nullifyEmptyString(mProto.getPassword());
    }

    /**
     * The display name for the credential, if defined. This is typically the user's
     * full name or an alias for the account that will be recognizable and distinguishable between
     * multiple credentials that the user may have.
     */
    @Nullable
    public String getDisplayName() {
        return nullifyEmptyString(mProto.getDisplayName());
    }

    /**
     * The display picture for the credential, if defined. This is typically a picture of the
     * user, or some chosen avatar for the account that will be recognizable and distinguishable
     * between multiple credentials that the user may have.
     */
    @Nullable
    public Uri getDisplayPicture() {
        String displayPictureUriStr = nullifyEmptyString(mProto.getDisplayPictureUri());
        return (displayPictureUriStr != null)
                ? Uri.parse(displayPictureUriStr)
                : null;
    }

    /**
     * The authentication method for the credential, which describes the mechanism by which
     * it should be verified by the receiving app. This may be any valid URI, but is typically
     * one of the {@link AuthenticationMethods standard values}.
     */
    @NonNull
    public Uri getAuthenticationMethod() {
        return Uri.parse(mProto.getAuthMethod());
    }

    /**
     * Additional, non-standard properties associated with the credential.
     */
    @NonNull
    public Map<String, byte[]> getAdditionalProperties() {
        return CollectionConverter.convertMapValues(
            mProto.getAdditionalPropsMap(),
            ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        byte[] encoded = mProto.toByteArray();
        dest.writeInt(encoded.length);
        dest.writeByteArray(encoded);
    }

    /**
     * Creates instances of {@link Credential}.
     */
    public static final class Builder {

        private Protobufs.Credential.Builder mProtoBuilder;

        /**
         * Starts the process of creating a credential, based on the properties of the
         * provided protocol buffer representation of the credential. The protocol buffer
         * must contain valid data for a credential.
         */
        public Builder(@NonNull Protobufs.Credential proto) {
            mProtoBuilder = Protobufs.Credential.newBuilder();

            // required properties
            setIdentifier(proto.getId());
            setAuthenticationDomain(new AuthenticationDomain(proto.getAuthDomain()));
            setAuthenticationMethod(proto.getAuthMethod());

            // optional properties
            setDisplayName(proto.getDisplayName());
            setDisplayPicture(proto.getDisplayPictureUri());
            setPassword(proto.getPassword());
            setAdditionalProperties(
                    CollectionConverter.convertMapValues(
                            proto.getAdditionalPropsMap(),
                            ByteStringConverters.BYTE_STRING_TO_BYTE_ARRAY));
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
                @NonNull Uri authenticationMethod,
                @NonNull AuthenticationDomain authenticationDomain) {
            mProtoBuilder = Protobufs.Credential.newBuilder();
            setIdentifier(identifier);
            setAuthenticationMethod(authenticationMethod);
            setAuthenticationDomain(authenticationDomain);
        }

        /**
         * Starts the process of creating a credential, with the mandatory identifier,
         * authentication method (as a string) and authentication domain (as a string). To create
         * a credential for your own application.
         *
         * @see #Builder(String, Uri, AuthenticationDomain)
         */
        public Builder(
                @NonNull String identifier,
                @NonNull String authenticationMethod,
                @NonNull String authenticationDomain) {
            this(identifier,
                    Uri.parse(authenticationMethod),
                    new AuthenticationDomain(authenticationDomain));
        }

        /**
         * Specifies the identifier for the credential. The provided value must be non-null
         * and non-empty.
         */
        @NonNull
        public Builder setIdentifier(@NonNull String identifier) {
            require(!TextUtils.isEmpty(identifier), "identifier must not be null or empty");
            mProtoBuilder.setId(identifier);
            return this;
        }

        /**
         * Specifies the authentication domain against which this credential is valid. The provided
         * value must be non-null.
         */
        @NonNull
        public Builder setAuthenticationDomain(@NonNull AuthenticationDomain domain) {
            require(domain, notNullValue());
            mProtoBuilder.setAuthDomain(extractString(domain));
            return this;
        }

        /**
         * Specifies the authentication method for verifying the credential. This may be any
         * URI of form {@code scheme://authority}, but is typically one of the
         * {@link AuthenticationMethods standard values}.
         */
        @NonNull
        public Builder setAuthenticationMethod(@NonNull Uri authMethod) {
            require(authMethod, isValidAuthenticationMethod());
            mProtoBuilder.setAuthMethod(extractString(authMethod));
            return this;
        }

        /**
         * Specifies the authentication method for the credential, in string form for convenience.
         * The provided value must be non-null and non-empty.
         * @see #setAuthenticationMethod(Uri)
         */
        @NonNull
        public Builder setAuthenticationMethod(@NonNull String authMethod) {
            require(!TextUtils.isEmpty(authMethod), "authMethod must not be null or empty");
            return setAuthenticationMethod(Uri.parse(authMethod));
        }

        /**
         * Specifies the password for the credential. If a null or empty value is provided, this
         * is equivalent to removing the password from the credential.
         */
        @NonNull
        public Builder setPassword(@Nullable String password) {
            if (nullifyEmptyString(password) == null) {
                mProtoBuilder.clearPassword();
            } else {
                mProtoBuilder.setPassword(password);
            }

            return this;
        }

        /**
         * Specifies the display name for the credential. If a null or empty value is provided,
         * this is equivalent to removing the display name from the credential.
         */
        @NonNull
        public Builder setDisplayName(@Nullable String displayName) {
            if (nullifyEmptyString(displayName) == null) {
                mProtoBuilder.clearDisplayName();
            } else {
                mProtoBuilder.setDisplayName(displayName);
            }
            return this;
        }

        /**
         * Specifies the display picture for the credential. If a null value is provided, this
         * is equivalent to removing the display picture from the credential. If a non-null
         * value is provided, it must be an HTTP or HTTPS URI to an image resource.
         */
        @NonNull
        public Builder setDisplayPicture(@Nullable Uri displayPicture) {
            require(displayPicture, nullOr(isWebUri()));
            if (displayPicture == null) {
                mProtoBuilder.clearDisplayPictureUri();
            } else {
                mProtoBuilder.setDisplayPictureUri(extractString(displayPicture));
            }
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
        public Builder setAdditionalProperties(@Nullable Map<String, byte[]> additionalProps) {
            if (mProtoBuilder.getAdditionalPropsCount() > 0) {
                mProtoBuilder.clearAdditionalProps();
            }
            mProtoBuilder.putAllAdditionalProps(
                    CollectionConverter.convertMapValues(
                            additionalProps,
                            ByteStringConverters.BYTE_ARRAY_TO_BYTE_STRING));
            return this;
        }

        /**
         * Creates the {@link Credential credential} instance with all properties specified.
         */
        @NonNull
        public Credential build() {
            return new Credential(mProtoBuilder.build());
        }

        private String extractString(@Nullable Object obj) {
            return (obj != null) ? obj.toString() : null;
        }
    }

    static final class CredentialCreator implements Creator<Credential> {
        @Override
        public Credential createFromParcel(Parcel source) {
            int encodedLength = source.readInt();
            byte[] encodedBytes = new byte[encodedLength];
            source.readByteArray(encodedBytes);

            try {
                Protobufs.Credential proto = Protobufs.Credential.parseFrom(encodedBytes);
                return new Credential(proto);
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to decode credential proto", ex);
            }
        }

        @Override
        public Credential[] newArray(int size) {
            return new Credential[0];
        }
    }
}
