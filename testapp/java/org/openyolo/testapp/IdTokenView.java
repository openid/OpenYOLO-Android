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

package org.openyolo.testapp;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Displays an ID token.
 */
public class IdTokenView extends LinearLayout {

    @BindView(R.id.raw_token)
    TextView mRawTokenView;

    /**
     * Standard view constructor.
     */
    public IdTokenView(Context context) {
        super(context);
        initialize(context);
    }

    /**
     * Standard view constructor.
     */
    public IdTokenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    /**
     * Standard view constructor.
     */
    public IdTokenView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        View view = View.inflate(context, R.layout.id_token_layout, this);
        ButterKnife.bind(view);
    }

    /**
     * Specifies the ID token to display.
     */
    public void setIdToken(@Nullable String idToken) {
        if (idToken == null) {
            mRawTokenView.setText(R.string.no_token);
            return;
        }

        mRawTokenView.setText(idToken);
    }

}
