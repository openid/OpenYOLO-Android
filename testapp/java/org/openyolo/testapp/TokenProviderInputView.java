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
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.HashMap;
import java.util.Map;
import org.openyolo.protocol.TokenRequestInfo;

/**
 * Displays options for setting a supported token provider on an OpenYOLO request.
 */
public class TokenProviderInputView extends LinearLayout {

    private static final String TAG = "TokenProvInputView";

    private static final String GOOGLE_TOKEN_PROVIDER_URI =
            "https://accounts.google.com";

    private static final String GOOGLE_CLIENT_ID =
            "304746752269-k0i6b4qsant0mi47612phok7ihveohmp.apps.googleusercontent.com";

    @BindView(R.id.use_google)
    Button mUseGoogleSettings;

    @BindView(R.id.token_provider_uri_field)
    EditText mTokenProviderUriField;

    @BindView(R.id.client_id_field)
    EditText mClientIdField;

    @BindView(R.id.nonce_field)
    EditText mNonceField;

    /**
     * Standard view constructor.
     */
    public TokenProviderInputView(Context context) {
        super(context);
        initialize(context);
    }

    /**
     * Standard view constructor.
     */
    public TokenProviderInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    /**
     * Standard view constructor.
     */
    public TokenProviderInputView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        View view = View.inflate(context, R.layout.token_provider_input_layout, this);
        ButterKnife.bind(view);

        mUseGoogleSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mTokenProviderUriField.setText(GOOGLE_TOKEN_PROVIDER_URI);
                mClientIdField.setText(GOOGLE_CLIENT_ID);
            }
        });
    }

    /**
     * Creates a token provider request info map, based on the settings in the view.
     */
    public Map<String, TokenRequestInfo> getTokenProviders() {
        HashMap<String, TokenRequestInfo> tokenProviders = new HashMap<>();
        String tokenProviderUriStr = mTokenProviderUriField.getText().toString().trim();

        if (tokenProviderUriStr.isEmpty()) {
            return tokenProviders;
        }

        Uri tokenProviderUri = Uri.parse(tokenProviderUriStr);
        if (!"https".equals(tokenProviderUri.getScheme())) {
            return tokenProviders;
        }

        TokenRequestInfo.Builder infoBuilder = new TokenRequestInfo.Builder();

        String clientId = mClientIdField.getText().toString().trim();
        if (!clientId.isEmpty()) {
            infoBuilder.setClientId(clientId);
        }

        String nonce = mNonceField.getText().toString().trim();
        if (!nonce.isEmpty()) {
            infoBuilder.setNonce(nonce);
        }

        tokenProviders.put(tokenProviderUriStr, infoBuilder.build());
        return tokenProviders;
    }
}
