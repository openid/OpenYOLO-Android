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

import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import com.google.bbq.Protobufs.BroadcastQuery;
import com.google.bbq.Protobufs.BroadcastQueryResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.valid4j.errors.RequireViolation;


/**
 * This class contains a set of tests for QueryResponseSenderTest.
 */
public class QueryResponseSenderTest {
    @Mock
    Context mockContext;
    @Mock
    Intent mockIntent;

    BroadcastQuery mockQuery;

    private QueryResponseSender underTest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        //set mock values.
        mockQuery = BroadcastQuery.newBuilder()
            .setDataType("dataType")
            .setRequestingApp("requestingApp")
            .setRequestId(128L)
            .setResponseId(256L)
            .build();

        //Create a test object for the QueryResponseSender using the mock
        underTest = new QueryResponseSender(mockContext) {
            @NonNull
            @Override
            Intent getIntentForQuery(@NonNull BroadcastQuery query, BroadcastQueryResponse.Builder responseBuilder) {
                return mockIntent;
            }
        };
    }

    @Test
    public void sendResponse_nonNullQuery_nullReponse() throws Exception {
        underTest.sendResponse(mockQuery, null);
        verify(mockContext, times(1)).sendBroadcast(mockIntent);
    }

    @Test(expected = RequireViolation.class)
    public void sendResponse_nullQuery_nullReponse() throws Exception {
        underTest.sendResponse(null, null);
    }
}