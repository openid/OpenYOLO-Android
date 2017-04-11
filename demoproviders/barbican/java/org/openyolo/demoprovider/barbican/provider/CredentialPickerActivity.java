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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openyolo.demoprovider.barbican.CredentialClassifier;
import org.openyolo.demoprovider.barbican.CredentialQualityScore;
import org.openyolo.demoprovider.barbican.R;
import org.openyolo.proto.Credential;
import org.openyolo.proto.CredentialList;
import org.openyolo.spi.RetrieveIntentResultUtil;

/**
 * Displays a dialog to pick a credential from a list.
 */
public class CredentialPickerActivity extends AppCompatActivity {

    private static final String LOG_TAG = "CredentialPicker";

    private static final String EXTRA_CREDENTIALS = "credentials";

    @BindView(R.id.available_credentials)
    RecyclerView mCredentialView;

    /**
     * Creates an intent to display the credential picker with the provided set of credentials.
     */
    public static Intent createIntent(Context context, List<Credential> credentials) {
        Intent intent = new Intent(context, CredentialPickerActivity.class);
        intent.putExtra(EXTRA_CREDENTIALS, new CredentialList(credentials).encode());
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, "Picker invoked by " + getCallingPackage());

        setContentView(R.layout.credential_picker_layout);
        ButterKnife.bind(this);

        mCredentialView.setAdapter(new CredentialPickerAdapter(getCredentials()));
        mCredentialView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }

        return super.onTouchEvent(event);
    }

    private List<Credential> getCredentials() {
        byte[] credentialBytes = getIntent().getByteArrayExtra(EXTRA_CREDENTIALS);
        try {
            List<Credential> credentials =
                    new ArrayList<>(CredentialList.ADAPTER.decode(credentialBytes).credentials);
            Collections.sort(credentials, CredentialQualityScore.QUALITY_SORT);
            return credentials;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to decode credentials from intent", ex);
        }
    }

    private final class CredentialPickerAdapter extends RecyclerView.Adapter<CredentialViewHolder> {

        List<Credential> mCredentials;

        CredentialPickerAdapter(List<Credential> credentials) {
            mCredentials = credentials;
        }

        @Override
        public CredentialViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View credentialView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.credential_picker_item_view, parent, false);

            return new CredentialViewHolder(credentialView);
        }

        @Override
        public void onBindViewHolder(CredentialViewHolder holder, int position) {
            holder.bind(mCredentials.get(position));
        }

        @Override
        public int getItemCount() {
            return mCredentials.size();
        }
    }

    private final class CredentialViewHolder extends RecyclerView.ViewHolder {

        private ImageView mProfileIcon;
        private TextView mPrimaryLabel;
        private TextView mSecondaryLabel;

        CredentialViewHolder(View itemView) {
            super(itemView);

            mProfileIcon = (ImageView) itemView.findViewById(R.id.credential_icon);
            mPrimaryLabel = (TextView) itemView.findViewById(R.id.credential_primary_label);
            mSecondaryLabel = (TextView) itemView.findViewById(R.id.credential_secondary_label);
        }

        void bind(final Credential credential) {

            int iconId = CredentialClassifier.getDefaultIconForCredential(credential);

            if (credential.displayPictureUri != null) {
                Uri displayPictureUri = Uri.parse(credential.displayPictureUri);
                Glide.with(CredentialPickerActivity.this)
                        .load(displayPictureUri)
                        .fitCenter()
                        .fallback(iconId)
                        .into(mProfileIcon);
            } else {
                mProfileIcon.setImageResource(iconId);
            }

            if (credential.displayName != null) {
                mPrimaryLabel.setText(credential.displayName);
                mSecondaryLabel.setText(credential.id);
                mSecondaryLabel.setVisibility(View.VISIBLE);
            } else {
                mPrimaryLabel.setText(credential.id);
                mSecondaryLabel.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent responseData =
                            RetrieveIntentResultUtil.createResponseData(credential);
                    setResult(RESULT_OK, responseData);
                    finish();
                }
            });
        }
    }
}
