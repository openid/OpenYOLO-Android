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

package org.openyolo.api.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.assertj.core.api.Java6Assertions.assertThat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okio.ByteString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.api.internal.CollectionConverter;
import org.openyolo.api.internal.KeyValuePairConverters;
import org.openyolo.proto.KeyValuePair;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Battery of tests for KeyValuePairConverters
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class KeyValuePairConvertersTest {
    @Test
    public void convertToMap() throws Exception {
        List<KeyValuePair> dataPairs = new ArrayList<>();

        String first = "first";
        String second = "second";
        String third = "third";

        dataPairs.add(new KeyValuePair(first, ByteString.of(first.getBytes())));
        dataPairs.add(new KeyValuePair(second, ByteString.of(second.getBytes())));
        dataPairs.add(new KeyValuePair(third, ByteString.of(third.getBytes())));

        Map<String, byte[]> dataMap =
                CollectionConverter.toMap(dataPairs, KeyValuePairConverters.CONVERTER_KVP_TO_PAIR);

        assertThat(dataMap).hasSize(3);
        assertThat(dataMap).containsOnlyKeys(first, second, third);
        assertThat(dataMap.get(first)).isEqualTo(first.getBytes());
        assertThat(dataMap.get(second)).isEqualTo(second.getBytes());
        assertThat(dataMap.get(third)).isEqualTo(third.getBytes());
    }

    @Test
    public void convertToMap_nullList() throws Exception {
        Map<String, byte[]> dataMap =
                CollectionConverter.toMap(null, KeyValuePairConverters.CONVERTER_KVP_TO_PAIR);
        assertThat(dataMap).isEmpty();
        assertThat(dataMap).isEmpty();
    }

    @Test
    public void convertToList() throws Exception {
        String first = "first";
        String second = "second";
        String third = "third";

        Map<String, byte[]> data = new HashMap<>();
        data.put(first, first.getBytes());
        data.put(second, second.getBytes());
        data.put(third, third.getBytes());

        List<KeyValuePair> pairs =
                CollectionConverter.toList(
                        data.entrySet(),
                        KeyValuePairConverters.CONVERTER_ENTRY_TO_KVP);

        assertThat(pairs).hasSize(3);
        assertThat(pairs).contains(
                new KeyValuePair(first, ByteString.of(first.getBytes())),
                new KeyValuePair(second, ByteString.of(second.getBytes())),
                new KeyValuePair(third, ByteString.of(third.getBytes())));
    }

    public void convertToList_nullEntrySet() throws Exception {
        List<Map.Entry<String, byte[]>> nullEntrySet = null;
        List<KeyValuePair> list =
                CollectionConverter.toList(
                        nullEntrySet,
                        KeyValuePairConverters.CONVERTER_ENTRY_TO_KVP);
        assertThat(list).isEmpty();
    }
}