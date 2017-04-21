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

import android.content.Intent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.ProtocolConstants;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.openyolo.protocol.AuthenticationMethods.EMAIL;

/**
 * Battery of tests for RetrieveIntentResultUtil
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RetrieveIntentResultUtilTest {
    public static final String EMAIL_ID = "alice@example.com";

    @Test
    public void testCreateResponseData() throws Exception {
        Credential cr = new Credential.Builder(
                EMAIL_ID,
                EMAIL,
                new AuthenticationDomain("https://www.example.com"))
                .build();
        Intent response = RetrieveIntentResultUtil.createResponseData(cr.getProto());

        assertThat(response.hasExtra(ProtocolConstants.EXTRA_CREDENTIAL)).isTrue();
        byte[] bytes = response.getExtras().getByteArray(ProtocolConstants.EXTRA_CREDENTIAL);
        Credential credential = Credential.fromProtoBytes(bytes);
        assertThat(credential.getIdentifier()).isEqualTo(EMAIL_ID);
        assertThat(credential.getAuthenticationMethod()).isEqualTo(EMAIL);
    }

    @Test
    public void testCreateResponseData_withNull() throws Exception {
        Intent response = RetrieveIntentResultUtil.createResponseData(null);
        assertNull(response.getExtras());
    }

}