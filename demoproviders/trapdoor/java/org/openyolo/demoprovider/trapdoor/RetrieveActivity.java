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

package org.openyolo.demoprovider.trapdoor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import me.philio.pinentry.PinEntryView;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.AuthenticationMethods;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.CredentialRetrieveResult;

/**
 * Collects a 6-digit pin from the user, then generates a password to return to the invoking app.
 */
public class RetrieveActivity extends AppCompatActivity {

    private static final int PIN_SIZE = 6;

    private static final String LOG_TAG = "TrapdoorRetrieve";

    private AuthenticationDomain mDomain;

    /**
     * Creates an Intent to start this activity.
     */
    public static Intent createIntent(Context context) {
        return new Intent(context, RetrieveActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getCallingPackage() == null) {
            Log.w(LOG_TAG, "No calling package for retrieve request, canceling");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        mDomain = AuthenticationDomain.fromPackageName(this, getCallingPackage());
        if (null == mDomain) {
            Log.w(LOG_TAG, "No AuthenticationDomain associated with the calling package.");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        Log.i(LOG_TAG, "processing retrieve intent from " + mDomain);

        setContentView(R.layout.dialog_layout);
        ((PinEntryView)findViewById(R.id.pin_entry_field))
                .addTextChangedListener(new PinChangedWatcher());
    }

    private class PinChangedWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence seq, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence seq, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable pin) {
            if (pin.length() != PIN_SIZE) {
                return;
            }
            String userName = UserDataStore.getUserName(RetrieveActivity.this);
            String password = PasswordGenerator.generatePassword(
                    userName,
                    pin.toString(),
                    mDomain.toString());

            Credential credential = new Credential.Builder(
                    UserDataStore.getUserName(RetrieveActivity.this),
                    AuthenticationMethods.EMAIL,
                    mDomain)
                    .setPassword(password)
                    .build();

            CredentialRetrieveResult result = new CredentialRetrieveResult.Builder(
                    CredentialRetrieveResult.CODE_CREDENTIAL_SELECTED)
                    .setCredential(credential)
                    .build();
            setResult(result.getResultCode(), result.toResultDataIntent());
            finish();
        }
    }
}
