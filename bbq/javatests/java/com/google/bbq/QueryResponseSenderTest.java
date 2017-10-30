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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import android.content.Intent;
import com.google.bbq.Protobufs.BroadcastQuery;
import com.google.bbq.Protobufs.BroadcastQueryResponse;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import java.util.List;


/**
 * This class contains a set of tests for QueryResponseSenderTest.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class QueryResponseSenderTest {

    private BroadcastQuery mQuery;
    private QueryResponseSender mResponseSender;

    @Before
    public void setUp() throws Exception {
        //set mock values.
        mQuery = BroadcastQuery.newBuilder()
            .setDataType("example")
            .setRequestingApp("com.example.app")
            .setRequestId(128L)
            .setResponseId(256L)
            .build();

        mResponseSender = new QueryResponseSender(RuntimeEnvironment.application);
    }

    @Test
    public void sendResponse() throws Exception {
        byte[] responseMessageBytes = new byte[] { 0, 1, 2 };
        mResponseSender.sendResponse(mQuery, responseMessageBytes);
        checkBroadcastResponse(ByteString.copyFrom(responseMessageBytes));
    }

    @Test
    public void sendResponse_nonNullQuery_nullReponse() throws Exception {
        mResponseSender.sendResponse(mQuery, null);
        checkBroadcastResponse(ByteString.EMPTY);
    }

    private void checkBroadcastResponse(ByteString expectedResponseBytes)
            throws InvalidProtocolBufferException {
        List<Intent> broadcasts =
                Shadows.shadowOf(RuntimeEnvironment.application).getBroadcastIntents();

        assertThat(broadcasts.size()).isEqualTo(1);
        Intent broadcastIntent = broadcasts.get(0);

        assertThat(broadcastIntent.getAction())
                .isEqualTo("example:0000000000000080");
        assertThat(broadcastIntent.getCategories()).containsExactly(QueryUtil.BBQ_CATEGORY);
        assertThat(broadcastIntent.getPackage()).isEqualTo(mQuery.getRequestingApp());
        assertThat(broadcastIntent.getByteArrayExtra(QueryUtil.EXTRA_RESPONSE_MESSAGE)).isNotNull();

        byte[] responseBytes = broadcastIntent.getByteArrayExtra(QueryUtil.EXTRA_RESPONSE_MESSAGE);
        BroadcastQueryResponse response = BroadcastQueryResponse.parseFrom(responseBytes);

        assertThat(response.getRequestId()).isEqualTo(mQuery.getRequestId());
        assertThat(response.getResponseId()).isEqualTo(mQuery.getResponseId());
        assertThat(response.getResponseMessage()).isEqualTo(expectedResponseBytes);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void sendResponse_nullQuery_nullReponse() throws Exception {
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                mResponseSender.sendResponse(null, null);
            }
        }).isInstanceOf(NullPointerException.class);
    }
}
