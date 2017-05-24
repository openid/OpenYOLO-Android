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
import static org.openyolo.protocol.internal.CustomMatchers.isValidAuthenticationDomain;
import static org.openyolo.protocol.internal.CustomMatchers.notNullOrEmptyString;
import static org.valid4j.Validation.validate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A URI-based representation of an authentication domain, which determines the scope within which
 * a credential is valid. An authentication domain may have multiple equivalent representations,
 * such as when apps and sites share the same authentication system. The responsibility for
 * determining whether two given authentication domains are equivalent lies with the credential
 * provider.
 *
 * <p>Two broad classes of authentication domain are defined in the OpenYOLO
 * specification : Android authentication domains, of form
 * {@code android://<signature>@<packageName>}, and Web authentication domains, of form
 * {@code http(s)://host@port}. Formally, authentication domains are absolute hierarchical URIs
 * with no path, query or fragment. They must therefore always be of form
 * {@code scheme://authority}.
 */
@SuppressLint("PackageManagerGetSignatures")
public final class AuthenticationDomain implements Comparable<AuthenticationDomain> {

    private static final String DIGEST_SHA_512 = "SHA-512";
    private static final String SCHEME_ANDROID = "android";
    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";

    private final String mUriStr;

    /**
     * Initially null, created on-demand for each thread from mUriStr.
     */
    private final ThreadLocal<Uri> mParsedUri = new ThreadLocal<>();

    /**
     * Creates an authentication domain that represents the current package, as identified
     * by the provided context's {@link Context#getPackageName() getPackageName} method.
     */
    @NonNull
    public static AuthenticationDomain getSelfAuthDomain(@NonNull Context context) {
        validate(context, notNullValue(), IllegalArgumentException.class);

        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        PackageInfo packageInfo;
        try {
            packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException("Unable to find package info for " + packageName);
        }

        return createAndroidAuthDomain(packageName, packageInfo.signatures[0]);
    }

    /**
     * Creates the list of all authentication domains that represent the specified package.
     * The list will contain a distinct entry for every signature related to the application.
     */
    @NonNull
    public static List<AuthenticationDomain> listForPackage(
            @NonNull Context context,
            @Nullable String packageName) {
        validate(context, notNullValue(), IllegalArgumentException.class);

        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo;
        if (TextUtils.isEmpty(packageName)) {
            return Collections.emptyList();
        }
        try {
            packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            return Collections.emptyList();
        }

        ArrayList<AuthenticationDomain> authDomains =
                new ArrayList<>(packageInfo.signatures.length);
        for (Signature signature : packageInfo.signatures) {
            authDomains.add(createAndroidAuthDomain(packageName, signature));
        }

        return authDomains;
    }

    /**
     * Creates an Android authentication domain (of form {@code android://SIGNATURE@PACKAGE}),
     * given the provided package name and signature.
     */
    @NonNull
    public static AuthenticationDomain createAndroidAuthDomain(
            @NonNull String packageName,
            @NonNull Signature signature) {
        validate(packageName, notNullOrEmptyString(), IllegalArgumentException.class);
        validate(signature, notNullValue(), IllegalArgumentException.class);

        return new AuthenticationDomain(
                new Uri.Builder()
                        .scheme(SCHEME_ANDROID)
                        .encodedAuthority(generateSignatureHash(signature) + "@" + packageName)
                        .build()
                        .toString());
    }

    /**
     * Creates an authentication domain from its protocol buffer equivalent, in byte form.
     * @throws MalformedDataException if the given protocol buffer is invalid.
     */
    @NonNull
    public static AuthenticationDomain fromProtobufBytes(@NonNull byte[] protobufBytes)
            throws MalformedDataException {
        validate(protobufBytes, notNullValue(), MalformedDataException.class);

        try {
            return fromProtobuf(Protobufs.AuthenticationDomain.parseFrom(protobufBytes));
        } catch (IOException ex) {
            throw new MalformedDataException("Unable to parse the given protocol buffer", ex);
        }
    }

    /**
     * Creates an authentication domain from its protocol buffer equivalent.
     * @throws MalformedDataException if the given protocol buffer is invalid.
     */
    @NonNull
    public static AuthenticationDomain fromProtobuf(
            @NonNull Protobufs.AuthenticationDomain authDomain) throws MalformedDataException {
        validate(authDomain, notNullValue(), MalformedDataException.class);

        try {
            return new AuthenticationDomain(authDomain.getUri());
        } catch (IllegalArgumentException ex) {
            throw new MalformedDataException(ex);
        }
    }

    @NonNull
    static String generateSignatureHash(@NonNull Signature signature) {
        try {
            MessageDigest digest = MessageDigest.getInstance(DIGEST_SHA_512);
            byte[] hashBytes = digest.digest(signature.toByteArray());
            return Base64.encodeToString(hashBytes, Base64.URL_SAFE).replace("\n", "");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "Platform does not support" + DIGEST_SHA_512 + " hashing");
        }
    }

    /**
     * Creates an authentication domain from the provided String representation. If the string
     * provided is not a valid authentication domain, an {@link IllegalArgumentException}
     * will be thrown.
     */
    public AuthenticationDomain(@NonNull String authDomainString) {
        mUriStr = validate(
                authDomainString,
                isValidAuthenticationDomain(),
                IllegalArgumentException.class);
    }

    /**
     * Determines whether the authentication domain refers to an Android application.
     */
    public boolean isAndroidAuthDomain() {
        return SCHEME_ANDROID.equals(getParsedUri().getScheme());
    }

    /**
     * Determines whether the authentication domain refers to a Web domain.
     */
    public boolean isWebAuthDomain() {
        Uri parsedUri = getParsedUri();
        return SCHEME_HTTP.equals(parsedUri.getScheme())
                || SCHEME_HTTPS.equals(parsedUri.getScheme());
    }

    /**
     * Retrieves the Android package name from the authentication domain. If the authentication
     * domain does not represent an Android application, an {@link IllegalStateException} will
     * be thrown.
     */
    @NonNull
    public String getAndroidPackageName() {
        if (!isAndroidAuthDomain()) {
            throw new IllegalStateException("Authentication domain is not an Android domain");
        }
        return getParsedUri().getHost();
    }

    /**
     * Creates a protocol buffer representation of the authentication domain, for transmission
     * or storage.
     */
    public Protobufs.AuthenticationDomain toProtobuf() {
        return Protobufs.AuthenticationDomain.newBuilder()
                .setUri(mUriStr)
                .build();
    }

    /**
     * Retrieves the string form of the authentication domain.
     */
    @NonNull
    public String toString() {
        return mUriStr;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AuthenticationDomain)) {
            return false;
        }

        return compareTo((AuthenticationDomain) obj) == 0;
    }

    @Override
    public int hashCode() {
        return mUriStr.hashCode();
    }

    @Override
    public int compareTo(@NonNull AuthenticationDomain authenticationDomain) {
        return mUriStr.compareTo(authenticationDomain.mUriStr);
    }

    private Uri getParsedUri() {
        Uri parsedUri = mParsedUri.get();
        if (parsedUri == null) {
            parsedUri = Uri.parse(mUriStr);
            mParsedUri.set(parsedUri);
        }

        return parsedUri;
    }
}
