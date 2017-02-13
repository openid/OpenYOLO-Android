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

import android.support.annotation.Nullable;

/**
 * Represents an asset statement relation.
 */
public enum RelationType {
    /**
     * Relation to handle all URLs.
     */
    HandleAllUrls("delegate_permission/common.handle_all_urls"),

    /**
     * Relation to share sign-in credentials.
     */
    GetLoginCreds("delegate_permission/common.get_login_creds");

    private final String mDescription;

    RelationType(String desc) {
        this.mDescription = desc;
    }

    /**
     * Get the relation type description.
     *
     * @return The relation type string.
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Get the {@link NamespaceType} with the matching description or
     * null.
     *
     * @param desc The {@link RelationType} description.
     * @return The {@link RelationType} with the matching
     * {@link #getDescription()} } or null if a match isn't found.
     */
    @Nullable
    public static RelationType getRelationType(String desc) {
        for (RelationType relationType : values()) {
            if (relationType.getDescription().equals(desc)) {
                return relationType;
            }
        }
        return null;
    }
}
