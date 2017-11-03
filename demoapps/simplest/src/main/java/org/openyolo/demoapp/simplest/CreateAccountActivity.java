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
package org.openyolo.demoapp.simplest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.openyolo.api.CredentialClient;
import org.openyolo.protocol.AuthenticationMethods;
import org.openyolo.protocol.Hint;
import org.openyolo.protocol.HintRetrieveRequest;
import org.openyolo.protocol.HintRetrieveResult;

public class CreateAccountActivity extends AppCompatActivity {

    private static final int RC_RETRIEVE_HINT = 324;

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mUsernameEditText = findViewById(R.id.username_edittext);
        mPasswordEditText = findViewById(R.id.password_edittext);

        if (savedInstanceState == null) {
            // only on first launch
            startActivityForHintResult();
        }
    }

    public void onClickCreateAccount(View view) {
        String username = mUsernameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        // in your client application, store the credential here

        startActivity(new Intent(this, LoggedInActivity.class));
        finish();

        // call OpenYolo provider to save the credential
        new SaveInOpenYoloUtil().save(this, username, password);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RC_RETRIEVE_HINT == requestCode) {
            // receive an hint from an OpenYolo provider
            CredentialClient client = CredentialClient.getInstance(this);
            HintRetrieveResult result = client.getHintRetrieveResult(data);
            Hint hint = result.getHint();
            if (result.isSuccessful() && hint != null) {
                // A hint was retrieved, you may be able to automatically create an account for the
                // user, or offer the user to sign in if an existing account matches the hint.
                mUsernameEditText.setText(hint.getIdentifier());
                mPasswordEditText.setText(hint.getGeneratedPassword());
            }
        }
    }

    private void startActivityForHintResult() {
        // request some hints for the account creation
        CredentialClient client = CredentialClient.getInstance(this);

        HintRetrieveRequest request = HintRetrieveRequest.fromAuthMethods(
                AuthenticationMethods.EMAIL, AuthenticationMethods.USER_NAME);
        Intent retrieveHintIntent = client.getHintRetrieveIntent(request);
        startActivityForResult(retrieveHintIntent, RC_RETRIEVE_HINT);
    }
}
