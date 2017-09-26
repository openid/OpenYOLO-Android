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

package org.openyolo.protocol.internal;

import static junit.framework.Assert.fail;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import android.content.Intent;
import android.os.BadParcelableException;
import android.os.Parcel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.valid4j.errors.RequireViolation;

/**
 * Battery of tests for IntentUtilTest
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class IntentUtilTest {

    @Test(expected = RequireViolation.class)
    public void testToBytes_null() throws Exception {
        IntentUtil.toBytes(null);
    }

    @Test
    public void toBytes() throws Exception {
        Intent intent = new Intent("com.openyolo");
        byte[] result = IntentUtil.toBytes(intent);
        assertEquals(result.length, 235);
        //comparing a few random locations
        assertEquals(result[0], -84);
        assertEquals(result[8], 0);
        assertEquals(result[16], 22);
        assertEquals(result[32], 46);
        assertEquals(result[64], 0);
        assertEquals(result[128], -107);
        assertEquals(result[234], -1);
    }

    @Test
    public void fromBytes() throws Exception {
        Intent fromIntent = new Intent("com.openyolo.ACTION");
        fromIntent.setPackage("com.openyolo");
        byte[] fromBytes = IntentUtil.toBytes(fromIntent);

        Intent toIntent = IntentUtil.fromBytes(fromBytes);
        assertTrue(toIntent.getAction().equals(fromIntent.getAction()));
        assertTrue(toIntent.getPackage().equals(fromIntent.getPackage()));
    }

    @Test
    public void fromBytes_withNullIntent_throwsBadParcelableException() {
        byte[] intentBytes = toBytesUnchecked(null);


        assertThrows(BadParcelableException.class, () -> {
            IntentUtil.fromBytes(intentBytes);
        });
    }

    @Test
    public void fromBytes_withIntentNotPresent_throwsBadParcelableException() {
        byte[] intentBytes = new byte[10];


        assertThrows(BadParcelableException.class, () -> {
            IntentUtil.fromBytes(intentBytes);
        });
    }

    private static byte[] toBytesUnchecked(Intent intent) {
        Parcel parcel = Parcel.obtain();
        parcel.writeParcelable(intent,0);
        byte[] intentBytes = parcel.marshall();
        parcel.recycle();

        return intentBytes;
    }

    private interface Operation {
        void execute();
    }

    private static void assertThrows(Class expectedExceptionType, Operation operation) {
        try {
            operation.execute();

            // No exception was thrown.
            String failureMessage =
                    String.format(
                            "No exception was thrown, expected a %s exception",
                            expectedExceptionType);

            fail(failureMessage);
        } catch (Exception ex) {
            if (expectedExceptionType.isInstance(ex)) {
                // Expected exception was thrown.
                return;
            }

            // An unexpected exception was thrown.
            String failureMessage =
                    String.format(
                            "Expected exception to be instance of %s, but was %s",
                            expectedExceptionType,
                            ex.getClass());
            fail(failureMessage);
        }
    }
}