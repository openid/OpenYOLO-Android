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
import static org.valid4j.Assertive.require;

import android.support.annotation.NonNull;

/**
 * Represents a web asset.
 */
public class WebTarget {

    private static final int PRIME = 31;
    private NamespaceType mNamespace;
    private String mSite;

    private WebTarget(Builder builder) {
        this.mNamespace = builder.mNamespace;
        this.mSite = builder.mSite;
    }

    /**
     * Get the asset descriptor namespace.
     *
     * @return Asset descriptor namespace
     */
    public NamespaceType getNamespace() {
        return mNamespace;
    }

    /**
     * Get the asset descriptor site.
     *
     * @return Asset descriptor site.
     */
    public String getSite() {
        return mSite;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        WebTarget target = (WebTarget) obj;

        if (mNamespace != target.mNamespace) {
            return false;
        }
        return mSite != null ? mSite.equals(target.mSite) : target.mSite == null;

    }

    @Override
    public int hashCode() {
        int result = mNamespace != null ? mNamespace.hashCode() : 0;
        result = PRIME * result + (mSite != null ? mSite.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WebTarget{");
        sb.append("mNamespace=").append(mNamespace);
        sb.append(", mSite='").append(mSite).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /**
     * Builder for creating a {@link AndroidTarget}.
     */
    public static final class Builder {
        private final NamespaceType mNamespace = NamespaceType.Web;
        private String mSite;

        /**
         * Set the site URL.
         *
         * @param site Site URL
         * @return The Builder
         */
        public Builder site(@NonNull String site) {
            this.mSite = site;
            return this;
        }

        /**
         * Generate the {@link AndroidTarget}.
         * @return The {@link AndroidTarget}
         */
        public WebTarget build() {
            require(this.mSite, notNullValue());
            return new WebTarget(this);
        }
    }
}
