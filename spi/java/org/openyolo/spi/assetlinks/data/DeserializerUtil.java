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

import android.util.Patterns;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility methods for deserializing asset statements.
 */
public class DeserializerUtil {
    private DeserializerUtil() {
        throw new IllegalAccessError("do not instantiate this class");
    }

    /**
     * Get a list of {@link RelationType}s from the provided JSON.
     *
     * @param jsonObject JSON asset statement
     * @return List of {@link RelationType}s or an empty list
     */
    public static List<RelationType> getRelations(final JSONObject jsonObject) {
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

    /**
     * Check if a URL is valid, using {@link Patterns#WEB_URL}.
     *
     * @param potentialUrl Potential URL to check
     * @return True if the string is a valid URL, otherwise false
     */
    public static boolean isValidUrl(String potentialUrl) {
        return Patterns.WEB_URL.matcher(potentialUrl).matches();
    }
}
