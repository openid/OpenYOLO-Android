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

package org.openyolo.spi.assetlinks.loader;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.valid4j.Assertive.require;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openyolo.spi.AssetStatementsUtil;
import org.openyolo.spi.assetlinks.data.AndroidAssetStatement;
import org.openyolo.spi.assetlinks.data.AssetStatement;
import org.openyolo.spi.assetlinks.data.AssetStatementsFactory;
import org.openyolo.spi.assetlinks.data.RelationType;

/**
 * <p>This class loads the asset statement(s) from the Android app identified in the provided
 * <code>AndroidAssetStatement</code>.</p>
 */
@SuppressLint("PackageManagerGetSignatures")
public class TargetAndroidAssetStatementLoader {
    private static final String TAG = "AndroidAssetStatementLo";

    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
    private static final String DIGEST_SHA_256 = "SHA-256";

    private static final int HEX_0F = 0x0F;
    private static final int HEX_FF = 0xFF;
    private static final int FOUR = 4;

    private final AndroidAssetStatement mSourceAssetStatement;
    private final Context mContext;

    /**
     * <p>Create an instance of the target asset statement loader.</p>
     *
     * <p>The asset statements from the Android app identified by
     * {@link AndroidAssetStatement#getTarget()} in the <code>sourceAssetStatement</code> will be
     * retrieved and returned if the package name and cert-fingerprint match and if it has the
     * relation for sharing sign-in credentials.
     *</p>
     *
     * @param context Context
     * @param sourceAssetStatement The 'source' Digital Asset Link asset statement from a
     *     requesting client app.
     */
    public TargetAndroidAssetStatementLoader(@NonNull Context context,
                @NonNull AndroidAssetStatement sourceAssetStatement) {
        require(context, notNullValue());
        require(sourceAssetStatement, notNullValue());
        require(sourceAssetStatement.getTarget(), notNullValue());

        this.mContext = context.getApplicationContext();
        this.mSourceAssetStatement = sourceAssetStatement;
    }

    /**
     * <p>Load the 'target' (Android) {@link AssetStatement}s that are referenced in the 'source'
     * app's asset statements with the relation for sharing sign-in credentials.
     * </p>
     *
     * <p>
     * The following steps are performed:
     * <ol>
     * <li>Verify the source app has the relation to share sign-in credentials</li>
     * <li>Verify an Android app (the "target") is installed on the device with the target app
     * package name
     * and
     * cert fingerprint, and it has the relation for sharing sign-in credentials</li>
     * </ol>
     * If those checks are successful, the asset statements (if any) from the "target" Android will
     * be returned.  Otherwise an empty list will be returned.
     * </p>
     *
     * @return A List of (Android) {@link AssetStatement}s that passed the verification or an
     *     empty list
     */
    @NonNull
    public List<AssetStatement> getRelatedAssetStatements() {
        // verify the source asset statement has the relation for sharing sign-in credentials
        if (!containsRelation(mSourceAssetStatement, RelationType.GetLoginCreds)) {
            // TODO remove before released
            Log.d(TAG, "no asset statements found from "
                        + mSourceAssetStatement.getTarget().getPackageName()
                        + " for sharing credentials");
            return Collections.emptyList();
        }

        // see if an app exists with the target package name and cert-fingerprint
        final PackageInfo targetPackageInfo = getPackageInfo(mContext,
                    mSourceAssetStatement.getTarget().getPackageName(),
                    mSourceAssetStatement.getTarget().getSha256CertFingerprint());

        // if no app was found with the target package name and fingerprint, the target app
        // doesn't exist
        if (targetPackageInfo == null) {
            // TODO remove before released
            Log.d(TAG, "no app found with package name "
                        + mSourceAssetStatement.getTarget().getPackageName()
                        + " and cert fingerprint matching "
                        + mSourceAssetStatement.getTarget().getSha256CertFingerprint());
            return Collections.emptyList();
        }

        // get the asset statements from the target app
        final String targetAssetStmtsStr = AssetStatementsUtil.getAssetStatements(mContext,
                    mSourceAssetStatement.getTarget().getPackageName());
        if (TextUtils.isEmpty(targetAssetStmtsStr)) {
            // TODO remove before released
            Log.d(TAG, "no asset statements found for package name "
                        + mSourceAssetStatement.getTarget().getPackageName());
            return Collections.emptyList();
        }

        // get the asset statements from the target app defined in the source/client app's
        // asset_statements
        final List<AssetStatement> targetAssetStatements = parseAssetStatements
                    (targetAssetStmtsStr);
        if (targetAssetStatements.isEmpty()) {
            // TODO remove before released
            Log.d(TAG, "target app " + mSourceAssetStatement.getTarget().getPackageName()
                        + " contains no asset statements");
            return Collections.emptyList();
        }

        // filter out asset statements with non-Android app descriptors
        final List<AssetStatement> androidTargetAssetStmts
                    = filterAssetStatements(targetAssetStatements, AndroidAssetStatement.class);
        if (androidTargetAssetStmts.isEmpty()) {
            // TODO remove before released
            Log.d(TAG, "target app " + mSourceAssetStatement.getTarget().getPackageName()
                        + " contains no Android asset statements");
            return Collections.emptyList();
        }

        // filter out asset statements that done have the relation for sharing sign-in credentials
        final List<AssetStatement> androidLoginCredsAssetStmts
                    = filterAssetStatements(androidTargetAssetStmts,
                    RelationType.GetLoginCreds);
        if (androidLoginCredsAssetStmts.isEmpty()) {
            // TODO remove before released
            Log.d(TAG, "target app " + mSourceAssetStatement.getTarget().getPackageName()
                        + " contains no asset statements for sharing credentials");
        }
        return androidLoginCredsAssetStmts;
    }

