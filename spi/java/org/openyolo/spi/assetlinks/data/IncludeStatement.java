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
import java.util.Collections;
import java.util.List;

/**
 * Represents an include asset statement.
 */
public class IncludeStatement implements AssetStatement {
    private String mInclude;

    private IncludeStatement(Builder builder) {
        this.mInclude = builder.mUrl;
    }

    public String getInclude() {
        return mInclude;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        IncludeStatement that = (IncludeStatement) obj;

        return mInclude.equals(that.mInclude);

    }

    @Override
    public int hashCode() {
        return mInclude.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IncludeStatement{");
        sb.append("mInclude='").append(mInclude).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public List<RelationType> getRelations() {
        return Collections.emptyList();
    }

    /**
     * Builder for creating {@link IncludeStatement}.
     */
    public static final class Builder {
        private String mUrl;

        /**
         * Set the URL for the include.
         *
         * @param url The URL
         * @return The builder
         */
        public Builder url(@NonNull String url) {
            this.mUrl = url;
            return this;
        }

        /**
         * Create a new {@link IncludeStatement}.
         *
         * @return New {@link IncludeStatement}
         */
        public IncludeStatement build() {
            require(this.mUrl, notNullValue());
            return new IncludeStatement(this);
        }
    }

}
