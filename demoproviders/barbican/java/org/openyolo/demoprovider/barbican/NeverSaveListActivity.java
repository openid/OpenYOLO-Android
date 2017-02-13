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

import static org.openyolo.demoprovider.barbican.storage.CredentialStorageClient.ConnectedCallback;
import static org.openyolo.demoprovider.barbican.storage.CredentialStorageClient.connect;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.io.IOException;
import java.util.List;
import org.openyolo.demoprovider.barbican.storage.CredentialStorageClient;

/**
 * Displays the list of API implementors for which we have picked the "never save" option
 * user could review, select and undo one, a segment or all items.
 */
public class NeverSaveListActivity
        extends AppCompatActivity
        implements ConnectedCallback {

    private CredentialStorageClient mStorageClient;

    @BindView(R.id.headerToolbar)
    Toolbar mToolbar;

    @BindView(R.id.paired_list)
    RecyclerView mNeverSaveList;
    private NeverSaveAppAdapter mAppAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apipairing_list_layout);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mAppAdapter = new NeverSaveAppAdapter();
        mNeverSaveList.setAdapter(mAppAdapter);
        mNeverSaveList.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected void onStart() {
        super.onStart();
        connect(this, this);
    }

    @Override
    public void onStorageConnected(CredentialStorageClient client) {
        mStorageClient = client;
        mAppAdapter.setCredentialStorageClient(client);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mStorageClient.disconnect(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //hide the lock icon in this case
        menu.getItem(1).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        }

        if (R.id.action_wipe == item.getItemId()) {
            try {
                List<String> list = mStorageClient.getNeverSaveList();
                Toast.makeText(this, list.toString(), Toast.LENGTH_LONG).show();
                mStorageClient.clearNeverSaveList();
                mAppAdapter.notifyDataSetChanged();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }
}
