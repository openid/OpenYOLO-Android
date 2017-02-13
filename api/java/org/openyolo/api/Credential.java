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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.openyolo.api.internal.CustomMatchers.isValidAuthenticationMethod;
import static org.openyolo.api.internal.CustomMatchers.isWebUri;
import static org.openyolo.api.internal.CustomMatchers.nullOr;
import static org.valid4j.Assertive.require;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.openyolo.api.internal.CollectionConverter;
import org.openyolo.api.internal.KeyValuePairConverters;
import org.openyolo.proto.KeyValuePair;

/**
 * A representation of a credential for use in sign-in or sign-up. This provides a
 * higher-level, data-verifying wrapper for the underlying {@link org.openyolo.proto.Credential
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
    private final org.openyolo.proto.Credential mProto;

    /**
     * Deserializes and validates a credential protocol buffer.
     * @throws IOException if the credential is invalid.
     */
    public static Credential fromProtoBytes(byte[] credentialProtoBytes) throws IOException {
        return new Credential.Builder(
                org.openyolo.proto.Credential.ADAPTER.decode(credentialProtoBytes))
                .build();
    }

    private Credential(@NonNull org.openyolo.proto.Credential proto) {
        mProto = proto;
    }

    /**
     * The underlying protocol buffer form of the credential.
     */
    public org.openyolo.proto.Credential getProto() {
        return mProto;
    }

    /**
     * The credential identifier, which is typically unique within the authentication domain.
     * Must be non-empty.
     */
    @NonNull
    public String getIdentifier() {
        return mProto.id;
    }

    /**
     * The authentication domain against which this credential can be used, if specified.
     */
    @NonNull
    public AuthenticationDomain getAuthenticationDomain() {
        return new AuthenticationDomain(mProto.authDomain);
    }

    /**
     * The credential password, if defined. Must be non-empty, but any further restrictions are
     * the responsibility of the authentication domain to define and validate.
     */
    @Nullable
    public String getPassword() {
        return mProto.password;
    }

    /**
     * The display name for the credential, if defined. This is typically the user's
     * full name or an alias for the account that will be recognizable and distinguishable between
     * multiple credentials that the user may have.
     */
    @Nullable
    public String getDisplayName() {
        return mProto.displayName;
    }

    /**
     * The display picture for the credential, if defined. This is typically a picture of the
     * user, or some chosen avatar for the account that will be recognizable and distinguishable
     * between multiple credentials that the user may have.
     */
    @Nullable
    public Uri getDisplayPicture() {
        return (mProto.displayPictureUri != null)
                ? Uri.parse(mProto.displayPictureUri)
                : null;
    }

    /**
     * The authentication method for the credential, which describes the mechanism by which
     * it should be verified by the receiving app. This may be any valid URI, but is typically
     * one of the {@link AuthenticationMethods standard values}.
     */
    @NonNull
    public Uri getAuthenticationMethod() {
        return Uri.parse(mProto.authMethod);
    }

    /**
     * Additional, non-standard properties associated with the credential.
     */
    @NonNull
    public Map<String, byte[]> getAdditionalProperties() {
        Map<String, byte[]> additionalProps = new HashMap<>();

        for (KeyValuePair pair : mProto.additionalProps) {
            byte[] value = pair.value != null ? pair.value.toByteArray() : null;
            additionalProps.put(pair.name, value);
        }

        return additionalProps;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        byte[] encoded = org.openyolo.proto.Credential.ADAPTER.encode(mProto);
        dest.writeInt(encoded.length);
        dest.writeByteArray(encoded);
    }

    /**
     * Creates instances of {@link Credential}.
     */
    public static final class Builder {

        private org.openyolo.proto.Credential.Builder mProtoBuilder;

        /**
         * Starts the process of creating a credential, based on the properties of the
         * provided protocol buffer representation of the credential. The protocol buffer
         * must contain valid data for a credential.
         */
        public Builder(@NonNull org.openyolo.proto.Credential proto) {
            mProtoBuilder = new org.openyolo.proto.Credential.Builder();

            // required properties
            setIdentifier(proto.id);
            setAuthenticationDomain(new AuthenticationDomain(proto.authDomain));
            setAuthenticationMethod(proto.authMethod);

            // optional properties
            setDisplayName(proto.displayName);
            setDisplayPicture(proto.displayPictureUri);
            setPassword(proto.password);
            setAdditionalProperties(
                    CollectionConverter.toMap(
                            proto.additionalProps,
                            KeyValuePairConverters.CONVERTER_KVP_TO_PAIR));
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
            mProtoBuilder = new org.openyolo.proto.Credential.Builder();
            setIdentifier(identifier);
            setAuthenticationMethod(authenticationMethod);
            setAuthenticationDomain(authenticationDomain);
        }

        /**
         * Specifies the identifier for the credential. The provided value must not be null or
         * empty.
         */
        @NonNull
        public Builder setIdentifier(@NonNull String identifier) {
            require(!TextUtils.isEmpty(identifier), "identifer must not be null or empty");
            mProtoBuilder.id = identifier;
            return this;
        }

        /**
         * Specifies the authentication domain against which this credential is valid.
         */
        @NonNull
        public Builder setAuthenticationDomain(@NonNull AuthenticationDomain domain) {
            require(domain, notNullValue());
            mProtoBuilder.authDomain = extractString(domain);
            return this;
        }

        /**
         * Specifies the authentication method for verifying the credential. This may be any
         * URI of form {@code scheme://authority}, but is typically one of the
         * {@link AuthenticationMethods standard values}.
         */
        @NonNull
        public Builder setAuthenticationMethod(@NonNull Uri provider) {
            require(provider, isValidAuthenticationMethod());
            mProtoBuilder.authMethod = extractString(provider);
            return this;
        }

        /**
         * Specifies the authentication method for the credential, in string form for convenience.
         * @see #setAuthenticationMethod(Uri)
         */
        @NonNull
        public Builder setAuthenticationMethod(@NonNull String provider) {
            return setAuthenticationMethod(Uri.parse(provider));
        }

        /**
         * Specifies the password for the credential. If a non-null value is provided, it must
         * not be empty.
         */
        @NonNull
        public Builder setPassword(@Nullable String password) {
            if (password != null) {
                require(!TextUtils.isEmpty(password), "password must be null or not empty");
            }
            mProtoBuilder.password = password;
            return this;
        }

        /**
         * Specifies the display name for the credential. If a non-null value is provided, it
         * must not be empty.
         */
        @NonNull
        public Builder setDisplayName(@Nullable String displayName) {
            if (displayName != null) {
                require(!TextUtils.isEmpty(displayName),
                        "display name must be null or not empty");
            }
            mProtoBuilder.displayName = displayName;
            return this;
        }

        /**
         * Specifies the display picture for the credential, which must be a HTTP or HTTPS URI
         * to an image resource.
         */
        @NonNull
        public Builder setDisplayPicture(@Nullable Uri displayPicture) {
            require(displayPicture, nullOr(isWebUri()));
            mProtoBuilder.displayPictureUri = extractString(displayPicture);
            return this;
        }

        /**
         * Specifies the display picture for the credential, in string form for convenience.
         * @see #setDisplayPicture(Uri)
         */
        @NonNull
        public Builder setDisplayPicture(@Nullable String displayPicture) {
            return setDisplayPicture(
                    displayPicture != null ? Uri.parse(displayPicture) : null);
        }

        /**
         * Specifies any additional, non-standard properties associated with the credential.
         */
        public Builder setAdditionalProperties(@Nullable Map<String, byte[]> additionalProps) {
            mProtoBuilder.additionalProps = CollectionConverter.toList(
                    additionalProps.entrySet(),
                    KeyValuePairConverters.CONVERTER_ENTRY_TO_KVP);
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
                org.openyolo.proto.Credential proto =
                        org.openyolo.proto.Credential.ADAPTER.decode(encodedBytes);
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
