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

import android.content.Context;
import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import org.openyolo.spi.assetlinks.data.AndroidAssetStatement;
import org.openyolo.spi.assetlinks.data.AssetStatement;

/**
 * <p> This class loads the asset statements defined as 'targets' in the source asset statements
 * parameter.  It's basically a wrapper around calling {@link TargetAndroidAssetStatementLoader}
 * to load
 * Android (target) asset statements.
 * </p>
 * <p> Note, currently this class only loads Android (target) asset statements.  Web asset
 * statements not yet supported.
 * </p>
 */
public class TargetAssetStatementLoader {
    private final Context mContext;
    private final List<AssetStatement> mSourceAssetStatements;

    /**
     * Create a 'target' asset statement loader for the given 'source' asset statements.
     *
     * @param context Context
     * @param sourceAssetStatements Asset statements from the 'source' app.  Asset
     *     statements for the 'target' asset descriptors in this list will be retrieved.
     */
    public TargetAssetStatementLoader(@NonNull final Context context,
                @NonNull final List<AssetStatement> sourceAssetStatements) {
        require(context, notNullValue());
        require(sourceAssetStatements, notNullValue());

        this.mContext = context.getApplicationContext();
        this.mSourceAssetStatements = sourceAssetStatements;
    }

    /**
     * <p>Get the 'target' asset statements listed in the 'source' app's asset statements.</p>
     *
     * <p>See
     * {@link TargetAndroidAssetStatementLoader#getRelatedAssetStatements()} for more details on
     * the rules applied.</p>
     *
     * @return List of {@link AssetStatement}s or an empty list.
     */
    public List<AssetStatement> getRelatedAssetStatements() {
        final List<AssetStatement> statements = new ArrayList<>();

        // get asset statements from android apps that are referenced as targets in the source
        // app's asset statements
        statements.addAll(getTargetAndroidAssetStatements(mSourceAssetStatements));

        // get asset statements from websites that are referenced as targets in the source
        // app's asset statements
        //statements.addAll(getTargetWebAssetStatements(mSourceAssetStatements));

        return statements;
    }

    private List<AssetStatement> getTargetAndroidAssetStatements(final List<AssetStatement>
                sourceAssetStatements) {
        final List<AssetStatement> targetAndroidAssetStmts = new ArrayList<>();
        for (AssetStatement sourceAssetStatement : sourceAssetStatements) {
            if (sourceAssetStatement instanceof AndroidAssetStatement) {
                targetAndroidAssetStmts.addAll(getTargetAndroidAssetStatements(
                            (AndroidAssetStatement) sourceAssetStatement));
            }
        }
        return targetAndroidAssetStmts;
    }

    private List<AssetStatement> getTargetAndroidAssetStatements(final AndroidAssetStatement
                androidAssetStatement) {
        return new TargetAndroidAssetStatementLoader(mContext, androidAssetStatement)
                    .getRelatedAssetStatements();
    }

    //private List<AssetStatement> getTargetWebAssetStatements(final List<AssetStatement>
    //        mSourceAssetStatements) {
    //    return Collections.emptyList(); // TODO
    //}
}
