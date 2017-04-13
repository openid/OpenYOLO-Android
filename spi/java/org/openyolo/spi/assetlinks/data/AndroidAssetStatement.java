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

/**
 * Java representation of an asset statement for an Android app.
 */
public class AndroidAssetStatement implements AssetStatement {
    private static final int PRIME = 31;
    private final List<RelationType> mRelations;
    private final AndroidTarget mTarget;

    private AndroidAssetStatement(Builder builder) {
        this.mRelations = builder.mRelations;
        this.mTarget = builder.mTarget;
    }

    /**
     * Get the list of asset statement relations.
     *
     * @return Asset statement relations
     */
    @Override
    public List<RelationType> getRelations() {
        return mRelations;
    }

    /**
     * Get the asset statement target app descriptor.
     *
     * @return Asset statement target app descriptor
     */
    public AndroidTarget getTarget() {
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

        AndroidAssetStatement that = (AndroidAssetStatement) obj;

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
        final StringBuilder sb = new StringBuilder("AndroidAssetStatement{");
        sb.append("mRelations=").append(mRelations);
        sb.append(", mTarget=").append(mTarget);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Builder for creating a {@link AndroidAssetStatement.Builder}.
     */
    public static final class Builder {
        private List<RelationType> mRelations;
        private AndroidTarget mTarget;

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
         * The Android target (asset descriptor).
         *
         * @param target Android asset descriptor
         * @return The Builder
         */
        public Builder target(@NonNull AndroidTarget target) {
            this.mTarget = target;
            return this;
        }

        /**
         * Generate a {@link AndroidAssetStatement}.
         *
         * @return The {@link AndroidAssetStatement}
         */
        public AndroidAssetStatement build() {
            require(this.mRelations, notNullValue());
            require(this.mTarget, notNullValue());
            return new AndroidAssetStatement(this);
        }
    }
}
