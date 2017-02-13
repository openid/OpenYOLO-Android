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
 * Used to represent the different types of namespaces in a Digital Asset Links.
 */
public enum NamespaceType {

    /**
     * Web asset namespace.
     */
    Web("web"),

    /**
     * Android asset namespace.
     */
    AndroidApp("android_app");

    private final String mDescription;

    NamespaceType(String desc) {
        this.mDescription = desc;
    }

    public String getDescription() {
        return mDescription;
    }

    /**
     * Get the {@link NamespaceType} with the matching description or
     * null.
     *
     * @param desc The {@link NamespaceType} mDescription.
     * @return The {@link NamespaceType} with the matching
     * {@link #getDescription()} } or null if a match isn't found.
     */
    @Nullable
    public static NamespaceType getNamespaceType(String desc) {
        for (NamespaceType nsType : values()) {
            if (nsType.mDescription.equals(desc)) {
                return nsType;
            }
        }
        return null;
    }
}
