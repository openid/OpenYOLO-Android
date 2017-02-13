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
import static org.openyolo.spi.assetlinks.data.DeserializerUtil.getRelations;
import static org.valid4j.Assertive.require;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Deserializer for converting JSON into a list of {@link AndroidAssetStatement}.
 */
public class AndroidAssetStatementDeserializer implements Deserializer<AndroidAssetStatement> {
    private static final String TAG = "AndroidAssetStatementDe";

    @Override
    public List<AndroidAssetStatement> deserialize(@NonNull final JSONObject assetStatementJson) {
        require(assetStatementJson, notNullValue());
        if (!isAndroidAssetStatement(assetStatementJson)) {
            Log.e(TAG, "not a web asset statement");
            return Collections.emptyList();
        }
        return buildAndroidAssetStatements(assetStatementJson);
    }

    private List<AndroidAssetStatement> buildAndroidAssetStatements(final JSONObject
                assetStatementJson) {
        List<AndroidAssetStatement> assetStatements = new ArrayList<>();

        JSONObject target = assetStatementJson.optJSONObject("target");
        if (target == null) {
            Log.e(TAG, "'target' property not found");
            return Collections.emptyList();
        }

        JSONArray sha256CertFingerprints = target.optJSONArray("sha256_cert_fingerprints");
        if (sha256CertFingerprints == null) {
            Log.e(TAG, "'sha256_cert_fingerprints' property not found");
            return Collections.emptyList();
        }

        List<RelationType> relationTypes = getRelations(assetStatementJson);
        if (relationTypes.isEmpty()) {
            Log.e(TAG, "no asset statement relations found");
            return Collections.emptyList(); // asset statement is useless without relations
        }

        // creating a separate AndroidTarget for each cert fingerprint
        for (int i = 0; i < sha256CertFingerprints.length(); i++) {
            AndroidTarget androidTarget = buildAndroidTarget(target, sha256CertFingerprints
                        .optString(i));

            if (androidTarget == null) {
                Log.e(TAG, "unable to create valid android target");
                continue;
            }

            final AndroidAssetStatement assetStatement = new AndroidAssetStatement.Builder()
                        .relations(relationTypes)
                        .target(androidTarget)
                        .build();

            assetStatements.add(assetStatement);
        }
        return assetStatements;
    }

    private AndroidTarget buildAndroidTarget(final JSONObject target, final String fingerprint) {
        String packageName = target.optString("package_name");
        if (TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "package name not found");
            return null;
        }

        if (TextUtils.isEmpty(fingerprint)) {
            Log.e(TAG, "cert fingerprint not defined");
            return null;
        }

        return new AndroidTarget.Builder()
                    .packageName(packageName)
                    .sha256CertFingerprint(fingerprint)
                    .build();
    }

    private boolean isAndroidAssetStatement(final JSONObject jsonObject) {
        JSONObject target = jsonObject.optJSONObject("target");
        if (target == null) {
            return false;
        }
        final String namespace = target.optString("namespace");
        return NamespaceType.AndroidApp.getDescription().equals(namespace);
    }
}
