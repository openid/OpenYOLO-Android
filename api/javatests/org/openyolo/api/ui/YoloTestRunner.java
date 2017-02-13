/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openyolo.api.ui;

import org.junit.runners.model.InitializationError;
import org.openyolo.api.BuildConfig;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;

public class YoloTestRunner extends RobolectricTestRunner {
    public YoloTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        String buildVariant = (BuildConfig.FLAVOR.isEmpty()
                ? "" : BuildConfig.FLAVOR+ "/") + BuildConfig.BUILD_TYPE;
        String intermediatesPath = BuildConfig.class.getResource("")
                .toString().replace("file:", "");
        intermediatesPath = intermediatesPath
                .substring(0, intermediatesPath.indexOf("/classes"));

        System.setProperty("android.package",
                BuildConfig.APPLICATION_ID);
        System.setProperty("android.manifest",
                intermediatesPath + "/manifests/aapt/"
                        + buildVariant + "/AndroidManifest.xml");
        System.setProperty("android.resources",
                intermediatesPath + "/res/merged/" + buildVariant);
        System.setProperty("android.assets",
                intermediatesPath + "/assets/" + buildVariant);
    }

    @Override protected AndroidManifest getAppManifest(Config config) {

        String manifestProperty = System.getProperty("android.manifest");
        String resProperty = System.getProperty("android.resources");
        String assetsProperty = System.getProperty("android.assets");
        AndroidManifest manifest = new AndroidManifest(Fs.fileFromPath(manifestProperty), Fs.fileFromPath(resProperty),
                Fs.fileFromPath(assetsProperty)){
        };
        manifest.setPackageName("org.openyolo.api");
        return manifest;
    }
}