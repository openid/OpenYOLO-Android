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

import java.util.List;

public class WebSiteAssetStatement implements AssetStatement {
    private static final int PRIME = 31;
    private List<RelationType> mRelations;
    private WebTarget mTarget;

    private WebSiteAssetStatement(Builder builder) {
        this.mRelations = builder.mRelations;
        this.mTarget = builder.mWebTarget;
    }

    @Override
    public List<RelationType> getRelations() {
        return mRelations;
    }

    public WebTarget getTarget() {
        return mTarget;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        WebSiteAssetStatement that = (WebSiteAssetStatement) obj;

        if (!mRelations.equals(that.mRelations)) {
            return false;
        }
        return mTarget.equals(that.mTarget);

    }

    @Override
    public int hashCode() {
        int result = mRelations.hashCode();
        result = PRIME * result + mTarget.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WebSiteAssetStatement{");
        sb.append("mRelations=").append(mRelations);
        sb.append(", mTarget=").append(mTarget);
        sb.append('}');
        return sb.toString();
    }

    public static final class Builder {
        private WebTarget mWebTarget;
        private List<RelationType> mRelations;

        /**
         * Relations, for a Digital Asset Link.
         *
         * @param relations List of relations
         * @return The Builder
         */
        public Builder relations(@NonNull List<RelationType> relations) {
            this.mRelations = relations;
            return this;
        }

        /**
         * Set the {@link WebTarget}.
         *
         * @param webTarget The web target for the asset statement.
         * @return The Builder
         */
        public Builder webTarget(@NonNull WebTarget webTarget) {
            this.mWebTarget = webTarget;
            return this;
        }

        /**
         * Create a new {@link WebSiteAssetStatement}.
         * @return The new instance.
         */
        public WebSiteAssetStatement build() {
            require(this.mWebTarget, notNullValue());
            require(this.mRelations, notNullValue());
            return new WebSiteAssetStatement(this);
        }
    }
}
