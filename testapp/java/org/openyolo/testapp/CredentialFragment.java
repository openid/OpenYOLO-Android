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

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import com.bumptech.glide.Glide;
import org.openyolo.api.AuthenticationDomain;
import org.openyolo.api.AuthenticationMethods;
import org.openyolo.api.Credential;
import org.openyolo.api.PasswordSpecification;

/**
 * A reusable fragment that displays an OpenYolo credential.
 */
public final class CredentialFragment extends Fragment {

    private static final String EXTRA_ENABLE_INPUT_GENERATION = "EXTRA_ENABLE_INPUT_GENERATION";
    @BindView(R.id.generate_id_button)
    ImageButton mGenerateIdButton;

    @BindView(R.id.generate_password_button)
    ImageButton mGeneratePasswordButton;

    @BindView(R.id.openyolo_id_and_password_provider_button)
    ImageButton mIdAndPasswordProviderButton;

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
     * Returns a new instance of a {@link CredentialFragment}.
     */
    public static CredentialFragment newInstance(boolean enableInputGeneration) {
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_ENABLE_INPUT_GENERATION, enableInputGeneration);

        CredentialFragment fragment = new CredentialFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRandomData = new RandomData();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.credential_layout, container, false);
        ButterKnife.bind(this, view);
        setEnableInputGeneration(getArguments().getBoolean(EXTRA_ENABLE_INPUT_GENERATION));

        return view;
    }

    /**
     * Hides or shows input generation buttons.
     */
    private void setEnableInputGeneration(boolean isEnabled) {
        int visibility = View.GONE;
        if (isEnabled) {
            visibility = View.VISIBLE;
        }

        mGenerateIdButton.setVisibility(visibility);
        mGeneratePasswordButton.setVisibility(visibility);
        mGenerateProviderButton.setVisibility(visibility);
        mIdAndPasswordProviderButton.setVisibility(visibility);
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

    @OnClick(R.id.openyolo_id_and_password_provider_button)
    void setIdAndPasswordAuthenticationMethod() {
        mAuthenticationMethodField.setText(AuthenticationMethods.ID_AND_PASSWORD.toString());
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
        Glide.with(this)
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
     * Create an OpenYolo credential from the current fragment's fields.
     * @return an OpenYolo Credential .
     */
    @Nullable
    public Credential makeCredentialFromFields() {
        String authenticationMethod = mAuthenticationMethodField.getText().toString();
        Uri authMethodUri = Uri.parse(authenticationMethod);
        AuthenticationDomain authenticationDomain =
                AuthenticationDomain.getSelfAuthDomain(getContext());

        Credential.Builder credentialBuilder = new Credential.Builder(
                mIdField.getText().toString(),
                authMethodUri,
                authenticationDomain)
                .setDisplayName(convertEmptyToNull(mDisplayNameField.getText().toString()))
                .setDisplayPicture(convertEmptyToNull(mProfilePictureField.getText().toString()));

        if (authMethodUri.equals(AuthenticationMethods.ID_AND_PASSWORD)) {
            credentialBuilder.setPassword(mPasswordField.getText().toString());
        }

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
