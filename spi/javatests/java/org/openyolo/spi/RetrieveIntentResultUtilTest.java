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

package org.openyolo.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.openyolo.api.AuthenticationMethods.ID_AND_PASSWORD;
import android.content.Intent;
import android.util.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.api.AuthenticationDomain;
import org.openyolo.api.Credential;
import org.openyolo.api.CredentialClient;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Battery of tests for RetrieveIntentResultUtil
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RetrieveIntentResultUtilTest {
    public static final String EMAIL_ID = "alice@example.com";
    private String base64string = "ChFhbGljZUBleGFtcGxlLmNvbRIXaHR0cHM6Ly93d3cuZXhhbXBsZS5jb20aGm9wZW55b2xvOi8v\naWQtYW5kLXBhc3N3b3Jk\n";

    @Test
    public void testCreateResponseData() throws Exception {
        Credential cr = new Credential.Builder( EMAIL_ID, ID_AND_PASSWORD,
        new AuthenticationDomain("https://www.example.com")).build();
        Intent reposnse = RetrieveIntentResultUtil.createResponseData(cr.getProto());
        byte[] bytes = reposnse.getExtras().getByteArray(CredentialClient.EXTRA_CREDENTIAL);
        assertEquals(bytes.length, 72);
        String data = Base64.encodeToString(bytes, Base64.DEFAULT);
        assertEquals(base64string, data);
    }

    @Test
    public void testCreateResponseData_withNull() throws Exception {
        Intent reposnse = RetrieveIntentResultUtil.createResponseData(null);
        assertNull(reposnse.getExtras());
    }

}