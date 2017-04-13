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

import static android.content.DialogInterface.OnClickListener;

import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient;
import org.openyolo.protocol.AuthenticationDomain;


/**
 * RecyclerView adapter for displaying the API implementers for which Barbican picked "never save"
 * when attempting to store a credential.
 */
class NeverSaveAppAdapter extends RecyclerView.Adapter<NeverSaveAppAdapter.ViewHolder> {

    private static final String LOGTAG = "NeverSaveAppAdapter";
    private List<String> mPairedItems;
    private CredentialStorageClient mCredentialStorageClient;

    NeverSaveAppAdapter() {
        mPairedItems = Collections.emptyList();
    }

    /**
     * Adds a link to the storage client used by Barbican, the example password manager.
     *
     * @param credentialStorageClient - the client linking to app data
     */
    void setCredentialStorageClient(CredentialStorageClient credentialStorageClient) {
        mCredentialStorageClient = credentialStorageClient;
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                refreshList();
            }
        });
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View credentialView = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.paired_api_item, parent, false);
        return new ViewHolder(credentialView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(mPairedItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mPairedItems.size();
    }

    /**
     * Re-queries the list from storage.
     */
    private void refreshList() {
        try {
            mPairedItems = mCredentialStorageClient.getNeverSaveList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    final class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mAuthorityView;
        private ImageView mAuthorityIconView;
        private TextView mIdView;
        private ImageButton mDeleteBtn;

        ViewHolder(View itemView) {
            super(itemView);

            mAuthorityView = (TextView) itemView.findViewById(R.id.paired_api_title);
            mAuthorityIconView = (ImageView) itemView.findViewById(R.id.api_icon);
            mIdView = (TextView) itemView.findViewById(R.id.paired_api_desc);
            mDeleteBtn = (ImageButton) itemView.findViewById(R.id.btn_delete);
        }

        void bind(final String apibind) {
            String appLabel = apibind;
            Drawable appIcon = null;
            String packageName = null;
            final AuthenticationDomain domain = new AuthenticationDomain(apibind);
            if (domain.isAndroidAuthDomain()) {
                packageName = domain.getAndroidPackageName();
                PackageManager pm = itemView.getContext().getPackageManager();
                try {
                    ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
                    appLabel = pm.getApplicationLabel(info).toString();
                    appIcon = pm.getApplicationIcon(info);
                } catch (PackageManager.NameNotFoundException e) {
                    // will just show the raw info
                }
            }

            mAuthorityView.setText(appLabel);
            mIdView.setText(domain.getAndroidPackageName());

            if (appIcon != null) {
                mAuthorityIconView.setImageDrawable(appIcon);
                mAuthorityIconView.setVisibility(View.VISIBLE);
            } else {
                mAuthorityIconView.setVisibility(View.INVISIBLE);
            }
            mDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDeletePopup(domain);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showDeletePopup(domain);
                    return true;
                }
            });
        }

        private void showDeletePopup(final AuthenticationDomain domain) {
            AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                    .setTitle(R.string.remove_unpaired_api_dialog_title)
                    .setPositiveButton(R.string.openyolo_simple_yes, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            List<AuthenticationDomain> list = new ArrayList<>();
                            list.add(domain);
                            try {
                                mCredentialStorageClient.removeFromNeverSaveList(list);
                            } catch (IOException e) {
                                Log.d(LOGTAG, "Failed to clear item from Storage, error :", e);
                            }
                            notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton(R.string.openyolo_simple_no, null)
                    .create();

            dialog.show();

        }
    }
}
