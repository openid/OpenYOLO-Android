/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.bbq;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import android.os.Parcel;
import java.security.SecureRandom;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Battery of tests for the QueryResponse
 */
public class QueryResponseTest {
    @Mock
    private Parcel mockParcel;

    private QueryResponse underTest;
    private String mResponderPackage = "mResponderPackage";
    private long mResponseId = 128L;
    private byte[] mResponseMessage = new byte[64];

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(mResponseMessage);

        underTest = new QueryResponse(mResponderPackage, mResponseId, mResponseMessage);
    }

    @Test
    public void testDescribeContents() throws Exception {
        assertEquals(0, underTest.describeContents());
    }

    @Test
    public void testWriteToParcel_WithResponseMessage() throws Exception {
        underTest.writeToParcel(mockParcel, anyInt());
        verify(mockParcel, times(1)).writeString(mResponderPackage);
        verify(mockParcel, times(1)).writeLong(mResponseId);
        verify(mockParcel, times(1)).writeInt(1);
        verify(mockParcel, times(1)).writeInt(64);
        verify(mockParcel, times(1)).writeByteArray(mResponseMessage);
    }

    @Test
    public void testWriteToParcel_WithOutResponseMessage() throws Exception {
        underTest = new QueryResponse(mResponderPackage, mResponseId, null);
        underTest.writeToParcel(mockParcel, anyInt());
        verify(mockParcel, times(1)).writeString(mResponderPackage);
        verify(mockParcel, times(1)).writeLong(mResponseId);
        verify(mockParcel, times(1)).writeInt(0);
        verify(mockParcel, never()).writeByteArray(mResponseMessage);
    }

}