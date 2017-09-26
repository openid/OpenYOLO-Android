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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.AuthenticationMethod;
import org.openyolo.protocol.AuthenticationMethods;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.Hint;
import org.openyolo.protocol.PasswordSpecification;
import org.valid4j.errors.RequireViolation;

public final class CredentialView extends LinearLayout {

    private static final String TAG = "CredentialView";

    @BindView(R.id.generate_id_button)
    ImageButton mGenerateIdButton;

    @BindView(R.id.generate_password_button)
    ImageButton mGeneratePasswordButton;

    @BindView(R.id.openyolo_email_provider_button)
    ImageButton mEmailProviderButton;

    @BindView(R.id.google_provider_button)
    ImageButton mGenerateProviderButton;

    @BindView(R.id.facebook_provider_button)
    ImageButton mFacebookProviderButton;

    @BindView(R.id.generate_display_name_button)
    ImageButton mGenerateDisplayNameButton;

    @BindView(R.id.generate_profile_picture_button)
    ImageButton mGenerateProfilePictureButton;

    @BindView(R.id.id_field)
    EditText mIdField;

    @BindView(R.id.password_field)
    EditText mPasswordField;

    @BindView(R.id.display_name_field)
    EditText mDisplayNameField;

    @BindView(R.id.authentication_method_field)
    EditText mAuthenticationMethodField;

    @BindView(R.id.profile_picture_field)
    EditText mProfilePictureField;

    @BindView(R.id.profile_picture_view)
    ImageView mProfilePictureView;

    private RandomData mRandomData;

    /**
     * Simple constructor to use when creating a credential view from code.
     *
     * @param context The Context the view is running in, through which it can access the current
     *                theme, resources, etc.
     */
    public CredentialView(Context context) {
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
    public CredentialView(Context context, AttributeSet attrs) {
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
    public CredentialView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        View view = View.inflate(context, R.layout.credential_layout, this);

        mRandomData = new RandomData();
        ButterKnife.bind(view);
    }

    /**
     * Hides or shows input generation buttons.
     */
    public void setEnableInputGeneration(boolean isEnabled) {
        int visibility = View.GONE;
        if (isEnabled) {
            visibility = View.VISIBLE;
        }

        mGenerateIdButton.setVisibility(visibility);
        mGeneratePasswordButton.setVisibility(visibility);
        mGenerateProviderButton.setVisibility(visibility);
        mEmailProviderButton.setVisibility(visibility);
        mFacebookProviderButton.setVisibility(visibility);
        mGenerateDisplayNameButton.setVisibility(visibility);
        mGenerateProfilePictureButton.setVisibility(visibility);
    }

    @OnClick(R.id.generate_id_button)
    void generateId() {
        mIdField.setText(mRandomData.generateEmailAddress());
    }

    @OnClick(R.id.generate_password_button)
    void generatePassword() {
        mPasswordField.setText(PasswordSpecification.DEFAULT.generate());
    }

    @OnClick(R.id.openyolo_email_provider_button)
    void setEmailAuthenticationMethod() {
        mAuthenticationMethodField.setText(AuthenticationMethods.EMAIL.toString());
    }

    @OnClick(R.id.google_provider_button)
    void setGoogleAuthenticationMethod() {
        mAuthenticationMethodField.setText(AuthenticationMethods.GOOGLE.toString());
    }

    @OnClick(R.id.facebook_provider_button)
    void setFacebookAuthenticationMethod() {
        mAuthenticationMethodField.setText(AuthenticationMethods.FACEBOOK.toString());
    }

    @OnClick(R.id.generate_display_name_button)
    void generateDisplayName() {
        mDisplayNameField.setText(mRandomData.generateDisplayName());
    }

    @OnClick(R.id.generate_profile_picture_button)
    void generateProfilePicture() {
        mProfilePictureField.setText(mRandomData.generateProfilePictureUri());
    }

    @OnTextChanged(R.id.profile_picture_field)
    void loadProfilePicture() {
        GlideApp.with(getContext())
                .load(Uri.parse(mProfilePictureField.getText().toString()))
                .fitCenter()
                .into(mProfilePictureView);
    }

    /**
     * Clears all of the credential's fields.
     */
    public void clearFields() {
        mIdField.setText("");
        mPasswordField.setText("");
        mDisplayNameField.setText("");
        mAuthenticationMethodField.setText("");
        mProfilePictureField.setText("");
    }

    /**
     * Populates the fragment's fields from a given OpenYolo credential.
     *
     * @param credential an OpenYolo credential
     */
    public void setFieldsFromCredential(Credential credential) {
        copyIfNotNull(credential.getIdentifier(), mIdField);
        copyIfNotNull(credential.getPassword(), mPasswordField);
        copyIfNotNull(credential.getDisplayName(), mDisplayNameField);
        copyIfNotNull(credential.getDisplayPicture(), mProfilePictureField);
        copyIfNotNull(credential.getAuthenticationMethod(), mAuthenticationMethodField);
    }

    /**
     * Populates the fragment's fields from a given OpenYOLO hint.
     */
    public void setFieldsFromHint(Hint hint) {
        copyIfNotNull(hint.getIdentifier(), mIdField);
        copyIfNotNull(hint.getGeneratedPassword(), mPasswordField);
        copyIfNotNull(hint.getDisplayName(), mDisplayNameField);
        copyIfNotNull(hint.getDisplayPicture(), mProfilePictureField);
        copyIfNotNull(hint.getAuthenticationMethod(), mAuthenticationMethodField);
    }

    /**
     * Create an OpenYolo credential from the current fragment's fields.
     *
     * @return an OpenYolo Credential .
     */
    @Nullable
    public Credential makeCredentialFromFields() {
        String authMethodStr = mAuthenticationMethodField.getText().toString();
        AuthenticationMethod authMethod;
        try {
            authMethod = new AuthenticationMethod(authMethodStr);
        } catch (RequireViolation ex) {
            Log.w(TAG, "User entered authentication method " + authMethodStr + " is invalid", ex);
            return null;
        }

        AuthenticationDomain authenticationDomain =
                AuthenticationDomain.getSelfAuthDomain(getContext());

        Credential.Builder credentialBuilder = new Credential.Builder(
                mIdField.getText().toString(),
                authMethod,
                authenticationDomain)
                .setDisplayName(convertEmptyToNull(mDisplayNameField.getText().toString()))
                .setDisplayPicture(convertEmptyToNull(mProfilePictureField.getText().toString()));

        credentialBuilder.setPassword(mPasswordField.getText().toString());

        return credentialBuilder.build();
    }

    private static String convertEmptyToNull(@NonNull String str) {
        if (str.isEmpty()) {
            return null;
        }

        return str;
    }

    private static void copyIfNotNull(@Nullable Object value, @NonNull EditText field) {
        if (value != null) {
            field.setText(value.toString());
        }
    }
}
