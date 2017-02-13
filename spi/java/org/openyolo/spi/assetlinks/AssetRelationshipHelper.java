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

package org.openyolo.spi.assetlinks;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.valid4j.Assertive.require;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openyolo.api.AuthenticationDomain;
import org.openyolo.spi.assetlinks.data.AndroidAssetStatement;
import org.openyolo.spi.assetlinks.data.AssetStatement;

/**
 * <p>
 * This class loads the {@link org.openyolo.api.AuthenticationDomain}s for the 'source'
 * (client) and 'target' asset statements and finds the ones that are bidirectional.  That is,
 * where the source and target apps grant the same relations to each other.
 * </p>
 *
 * <p>Example for looking-up bidirectional relationships:</p>
 *
 * <pre>
 * {@code
 *
 * private void loadRelatedAuthDomains(Context context, String requestedAppPackage) {
 *      // get the asset statements for the requesting/client app
 *      List<AssetStatement> sourceAssetStatements = getSourceAssetStatements
 *               (context, requestedAppPackage);
 *
 *      TargetAssetStatementLoader assetStatementLoader = new
 *               TargetAssetStatementLoader(context, sourceAssetStatements);
 *
 *      // get all 'related' target asset statements - those referenced in the client app's
 *      // asset statements
 *      List<AssetStatement> targetAssetStatements = assetStatementLoader
 *               .getRelatedAssetStatements();
 *
 *      AssetRelationshipHelper assetRelationshipHelper = new
 *               AssetRelationshipHelper(context, requestedAppPackage, sourceAssetStatements,
 *                targetAssetStatements);
 *
 *      List<AuthenticationDomain> bidirectionalAuthDomains =
 *               assetRelationshipHelper.getBidirectionalAuthDomains();
 * }
 *
 * private List<AssetStatement> getSourceAssetStatements(Context context, String packageName) {
 *      String clientAssetStatements = AssetStatementsUtil.getAssetStatements(context,
 *               packageName);
 *      return parseAssetStatementsJson(clientAssetStatements);
 * }
 *
 * private List<AssetStatement> parseAssetStatementsJson(String json) {
 *      return AssetStatementsFactory.INSTANCE.createAssetStatements(json);
 * }
 *}
 * </pre>
 */
public class AssetRelationshipHelper {
    private static final String TAG = "AssetRelationshipHelper";

    private final Context mContext;
    private final String mClientPackageName;
    private final List<AssetStatement> mSourceAssetStatements;
    private final List<AssetStatement> mTargetAssetStatements;

    /**
     * Setup the helper and validate the input params.
     *
     * @param context Context
     * @param clientPackageName The client app's package name
     * @param sourceAssetStatements Asset statements from the 'source' (client) app
     * @param targetAssetStatements Asset statements from the various 'targets' referenced by the
     *     'source' (client) app
     */
    public AssetRelationshipHelper(@NonNull Context context, @NonNull String clientPackageName,
                @NonNull List<AssetStatement> sourceAssetStatements, @NonNull List<AssetStatement>
                targetAssetStatements) {

        require(context, notNullValue());
        require(!TextUtils.isEmpty(clientPackageName), "mClientPackageName must not be null or "
                    + "empty");
        require(sourceAssetStatements, notNullValue());
        require(targetAssetStatements, notNullValue());

        this.mContext = context.getApplicationContext();
        this.mClientPackageName = clientPackageName;
        this.mSourceAssetStatements = sourceAssetStatements;
        this.mTargetAssetStatements = targetAssetStatements;
    }

    /**
     * <p>
     * Load the asset statements for the client app.  Load the asset statements for each Android
     * 'target' in the client app's asset statements.  Return the of
     * {@link org.openyolo.api.AuthenticationDomain}s that have bidirectional relationships with
     * the client app.
     * </p>
     * <p>
     * Note, this currently only supports Android 'target' asset statements.
     * </p>
     *
     * @return Authentication domains that have a bidirectional relationship with the client
     *     Android app
     */
    public List<AuthenticationDomain> getBidirectionalAuthDomains() {
        if (mSourceAssetStatements.isEmpty() || mTargetAssetStatements.isEmpty()) {
            return Collections.emptyList();
        }

        final List<AuthenticationDomain> bidirectionalAuthDomains = new ArrayList<>();

        bidirectionalAuthDomains.addAll(getBidirectionalAuthDomains
                    (getTargetAndroidAssetStatements()));

        // TODO load authentication domains for web targets

        return bidirectionalAuthDomains;
    }

    private List<AuthenticationDomain> getBidirectionalAuthDomains(
                final List<AndroidAssetStatement> androidAssetStmts) {
        final List<AuthenticationDomain> authDomains = new ArrayList<>();
        for (AndroidAssetStatement androidAssetStmt : androidAssetStmts) {
            authDomains.addAll(getBidirectionalAuthDomains(androidAssetStmt));
        }
        // TODO remove before released
        Log.d(TAG, "Android bidirectional auth domains: " + authDomains);
        return authDomains;
    }

    /**
     * Filter out any authentication domains from the target apps that aren't in the requestor
     * domains, leaving us with the authentication domains that have bidirectional
     * credential sharing relationships.
     *
     * @param targetAssetStatement Asset statement of the 'target' app
     */
    private List<AuthenticationDomain> getBidirectionalAuthDomains(final AndroidAssetStatement
                targetAssetStatement) {

        // get the AuthenticationDomains for the source (client) app
        final List<AuthenticationDomain> sourceAuthDomains = createAuthDomains(
                    mClientPackageName);

        // get the AuthenticationDomains for app identified in the (target) android asset statement
        final List<AuthenticationDomain> targetAuthDomains = createAuthDomains(
                    targetAssetStatement);

        // filter out of the 'target' auth domains any AuthenticationDomains not in the
        // 'source' auth domains
        targetAuthDomains.retainAll(sourceAuthDomains);

        // TODO remove before released
        Log.d(TAG, "bidirectional auth domains for target pkg '" + mClientPackageName + "' and "
                    + "source pkg '" + targetAssetStatement.getTarget().getPackageName() + "': "
                    + targetAuthDomains);

        return targetAuthDomains;
    }

    /**
     * Returns a list of all the {@link org.openyolo.spi.assetlinks.data.AndroidAssetStatement}s in
     * {@link #mTargetAssetStatements}.
     */
    private List<AndroidAssetStatement> getTargetAndroidAssetStatements() {
        List<AndroidAssetStatement> androidAssetStatements = new ArrayList<>();
        for (AssetStatement assetStatement : mTargetAssetStatements) {
            if (assetStatement instanceof AndroidAssetStatement) {
                androidAssetStatements.add((AndroidAssetStatement) assetStatement);
            }
        }
        return androidAssetStatements;
    }

    private List<AuthenticationDomain> createAuthDomains(
                final AndroidAssetStatement androidAssetStatement) {
        return createAuthDomains(androidAssetStatement.getTarget().getPackageName());
    }

    private List<AuthenticationDomain> createAuthDomains(
                final String packageName) {
        return AuthenticationDomain.listForPackage(mContext, packageName);
    }
}