    /**
     * Get the {@link PackageInfo} for the specified <code>packageName</code> if the fingerprint
     * of the signing-cert matches the provided <code>certFingerprint</code>.
     *
     * @param context Context
     * @param packageName App package name
     * @param certFingerprint Hex, SHA-256 of the app signature (no dots)
     * @return The {@link PackageInfo} of the specified app or null if the {@link PackageManager}
     *     can't find the app or if signing signatures have the correct fingerprint.
     */
    @Nullable
    static PackageInfo getPackageInfo(@NonNull Context context, @NonNull String
                packageName, @NonNull String certFingerprint) {
        require(context, notNullValue());
        require(!TextUtils.isEmpty(packageName), "packageName must not be null or empty");
        require(!TextUtils.isEmpty(certFingerprint), "certFingerprint must not be null or empty");

        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager
                        .GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        return containsFingerprint(packageInfo, certFingerprint) ? packageInfo : null;
    }

    /**
     * Check if the {@link PackageInfo} app contains the specified cert fingerprint.
     *
     * @param packageInfo PackageInfo (retrieved with
     * {@link PackageManager#GET_SIGNATURES}
     * @param certFingerprint Hex, SHA-256 of the app signature (no dots)
     *
     * @return Whether the specified app contains the cert fingerprint
     */
    static boolean containsFingerprint(@NonNull PackageInfo packageInfo,
                @NonNull final String certFingerprint) {

        require(packageInfo, notNullValue());
        require(!TextUtils.isEmpty(certFingerprint), "certFingerprint must not be null or empty");

        final String paramFingerprint = certFingerprint.replaceAll(":", "");

        for (Signature signature : packageInfo.signatures) {
            String fingerprint = toHex(sha256(signature.toByteArray()));
            if (fingerprint.equals(paramFingerprint)) {
                return true;
            }
        }
        return false;
    }

    static byte[] sha256(final byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(DIGEST_SHA_256);
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Platform does not support" + DIGEST_SHA_256 + " "
                        + "hashing");
        }
    }

    static String toHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int indx = bytes[i] & HEX_FF;
            hexChars[i * 2] = HEX_DIGITS[indx >>> FOUR];
            hexChars[i * 2 + 1] = HEX_DIGITS[indx & HEX_0F];
        }
        return new String(hexChars);
    }

    /**
     * Check if the {@link AssetStatement} contains the specified {@link RelationType}.
     *
     * @param assetStatement Asset statement to be checked for the relation
     * @param relationType Relation to check for
     * @return True if the asset statement contains the relation, otherwise false
     */
    static boolean containsRelation(@NonNull final AssetStatement assetStatement,
                @NonNull final RelationType relationType) {

        require(assetStatement, notNullValue());
        require(relationType, notNullValue());

        if (assetStatement.getRelations() == null || assetStatement.getRelations().isEmpty()) {
            return false;
        }
        return assetStatement.getRelations().contains(relationType);
    }

    /**
     * From the provided list of {@link AssetStatement}s, return those containing the specified
     * {@link RelationType}.
     *
     * @param assetStatements Asset statements from with the return List will be generated
     * @param relationType Asset statements must have this relation type to be included in the
     *     output.
     * @return List of asset statements containing the specified relation type, or empty
     */
    @NonNull
    static List<AssetStatement> filterAssetStatements(@NonNull final List<AssetStatement>
                assetStatements,
                @NonNull final RelationType relationType) {

        require(assetStatements, notNullValue());
        require(relationType, notNullValue());

        List<AssetStatement> outputStatements = new ArrayList<>();
        for (AssetStatement stmt : assetStatements) {
            if (containsRelation(stmt, relationType)) {
                outputStatements.add(stmt);
            }
        }
        return outputStatements;
    }

    /**
     * From the provided list of {@link AssetStatement}s, return those that are an instance of
     * {@link Class}.
     *
     * @param assetStatements Asset statements from with the return List will be generated
     * @param clazz Asset statements must be of this type to be included in the
     *     output.
     * @return List of asset statements of the specified relation type, or empty
     */
    @NonNull
    static List<AssetStatement> filterAssetStatements(@NonNull final List<AssetStatement>
                assetStatements, Class clazz) {

        require(assetStatements, notNullValue());

        List<AssetStatement> outputStatements = new ArrayList<>();
        for (AssetStatement assetStatement : assetStatements) {
            if (assetStatement.getClass() == clazz) {
                outputStatements.add(assetStatement);
            }
        }
        return outputStatements;
    }

    /**
     * Parse the provided input into a {@link List} of {@link AssetStatement}s using
     * {@link AssetStatementsFactory}.
     *
     * @param assetStatements Input asset statements JSON
     * @return List of {@link AssetStatement}s
     */
    @NonNull
    static List<AssetStatement> parseAssetStatements(@NonNull final String assetStatements) {

        require(!TextUtils.isEmpty(assetStatements), "assetStatements must not be null or empty");

        if (TextUtils.isEmpty(assetStatements)) {
            return Collections.emptyList();
        }
        return AssetStatementsFactory.INSTANCE.createAssetStatements(assetStatements);
    }

}
