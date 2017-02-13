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

package org.openyolo.api;

/**
 * A callback handler for asynchronous call completion.
 * @param <ResultT> The type of value returned when the operation completes normally.
 * @param <ErrorT> The type of value returned when the operation fails unexpectedly.
 */
public interface ResultCallback<ResultT, ErrorT> {
    /**
     * Handles the result. If {@code error} has a non-null value, the operation failed.
     */
    void onComplete(ResultT result, ErrorT error);
}
