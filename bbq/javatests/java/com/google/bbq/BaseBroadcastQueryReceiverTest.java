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

package com.google.bbq;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import com.google.bbq.Protobufs.BroadcastQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

/**
 * Battery of tests for the BaseBroadcastQueryReceiver
 */
@RunWith(org.robolectric.RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BaseBroadcastQueryReceiverTest {
    BaseBroadcastQueryReceiver underTest;
    String logtag = "logtag";

    @Mock
    Context mockContext;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        underTest = new BaseBroadcastQueryReceiver(logtag) {
            @Override
            protected void processQuery(@NonNull Context context, @NonNull BroadcastQuery query) {

            }
        };
    }

    @Test
    public void testOnReceive_null() throws Exception {
        underTest.onReceive(mockContext, new Intent());
        verify(mockContext, never()).getPackageManager();
    }

    @Test
    public void testOnReceive_emptyArray() throws Exception {
        Intent intent = new Intent();

        intent.putExtra(QueryUtil.EXTRA_QUERY_MESSAGE, new byte[256]);
        underTest.onReceive(mockContext, intent);
        verify(mockContext, never()).getPackageManager();
    }

}