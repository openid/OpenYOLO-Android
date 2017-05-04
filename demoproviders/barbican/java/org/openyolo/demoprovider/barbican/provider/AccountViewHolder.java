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

package org.openyolo.demoprovider.barbican.provider;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import org.openyolo.demoprovider.barbican.R;
import org.openyolo.protocol.AuthenticationMethod;
import org.openyolo.protocol.AuthenticationMethods;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.Hint;

/**
 * RecyclerView holder for displaying account options to the user in a credential or hint picker.
 */
public final class AccountViewHolder extends RecyclerView.ViewHolder {

    private ImageView mProfileIcon;
    private TextView mPrimaryLabel;
    private TextView mSecondaryLabel;

    AccountViewHolder(View itemView) {
        super(itemView);

        mProfileIcon = (ImageView) itemView.findViewById(R.id.credential_icon);
        mPrimaryLabel = (TextView) itemView.findViewById(R.id.credential_primary_label);
        mSecondaryLabel = (TextView) itemView.findViewById(R.id.credential_secondary_label);
    }

    void bind(final Credential credential, ClickHandler<Credential> clickHandler) {
        bind(
                credential.getIdentifier(),
                credential.getAuthenticationMethod(),
                credential.getDisplayName(),
                credential.getDisplayPicture(),
                credential,
                clickHandler);
    }

    void bind(final Hint hint, ClickHandler<Hint> clickHandler) {
        bind(
                hint.getIdentifier(),
                hint.getAuthenticationMethod(),
                hint.getDisplayName(),
                hint.getDisplayPictureUri(),
                hint,
                clickHandler);
    }

    <T> void bind(
            @NonNull String id,
            @NonNull AuthenticationMethod authMethod,
            @Nullable String displayName,
            @Nullable Uri profilePictureUri,
            @NonNull final T sourceObject,
            @NonNull final ClickHandler<T> clickHandler) {

        int iconId;
        if (AuthenticationMethods.EMAIL.equals(authMethod)) {
            iconId = R.drawable.email;
        } else if (AuthenticationMethods.PHONE.equals(authMethod)) {
            iconId = R.drawable.phone;
        } else {
            iconId = R.drawable.person;
        }

        if (profilePictureUri != null) {
            Glide.with(mProfileIcon.getContext())
                    .load(profilePictureUri)
                    .fitCenter()
                    .fallback(iconId)
                    .into(mProfileIcon);
        } else {
            mProfileIcon.setImageResource(iconId);
        }

        if (displayName != null) {
            mPrimaryLabel.setText(displayName);
            mSecondaryLabel.setText(id);
            mSecondaryLabel.setVisibility(View.VISIBLE);
        } else {
            mPrimaryLabel.setText(id);
            mSecondaryLabel.setVisibility(View.GONE);
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickHandler.onClick(sourceObject);
            }
        });
    }

    interface ClickHandler<T> {
        void onClick(T clicked);
    }
}
