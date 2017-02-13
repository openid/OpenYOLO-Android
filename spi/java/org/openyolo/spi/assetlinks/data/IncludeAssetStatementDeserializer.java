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

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

/**
 * Deserializer for converting JSON into a list of {@link IncludeStatement}.
 */
public class IncludeAssetStatementDeserializer implements Deserializer<IncludeStatement> {
    private static final String TAG = "IncludeAssetStatementDe";

    @Override
    public List<IncludeStatement> deserialize(@NonNull final JSONObject assetStatementJson) {
        require(assetStatementJson, notNullValue());
        if (isIncludeAssetStatement(assetStatementJson)) {
            IncludeStatement includeStatement = buildIncludeAssetStatement(assetStatementJson);
            if (includeStatement == null) {
                return Collections.emptyList();
            }
            return Collections.singletonList(includeStatement);
        }
        return Collections.emptyList();
    }

    private IncludeStatement buildIncludeAssetStatement(JSONObject assetStatementJson) {
        String url = assetStatementJson.optString("include");
        if (!isValidUrl(url)) {
            Log.e(TAG, "include is not a valid URL");
            return null;
        }

        return new IncludeStatement.Builder()
                    .url(url)
                    .build();
    }

    private boolean isIncludeAssetStatement(JSONObject assetStatementJson) {
        return assetStatementJson != null && assetStatementJson.has("include");
    }
}
