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

package org.openyolo.api.internal;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods to convert collections to different types, while also potentially changing
 * the types of the values themselves.
 */
public final class CollectionConverter {

    /**
     * Converts all the values in the provided collection, and adds them to a new set.
     */
    @NonNull
    public static <T, U> Set<U> toSet(
            @Nullable Collection<T> values,
            @NonNull ValueConverter<T, U> valueConverter) {
        Set<U> result = createSet(values);
        convert(values, result, valueConverter);
        return result;
    }

    /**
     * Converts all the provided values, and adds them to a new set.
     */
    @NonNull
    public static <T, U> Set<U> toSet(
            @Nullable T firstValue,
            @Nullable T[] additionalValues,
            @NonNull ValueConverter<T, U> valueConverter) {
        Set<U> result = createSet(firstValue, additionalValues);
        convert(firstValue, additionalValues, result, valueConverter);
        return result;
    }

    /**
     * Converts all the values in the provided collection, and adds them to a new list.
     */
    @NonNull
    public static <T, U> List<U> toList(
            @Nullable Collection<T> values,
            @NonNull ValueConverter<T, U> valueConverter) {
        List<U> result = createList(values);
        convert(values, result, valueConverter);
        return result;
    }

    /**
     * Converts all the provided values, and adds them to a new set.
     */
    @NonNull
    public static <T, U> List<U> toList(
            @Nullable T firstValue,
            @Nullable T[] values,
            @NonNull ValueConverter<T, U> valueConverter) {
        List<U> result = createList(firstValue, values);
        convert(firstValue, values, result, valueConverter);
        return result;
    }

    /**
     * Converts all the values in the provided collection, and adds them to a new map.
     */
    @NonNull
    public static <T, U, V> Map<U, V> toMap(
            @Nullable Collection<T> values,
            @NonNull ValueConverter<T, Pair<U, V>> valueConverter) {
        if (values == null) {
            return Collections.emptyMap();
        }

        Map<U, V> result = createMap(values.size());
        for (T value : values) {
            Pair<U, V> converted = valueConverter.convert(value);
            result.put(converted.first, converted.second);
        }

        return result;
    }

    private static <T, U> void convert(
            @Nullable Collection<T> source,
            @NonNull Collection<U> target,
            @NonNull ValueConverter<T, U> valueConverter) {
        if (source == null) {
            return;
        }

        for (T value : source) {
            target.add(valueConverter.convert(value));
        }
    }

    private static <T, U> void convert(
            @Nullable T firstValue,
            @Nullable T[] additionalValues,
            @NonNull Collection<U> target,
            @NonNull ValueConverter<T, U> valueConverter) {
        if (firstValue == null && additionalValues == null) {
            return;
        }

        if (firstValue != null) {
            target.add(valueConverter.convert(firstValue));
        }

        if (additionalValues != null) {
            for (T value : additionalValues) {
                target.add(valueConverter.convert(value));
            }
        }
    }

    @NonNull
    private static <T> Set<T> createSet(Collection collection) {
        return createSet(collection != null ? collection.size() : 0);
    }

    @NonNull
    private static <T, U> Set<T> createSet(U firstValue, U[] values) {
        return createSet((firstValue != null ? 1 : 0)
                + (values != null ? values.length : 0));
    }

    @NonNull
    private static <T> Set<T> createSet(int size) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new ArraySet<>(size);
        } else {
            return new HashSet<>();
        }
    }

    @NonNull
    private static <T, U> List<T> createList(
            @Nullable Collection<U> values) {
        return createList(values != null ? values.size() : 0);
    }

    @NonNull
    private static <T, U> List<T> createList(
            @Nullable U firstValue,
            @Nullable U[] additionalValues) {
        return createList((firstValue != null ? 1 : 0)
                + (additionalValues != null ? additionalValues.length : 0));
    }

    @NonNull
    private static <T> List<T> createList(int size) {
        return new ArrayList<>(size);
    }

    @NonNull
    private static <T, U> Map<T, U> createMap(int size) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new ArrayMap<>(size);
        } else {
            return new HashMap<>();
        }
    }

    private CollectionConverter() {
        throw new IllegalStateException("not intended to be constructed");
    }
}
