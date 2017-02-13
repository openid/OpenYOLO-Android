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

import java.util.List;
import org.json.JSONObject;

/**
 * Interface for asset statement deserializers.
 *
 * @param <T> Type of asset statement being created
 */
public interface Deserializer<T extends AssetStatement> {
    /**
     * Deserialize the provided JSON into a list of {@link AssetStatement}s.
     *
     * @param json Asset statements JSON
     * @return A list of {@link AssetStatement}s or an empty list if no valid asset statements
     *      are found
     */
    List<T> deserialize(JSONObject json);
}
