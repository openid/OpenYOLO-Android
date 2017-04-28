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

package org.openyolo.protocol.internal;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.valid4j.Assertive.ensure;
import static org.valid4j.Assertive.require;

import android.content.Intent;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.google.protobuf.ByteString;

/**
 * Utilities for manipulating intents for use in protocol buffers.
 */
public final class IntentUtil {

    private IntentUtil() {
        throw new IllegalStateException("not intended to be constructed");
    }

    /**
     * Serializes the provided intent to a byte array.
     */
    @NonNull
    public static byte[] toBytes(@NonNull Intent intent) {
        require(intent, notNullValue());

        Parcel parcel = Parcel.obtain();
        parcel.writeParcelable(intent, 0);
        byte[] intentBytes = parcel.marshall();
        parcel.recycle();

        return intentBytes;
    }

    /**
     * Serializes the provided intent to a ByteString, for use with a protocol buffer.
     */
    public static ByteString toByteString(@NonNull Intent intent) {
        require(intent, notNullValue());
        return ByteString.copyFrom(toBytes(intent));
    }

    /**
     * Deserializes an intent from the provided bytes array.
     */
    @NonNull
    public static Intent fromBytes(@NonNull byte[] intentBytes) {
        require(intentBytes, notNullValue());

        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(intentBytes, 0, intentBytes.length);
        parcel.setDataPosition(0);
        Intent intent = parcel.readParcelable(IntentUtil.class.getClassLoader());
        parcel.recycle();

        ensure(intent, notNullValue());
        return intent;
    }
}
