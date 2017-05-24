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

package org.openyolo.demoprovider.barbican;

import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.MalformedDataException;
import org.openyolo.protocol.Protobufs.Credential;

/**
 * RecyclerView adapter for displaying stored credentials.
 */
class CredentialAdapter extends RecyclerView.Adapter<CredentialAdapter.ViewHolder> {

    private List<Credential> mCredentials;
    private CredentialStorageClient mClient;

    CredentialAdapter() {
        mCredentials = Collections.emptyList();
    }

    void setCredentials(List<Credential> credentials) {
        mCredentials = credentials;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View credentialView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.credential_view, parent, false);

        return new ViewHolder(credentialView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(mCredentials.get(position));
    }

    @Override
    public int getItemCount() {
        return mCredentials.size();
    }

    void setCredentialStorageClient(CredentialStorageClient credentialStorageClient) {
        mClient = credentialStorageClient;
    }

    final class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mAuthorityView;
        private ImageView mAuthorityIconView;
        private TextView mIdView;

        ViewHolder(View itemView) {
            super(itemView);

            mAuthorityView = (TextView) itemView.findViewById(R.id.credential_authority);
            mAuthorityIconView = (ImageView) itemView.findViewById(R.id.credential_authority_icon);
            mIdView = (TextView) itemView.findViewById(R.id.credential_id);
        }

        void bind(final Credential credential) {
            String appLabel = credential.getAuthDomain().toString();
            Drawable appIcon = null;

            try {
                AuthenticationDomain domain =
                        AuthenticationDomain.fromProtobuf(credential.getAuthDomain());
                if (domain.isAndroidAuthDomain()) {
                    String packageName = domain.getAndroidPackageName();
                    PackageManager pm = itemView.getContext().getPackageManager();
                    try {
                        ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
                        appLabel = pm.getApplicationLabel(info).toString();
                        appIcon = pm.getApplicationIcon(info);
                    } catch (PackageManager.NameNotFoundException e) {
                        // will just show the raw info
                    }
                }
            } catch (MalformedDataException ex) {
                // will just show the raw info
            }

            mAuthorityView.setText(appLabel);
            mIdView.setText(credential.getId());

            if (appIcon != null) {
                mAuthorityIconView.setImageDrawable(appIcon);
                mAuthorityIconView.setVisibility(View.VISIBLE);
            } else {
                mAuthorityIconView.setVisibility(View.INVISIBLE);
            }

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle(R.string.ask_delete_credential)
                            .setPositiveButton(R.string.openyolo_simple_yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface,
                                                            int which) {
                                            try {
                                                mClient.deleteCredential(credential);
                                                mCredentials.remove(credential);
                                                notifyDataSetChanged();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    })
                            .setNegativeButton(R.string.openyolo_simple_no, null)
                            .create();

                    dialog.show();
                    return true;
                }
            });
        }
    }
}
