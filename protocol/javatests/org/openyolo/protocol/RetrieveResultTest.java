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

package org.openyolo.protocol;

import static junit.framework.TestCase.assertNotNull;
import static org.assertj.core.api.Java6Assertions.assertThat;

import android.content.Intent;
import android.os.Parcel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.internal.IntentUtil;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.valid4j.errors.RequireViolation;

/**
 * Battery of tests for the RetrieveIntent class
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RetrieveResultTest {

    private RetrieveBbqResponse underTest;

    @Before
    public void setUp() throws Exception {
        Intent fromIntent = new Intent("com.openyolo.ACTION");
        fromIntent.setPackage("com.openyolo");
        byte[] fromBytes = IntentUtil.toBytes(fromIntent);
        Intent retrieveIntent= IntentUtil.fromBytes(fromBytes);

        Map<String, Protobufs.CredentialRetrieveBbqResponse> protoResponses = new HashMap<>();

        underTest = new RetrieveBbqResponse.Builder()
                .setProtoResponses(protoResponses)
                .setRetrieveIntent(retrieveIntent)
                .build();
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

    @Test
    public void testWriteAndRead() {
        Parcel p = Parcel.obtain();
        underTest.writeToParcel(p, 0);
        p.setDataPosition(0);

        RetrieveBbqResponse result = RetrieveBbqResponse.CREATOR.createFromParcel(p);
        checkIntentsEquivalent(underTest.getRetrieveIntent(), result.getRetrieveIntent());
        assertThat(result.getResponderPackageNames())
                .isEqualTo(underTest.getResponderPackageNames());

        for (String responder : result.getResponderPackageNames()) {
            checkIntentsEquivalent(
                    underTest.getRetrieveIntentForResponder(responder),
                    result.getRetrieveIntentForResponder(responder));

            assertThat(result.getAdditionalPropsForResponder(responder))
                    .isEqualTo(underTest.getAdditionalPropsForResponder(responder));
        }
    }

    private void checkIntentsEquivalent(Intent expected, Intent actual) {
        // NOTE: we don't check the full intent, just the basic properties
        assertThat(actual.getPackage())
                .isEqualTo(expected.getPackage());
        assertThat(actual.getAction())
                .isEqualTo(expected.getAction());
    }
}
