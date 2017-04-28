/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
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

package org.openyolo.demoprovider.barbican.provider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.io.IOException;
import org.openyolo.demoprovider.barbican.R;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.CredentialRetrieveResult;
import org.openyolo.protocol.Protobufs;

/**
 * Displays a brief, "snackbar"-like notification to the user indicating that a credential
 * is being returned to a requesting app.
 */
public class AutoSignInActivity extends AppCompatActivity {

    private static final long DISPLAY_TIME_MS = 2000;

    private static final String EXTRA_CREDENTIAL = "credential";

    @BindView(android.R.id.content)
    View mRootView;

    @BindView(R.id.credential_primary)
    TextView mCredentialPrimary;

    @BindView(R.id.credential_secondary)
    TextView mCredentialSecondary;

    /**
     * Creates an intent to display this activity with the specified credential.
     */
    public static Intent createIntent(Context context, Credential credential) {
        return createIntent(context, credential.toProtobuf());
    }

    /**
     * Creates an intent to display this activity with the specified credential, in protobuf form.
     */
    public static Intent createIntent(Context context, Protobufs.Credential credential) {
        Intent intent = new Intent(context, AutoSignInActivity.class);
        intent.putExtra(EXTRA_CREDENTIAL, credential.toByteArray());
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_sign_in_layout);
        positionWindow();
        ButterKnife.bind(this);

        Credential credential = getCredential();
        if (credential.getDisplayName() != null) {
            mCredentialPrimary.setText(credential.getDisplayName());
            mCredentialSecondary.setText(credential.getIdentifier());
        } else {
            mCredentialPrimary.setText(credential.getIdentifier());
            mCredentialSecondary.setVisibility(View.GONE);
        }

        mRootView.postDelayed(new ReturnCredentialTask(), DISPLAY_TIME_MS);
    }

    /**
     * Ensures the activity's window fills the full "visible" section of the screen, excluding
     * the status bar. This is a workaround for a size bug when using
     * {@code windowMinWidthMajor / windowMinWidthMinor} that can leave the windows a few pixels
     * off the correct size.
     */
    private void positionWindow() {
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = 0;
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        getWindow().setLayout(
                rectangle.width(),
                rectangle.height() - statusBarHeight);
    }

    private Credential getCredential() {
        byte[] credentialBytes = getIntent().getByteArrayExtra(EXTRA_CREDENTIAL);
        try {
            return Credential.fromProtoBytes(credentialBytes);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to decode credential");
        }
    }

    private class ReturnCredentialTask implements Runnable {
        @Override
        public void run() {
            CredentialRetrieveResult result = new CredentialRetrieveResult.Builder(
                    CredentialRetrieveResult.RESULT_SUCCESS)
                    .setCredential(getCredential())
                    .build();
            setResult(result.getResultCode(), result.toResultDataIntent());
            finish();
        }
    }
}
