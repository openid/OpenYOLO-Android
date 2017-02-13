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

package org.openyolo.spi.assetlinks.data;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.openyolo.spi.assetlinks.data.DeserializerUtil.isValidUrl;
import static org.valid4j.Assertive.require;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Deserializer for converting JSON into a list of {@link WebSiteAssetStatement}.
 */
public class WebAssetStatementDeserializer implements Deserializer<WebSiteAssetStatement> {
    private static final String TAG = "WebAssetStatementDeseri";

    @Override
    public List<WebSiteAssetStatement> deserialize(@NonNull final JSONObject assetStatementJson) {
        require(assetStatementJson, notNullValue());
        if (!isWebAssetStatement(assetStatementJson)) {
            Log.e(TAG, "not a web asset statement");
            return Collections.emptyList();

        }
        WebSiteAssetStatement webSiteAssetStatement = buildWebStatement(assetStatementJson);
        if (webSiteAssetStatement == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(webSiteAssetStatement);
    }

    private WebSiteAssetStatement buildWebStatement(JSONObject assetStatementJson) {
        JSONObject target = assetStatementJson.optJSONObject("target");
        if (target == null) {
            Log.e(TAG, "'target' property not found");
            return null;
        }

        List<RelationType> relationTypes = getRelations(assetStatementJson);
        if (relationTypes.isEmpty()) {
            Log.e(TAG, "no asset statement relations found");
            return null;
        }

        final WebTarget webTarget = createWebTarget(target);
        if (webTarget == null) {
            Log.e(TAG, "unable to create web target");
            return null;
        }

        return new WebSiteAssetStatement.Builder()
                    .relations(relationTypes)
                    .webTarget(webTarget)
                    .build();
    }

    private WebTarget createWebTarget(JSONObject targetJson) {
        final String site = targetJson.optString("site");
        if (!isValidUrl(site)) {
            Log.e(TAG, "invalid 'site' value in asset statement");
            return null;
        }

        return new WebTarget.Builder()
                    .site(site)
                    .build();
    }

    private List<RelationType> getRelations(final JSONObject jsonObject) {
        JSONArray relation = jsonObject.optJSONArray("relation");
        if (relation == null) {
            return Collections.emptyList();
        }

        List<RelationType> relationTypes = new ArrayList<>();
        for (int i = 0; i < relation.length(); i++) {
            RelationType relationType = RelationType.getRelationType(relation.optString(i));
            if (relationType != null) {
                relationTypes.add(relationType);
            }
        }
        return relationTypes;
    }

    private boolean isWebAssetStatement(JSONObject jsonObject) {
        JSONObject target = jsonObject.optJSONObject("target");
        if (target == null) {
            Log.e(TAG, "'target' property not found");
            return false;
        }
        final String namespace = target.optString("namespace");
        return NamespaceType.Web.getDescription().equals(namespace);
    }
}
