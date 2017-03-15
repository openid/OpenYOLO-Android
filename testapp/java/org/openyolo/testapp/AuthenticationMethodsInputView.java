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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

import java.util.HashSet;
import java.util.Set;

import org.openyolo.api.AuthenticationMethods;

public final class AuthenticationMethodsInputView extends LinearLayout {

    @BindView(R.id.facebook_authentication_method_checkbox)
    CheckBox mFaceBookCheckBox;

    @BindView(R.id.google_authentication_method_checkbox)
    CheckBox mGoogleCheckBox;

    @BindView(R.id.id_and_password_authentication_method_checkbox)
    CheckBox mIdAndPasswordCheckBox;

    @BindView(R.id.custom_authentication_method_checkbox)
    CheckBox mCustomAuthenticationMethodCheckBox;

    @BindView(R.id.custom_authentication_method)
    EditText mCustomAuthenticationMethod;

    /**
     * Simple constructor to use when creating an authentication method selection view from code.
     *
     * @param context The Context the view is running in, through which it can access the current
     *                theme, resources, etc.
     */
    public AuthenticationMethodsInputView(Context context) {
        super(context);
        initialize(context);
    }

    /**
     * Constructor that is called when inflating a view from XML.
     *
     * @param context The Context the view is running in, through which it can access the current
     *                theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public AuthenticationMethodsInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style from a theme attribute.
     *
     * @param context  The Context the view is running in, through which it can access the current
     *                 theme, resources, etc.
     * @param attrs    The attributes of the XML tag that is inflating the view.
     * @param defStyle An attribute in the current theme that contains a reference to a style
     *                 resource that supplies default values for the view. Can be 0 to not look for
     *                 defaults.
     */
    public AuthenticationMethodsInputView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        View view = View.inflate(context, R.layout.authentication_method_input_layout, this);

        ButterKnife.bind(view);
    }

    /**
     * @return A set of authentication domains that are marked as enabled.
     */
    public Set<Uri> getEnabledAuthenticationMethods() {
        Set<Uri> authenticationMethods = new HashSet<>();

        if (mFaceBookCheckBox.isChecked()) {
            authenticationMethods.add(AuthenticationMethods.FACEBOOK);
        }

        if (mGoogleCheckBox.isChecked()) {
            authenticationMethods.add(AuthenticationMethods.GOOGLE);
        }

        if (mIdAndPasswordCheckBox.isChecked()) {
            authenticationMethods.add(AuthenticationMethods.ID_AND_PASSWORD);
        }

        if (mCustomAuthenticationMethodCheckBox.isChecked()) {
            Uri authenticationMethod = Uri.parse(mCustomAuthenticationMethod.getText().toString());
            authenticationMethods.add(authenticationMethod);
        }

        return authenticationMethods;
    }
}
