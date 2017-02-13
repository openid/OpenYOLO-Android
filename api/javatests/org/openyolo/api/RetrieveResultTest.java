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

package org.openyolo.api;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import android.content.Intent;
import android.os.Parcelable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.api.internal.IntentUtil;
import org.openyolo.proto.CredentialRetrieveResponse;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.valid4j.errors.RequireViolation;

/**
 * Battery of tests for the RetrieveIntent class
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RetrieveResultTest {

    private RetrieveResult underTest;

    @Before
    public void setUp() throws Exception {
        Intent fromIntent = new Intent("com.openyolo.ACTION");
        fromIntent.setPackage("com.openyolo");
        byte[] fromBytes = IntentUtil.toBytes(fromIntent);
        Intent retrieveIntent= IntentUtil.fromBytes(fromBytes);

        Map<String, CredentialRetrieveResponse> protoResponses = new HashMap<>();

        underTest = new RetrieveResult.Builder()
                .setProtoResponses(protoResponses)
                .setRetrieveIntent(retrieveIntent)
                .build();
    }

    @Test
    public void testInnerClassNewArray(){
        int size = 10;
        Parcelable.Creator<RetrieveResult> creator = new RetrieveResult.RetrieveResultCreator();
        RetrieveResult[] result = creator.newArray(size);
        assertEquals(size, result.length);

    }

    @Test
    public void testGetRetrieveIntent(){
        Intent intent = underTest.getRetrieveIntent();
        assertNotNull(intent);
    }

    @Test
    public void testGetResponderPackageNames(){
        Set<String> responders = underTest.getResponderPackageNames();
        assertNotNull(responders);
    }

    @Test(expected = RequireViolation.class)
    public void testGetRetrieveIntentForResponder_null(){
        underTest.getRetrieveIntentForResponder(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRetrieveIntentForResponder_illegal(){
        underTest.getRetrieveIntentForResponder("BLahblah");
    }

}