/*
 * Copyright 2017 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openyolo.api.internal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * An invisible Activity which immediately finishes with the given {@link ActivityResult}.
 */
public final class FinishWithResultActivity extends Activity {

    private static final String KEY_RESULT = "ActivityResult";

    /**
     * Returns an Intent for {@link FinishWithResultActivity} for the given {@link ActivityResult}.
     */
    public static Intent createIntent(Context context, ActivityResult result) {
        return new Intent()
                .setClass(context, FinishWithResultActivity.class)
                .putExtra(KEY_RESULT, result);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityResult result = getIntent().getParcelableExtra(KEY_RESULT);
        if (null != result) {
            setResult(result.getResultCode(), result.getData());
        }

        finish();
    }
}
