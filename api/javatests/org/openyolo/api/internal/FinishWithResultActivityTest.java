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

package org.openyolo.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.Intent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.api.CredentialClient;
import org.openyolo.protocol.HintRetrieveResult;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

/**
 * Units tests for {@link FinishWithResultActivity}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public final class FinishWithResultActivityTest {

    private static final Context CONTEXT = RuntimeEnvironment.application;

    @Test
    public void startActivity_withResult_finishesWithResult() {
        int givenResultCode = 0x2c001;
        Intent givenIntent =
                new HintRetrieveResult.Builder(givenResultCode).build().toResultDataIntent();
        Intent intent = FinishWithResultActivity.createIntent(
                CONTEXT,
                ActivityResult.of(givenResultCode, givenIntent));

        ShadowActivity activity = startActivity(intent);

        assertThat(activity.isFinishing()).isTrue();
        HintRetrieveResult result =
                CredentialClient.getInstance(CONTEXT)
                        .getHintRetrieveResult(activity.getResultIntent());
        assertThat(result.getResultCode()).isEqualTo(givenResultCode);
        assertThat(activity.getResultCode()).isEqualTo(givenResultCode);
    }

    private static ShadowActivity startActivity(Intent intent) {
        return shadowOf(buildActivity(FinishWithResultActivity.class, intent).create().get());
    }
}
