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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Used to create a list of {@link AssetStatement}s from a JSON string.
 */
public enum AssetStatementsFactory {
    /**
     * The instance of the factory.
     */
    INSTANCE;

    private static final String TAG = "AssetStatementsFactory";

    /**
     * Parses the provided asset statements JSON into a list of
     * {@link AssetStatement}s.
     *
     * @param json Asset statements JSON
     * @return A list of {@link AssetStatement}s or an empty list
     */
    @NonNull
    public List<AssetStatement> createAssetStatements(@Nullable String json) {
        if (TextUtils.isEmpty(json)) {
            return Collections.emptyList();
        }

        JSONArray assetStatementsArray = parseJson(json);
        if (assetStatementsArray == null) {
            return Collections.emptyList();
        }

        return parseAssetStatements(assetStatementsArray);
    }

    private List<AssetStatement> parseAssetStatements(@NonNull JSONArray assetStatementsArray) {
        List<AssetStatement> assetStatements = new ArrayList<>();
        for (int i = 0; i < assetStatementsArray.length(); i++) {
            final JSONObject assetStatementJson = assetStatementsArray.optJSONObject(i);
            if (assetStatementJson == null) {
                continue;
            }

            final AssetType assetType = getAssetType(assetStatementJson);
            if (assetType == null) {
                Log.e(TAG, "invalid asset statement type found");
                continue;
            }

            switch (assetType) {
                case Android:
                    assetStatements.addAll(createAndroidAssetStatement(assetStatementJson));
                    break;
                case Web:
                    assetStatements.addAll(createWebAssetStatement(assetStatementJson));
                    break;
                case Include:
                    assetStatements.addAll(createIncludeAssetStatement(assetStatementJson));
                    break;
                default:
                    Log.e(TAG, "invalid asset statement type found");
            }
        }
        return assetStatements;
    }

    private Collection<? extends AssetStatement> createIncludeAssetStatement(JSONObject
                assetStatementJson) {
        return new IncludeAssetStatementDeserializer().deserialize(assetStatementJson);
    }

    private Collection<? extends AssetStatement> createWebAssetStatement(JSONObject
                assetStatementJson) {
        return new WebAssetStatementDeserializer().deserialize(assetStatementJson);
    }

    private Collection<? extends AssetStatement> createAndroidAssetStatement(JSONObject
                assetStatementJson) {
        return new AndroidAssetStatementDeserializer().deserialize(assetStatementJson);
    }

    @Nullable
    private AssetType getAssetType(@NonNull JSONObject assetStatementJson) {
        if (assetStatementJson.has("include")) {
            return AssetType.Include;
        } else {
            JSONObject target = assetStatementJson.optJSONObject("target");
            if (target == null) {
                return null;
            }
            String namespace = target.optString("namespace");
            if (TextUtils.isEmpty(namespace)) {
                return null;
            }
            if (NamespaceType.Web.getDescription().equals(namespace)) {
                return AssetType.Web;
            } else if (NamespaceType.AndroidApp.getDescription().equals(namespace)) {
                return AssetType.Android;
            }
            return null;
        }
    }

    @Nullable
    private JSONArray parseJson(String input) {
        try {
            return new JSONArray(input);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    private enum AssetType {
        Include, Android, Web
    }
}
