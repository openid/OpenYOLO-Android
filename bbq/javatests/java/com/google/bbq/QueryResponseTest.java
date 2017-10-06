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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import android.os.Parcel;
import java.security.SecureRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Battery of tests for the QueryResponse
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class QueryResponseTest {

    private static final String PACKAGE = "com.example.provider";
    private static final long RESPONSE_ID = 1234L;
    private static final byte[] RESPONSE_MESSAGE = new byte[] { 0, 1, 2, 3, 4 };

    private static final QueryResponse QUERY_RESPONSE =
            new QueryResponse(PACKAGE, RESPONSE_ID, RESPONSE_MESSAGE);

    @Test
    public void testDescribeContents() throws Exception {
        assertEquals(0, QUERY_RESPONSE.describeContents());
    }

    @Test
    public void testWriteAndReadParcel() throws Exception {
        Parcel p = Parcel.obtain();
        p.writeParcelable(QUERY_RESPONSE, 0);
        p.setDataPosition(0);
        QueryResponse read = p.readParcelable(QueryResponse.class.getClassLoader());

        assertThat(read.responderPackage).isEqualTo(QUERY_RESPONSE.responderPackage);
        assertThat(read.responseId).isEqualTo(QUERY_RESPONSE.responseId);
        assertThat(read.responseMessage).isEqualTo(QUERY_RESPONSE.responseMessage);
    }

    @Test
    public void testWriteAndReadParcel_noResponseMessage() {
        QueryResponse queryResponse = new QueryResponse(PACKAGE, RESPONSE_ID, null);
        Parcel p = Parcel.obtain();
        p.writeParcelable(queryResponse, 0);
        p.setDataPosition(0);
        QueryResponse read = p.readParcelable(QueryResponse.class.getClassLoader());

        assertThat(read.responderPackage).isEqualTo(QUERY_RESPONSE.responderPackage);
        assertThat(read.responseId).isEqualTo(QUERY_RESPONSE.responseId);
        assertThat(read.responseMessage).isNull();
    }

    @Test
    public void QueryResponseCreator_testNewArray() {
        QueryResponse[] queryResponses = QueryResponse.CREATOR.newArray(10);
        assertThat(queryResponses).hasSize(10);
    }
}