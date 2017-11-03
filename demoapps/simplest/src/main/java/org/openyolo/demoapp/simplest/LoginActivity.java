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
import android.widget.Toast;

import org.openyolo.api.CredentialClient;
import org.openyolo.protocol.AuthenticationMethods;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.CredentialRetrieveRequest;
import org.openyolo.protocol.CredentialRetrieveResult;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_RETRIEVE_CREDENTIAL = 234;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsernameEditText = findViewById(R.id.username_edittext);
        mPasswordEditText = findViewById(R.id.password_edittext);

        if (savedInstanceState == null) {
            // only on first launch
            startActivityForCredentialResult();
        }
    }

    public void onClickLogin(View view) {
        // clicked on the login button
        loginWith(mUsernameEditText.toString(), mPasswordEditText.toString());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RC_RETRIEVE_CREDENTIAL == requestCode) {
            CredentialClient client = CredentialClient.getInstance(this);
            CredentialRetrieveResult result = client.getCredentialRetrieveResult(data);
            Credential credential = result.getCredential();
            if (result.isSuccessful() && credential != null) {
                // A credential was retrieved, automatically sign the user in.
                String username = credential.getIdentifier();
                String password = credential.getPassword();
                mUsernameEditText.setText(username);
                mPasswordEditText.setText(password);
                loginWith(username, password);
            }
        }
    }

    private void loginWith(String username, String password) {
        if (validateCredentialWithBackend()) {
            startActivity(new Intent(this, LoggedInActivity.class));
            finish();

            // call OpenYolo provider to save the credential
            new SaveInOpenYoloUtil().save(this, username, password);
        } else {
            Toast.makeText(this, R.string.invalid_credentials, Toast.LENGTH_LONG).show();
        }
    }

    private void startActivityForCredentialResult() {
        // request credential from open yolo providers
        CredentialClient client = CredentialClient.getInstance(this);

        CredentialRetrieveRequest request = CredentialRetrieveRequest.fromAuthMethods(
                AuthenticationMethods.EMAIL, AuthenticationMethods.USER_NAME);
        Intent retrieveCredentialIntent = client.getCredentialRetrieveIntent(request);
        startActivityForResult(retrieveCredentialIntent, RC_RETRIEVE_CREDENTIAL);
    }

    private boolean validateCredentialWithBackend() {
        // do the validation of the credential here
        return true;
    }
}
