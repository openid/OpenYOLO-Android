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

package org.openyolo.api.internal;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.valid4j.Assertive.require;

import android.net.Uri;

/**
 * Utility methods for parsing and validating URIs used in OpenYOLO.
 */
public final class UriConverters {

    /**
     * Converts strings to Uri instances.
     */
    public static final ValueConverter<String, Uri> CONVERTER_STRING_TO_URI =
            new ValueConverter<String, Uri>() {
                @Override
                public Uri convert(String value) {
                    require(value, notNullValue());
                    return Uri.parse(value);
                }
            };

    /**
     * Converts Uri instacnes to strings.
     */
    public static final ValueConverter<Uri, String> CONVERTER_URI_TO_STRING =
            new ValueConverter<Uri, String>() {
                @Override
                public String convert(Uri value) {
                    require(value, notNullValue());
                    return value.toString();
                }
            };

    private UriConverters() {
        throw new IllegalStateException("not intended to be constructed");
    }
}
