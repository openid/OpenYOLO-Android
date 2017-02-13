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

/**
 * Returns the provided value as-is. Useful for performing collection type conversion where
 * the values do not change.
 */
public final class NoopValueConverter<T> implements ValueConverter<T, T> {

    private static final NoopValueConverter INSTANCE = new NoopValueConverter();

    /**
     * The singleton instance of {@link NoopValueConverter}.
     */
    public static <T> NoopValueConverter<T> getInstance() {
        return INSTANCE;
    }

    @Override
    public T convert(T value) {
        return value;
    }
}
