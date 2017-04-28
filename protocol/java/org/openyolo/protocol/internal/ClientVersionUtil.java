/*
 * Copyright 2017 The OpenYOLO Authors. All Rights Reserved.
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

package org.openyolo.protocol.internal;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import org.openyolo.protocol.BuildConfig;
import org.openyolo.protocol.Protobufs;

/**
 * Holds the client version that is built from the static configuration.
 */
public final class ClientVersionUtil {

    private static final String VENDOR = "openid.net";

    @NonNull
    private static final Protobufs.ClientVersion BUILD_CLIENT_VERSION =
            Protobufs.ClientVersion.newBuilder()
                    .setVendor(VENDOR)
                    .setMajor(parseVersionPart(BuildConfig.clientVersionMajor))
                    .setMinor(parseVersionPart(BuildConfig.clientVersionMinor))
                    .setPatch(parseVersionPart(BuildConfig.clientVersionPatch))
                    .build();

    private static Protobufs.ClientVersion sClientVersion;

    /**
     * Retrieve the client version, typically sourced from the static build information.
     */
    @NonNull
    public static Protobufs.ClientVersion getClientVersion() {
        Protobufs.ClientVersion clientVersion = sClientVersion;
        if (clientVersion == null) {
            clientVersion = BUILD_CLIENT_VERSION;
            sClientVersion = clientVersion;
        }

        return clientVersion;
    }

    /**
     * Overrides the client version, for testing purposes.
     */
    @VisibleForTesting
    public static void setClientVersion(Protobufs.ClientVersion clientVersion) {
        sClientVersion = clientVersion;
    }

    @VisibleForTesting
    static int parseVersionPart(String versionPart) {
        if (versionPart == null || versionPart.isEmpty()) {
            return 0;
        }

        int versionNum;
        try {
            versionNum = Integer.parseInt(versionPart);
        } catch (NumberFormatException ex) {
            return 0;
        }

        if (versionNum < 0) {
            return 0;
        }

        return versionNum;
    }

}
