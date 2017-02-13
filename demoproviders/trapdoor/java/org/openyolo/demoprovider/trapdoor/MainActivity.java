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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import me.philio.pinentry.PinEntryView;

/**
 * The launch activity for Trapdoor, which allows the user to set some basic user data for
 * credentials and test out password generation.
 */
public class MainActivity extends AppCompatActivity {

    private static final int PIN_LENGTH = 6;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        EditText userNameField = (EditText)findViewById(R.id.user_name_field);
        if (getUsername().isEmpty()) {
            userNameField.setText(UserDataStore.getUserName(this));
        }

        ((EditText)findViewById(R.id.user_name_field))
                .addTextChangedListener(new UpdateUsernameWatcher());

        ((PinEntryView)findViewById(R.id.pin_entry_field))
                .addTextChangedListener(new RegenerateWatcher());

        ((EditText)findViewById(R.id.app_name_field))
                .addTextChangedListener(new RegenerateWatcher());

        findViewById(R.id.copy_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyPassword();
            }
        });

        generatePassword();
    }

    private void generatePassword() {
        String userName = getUsername();
        String pin = getPin();
        String appName = getAppName();

        TextView passwordView = (TextView)findViewById(R.id.password_view);
        Button copyButton = (Button)findViewById(R.id.copy_password);

        if (userName.isEmpty() || pin.length() < PIN_LENGTH || appName.isEmpty()) {
            passwordView.setText("");
            copyButton.setVisibility(View.GONE);
        } else {
            passwordView.setText(PasswordGenerator.generatePassword(userName, pin, appName));
            copyButton.setVisibility(View.VISIBLE);
        }
    }

    private String getPin() {
        return ((PinEntryView) findViewById(R.id.pin_entry_field)).getText().toString();
    }

    private String getUsername() {
        return ((EditText) findViewById(R.id.user_name_field)).getText().toString().trim();
    }

    private String getAppName() {
        return ((EditText) findViewById(R.id.app_name_field)).getText().toString().trim();
    }

    private String getLastPassword() {
        return ((TextView) findViewById(R.id.password_view)).getText().toString();
    }

    private void copyPassword() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Password", getLastPassword());
        clipboard.setPrimaryClip(clip);
    }

    private class RegenerateWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence seq, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence seq, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable ed) {
            generatePassword();
        }
    }

    private class UpdateUsernameWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence seq, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence seq, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable userName) {
            UserDataStore.setUserName(MainActivity.this, userName.toString());
        }
    }
}
