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
 * <p>Java representation of a target in an Android asset statement.</p>
 *
 * <p>Note: the cert-fingerprint is scalar because a separate
 * {@link AndroidAssetStatement} is created for each cert-fingerprint.
 * This is done as a convenience and for precision when determining whether an asset relationship
 * is bidirectional.
 * </p>
 */
public class AndroidTarget {
    private static final int PRIME = 31;
    private final NamespaceType mNamespace;
    private final String mPackageName;
    private final String mSha256CertFingerprint;

    private AndroidTarget(AndroidTarget.Builder builder) {
        this.mNamespace = builder.mNamespace;
        this.mPackageName = builder.mPackageName;
        this.mSha256CertFingerprint = builder.mSha256CertFingerprint;
    }

    /**
     * Get the target's namespace.
     *
     * @return Namespace
     */
    public NamespaceType getNamespace() {
        return mNamespace;
    }

    /**
     * Returns the target package name.
     *
     * @return package name
     */
    public String getPackageName() {
        return mPackageName;
    }

    /**
     * Returns the target app's cert fingerprint.
     *
     * @return cert fingerprint
     */
    public String getSha256CertFingerprint() {
        return mSha256CertFingerprint;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        AndroidTarget that = (AndroidTarget) obj;

        if (mNamespace != that.mNamespace) {
            return false;
        }
        if (!mPackageName.equals(that.mPackageName)) {
            return false;
        }
        return mSha256CertFingerprint.equals(that.mSha256CertFingerprint);

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AndroidTarget{");
        sb.append("mNamespace=").append(mNamespace);
        sb.append(", mPackageName='").append(mPackageName).append('\'');
        sb.append(", sha256CertFingerprints=").append(mSha256CertFingerprint);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = mNamespace.hashCode();
        result = PRIME * result + mPackageName.hashCode();
        result = PRIME * result + mSha256CertFingerprint.hashCode();
        return result;
    }

    /**
     * Builder for creating a {@link AndroidTarget}.
     */
    public static final class Builder {
        private final NamespaceType mNamespace = NamespaceType.AndroidApp;
        private String mPackageName;
        private String mSha256CertFingerprint;

        /**
         * Set the target app package name.
         *
         * @param packageName App package name
         * @return The Builder
         */
        public Builder packageName(@NonNull String packageName) {
            this.mPackageName = packageName;
            return this;
        }

        /**
         * Set the target's cert fingerprint.
         *
         * @param sha256CertFingerprint The app's cert-fingerprint
         * @return The Builder
         */
        public Builder sha256CertFingerprint(@NonNull String sha256CertFingerprint) {
            this.mSha256CertFingerprint = sha256CertFingerprint;
            return this;
        }

        /**
         * Generate the {@link AndroidTarget}.
         * @return The {@link AndroidTarget}
         */
        public AndroidTarget build() {
            require(this.mPackageName, notNullValue());
            require(this.mSha256CertFingerprint, notNullValue());
            return new AndroidTarget(this);
        }
    }
}
