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

package org.openyolo.demoprovider.barbican.storage;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.openyolo.api.AuthenticationDomain;
import org.openyolo.demoprovider.barbican.proto.AccountHint;
import org.openyolo.proto.Credential;

/**
 * A client to the {@link CredentialStorageService}, allowing activities to access the
 * retained instance of the credential store.
 */
public class CredentialStorageClient implements CredentialStorageApi {

    private static final String LOG_TAG = "StorageClient";

    private final CredentialStorageApi mApi;
    private final ServiceConnection mConnection;

    /**
     * Connects to the credential storage service, invoking the provided callback when connected.
     */
    public static void connect(Context context, ConnectedCallback callback) {
        CredentialStorageConnection connector = new CredentialStorageConnection(callback);
        Log.d(LOG_TAG, connector.mConnectionId + ": before startService");
        context.startService(new Intent(context, CredentialStorageService.class));
        Log.d(LOG_TAG, connector.mConnectionId + ": before bindService");
        context.bindService(
                new Intent(context, CredentialStorageService.class),
                connector,
                Context.BIND_AUTO_CREATE);
        Log.d(LOG_TAG, connector.mConnectionId + ": after bindService");
    }

    CredentialStorageClient(CredentialStorageApi api, ServiceConnection connection) {
        mApi = api;
        mConnection = connection;
    }

    /**
     * Disconnects the service connection to the storage service.
     */
    public void disconnect(Context context) {
        context.unbindService(mConnection);
    }

    @Override
    public boolean isCreated() {
        return mApi.isCreated();
    }

    @Override
    public void create(String password) throws IOException {
        mApi.create(password);
    }

    @Override
    public boolean isUnlocked() {
        return mApi.isUnlocked();
    }

    @Override
    public boolean unlock(String password) throws IOException {
        return mApi.unlock(password);
    }

    @Override
    public void lock() {
        mApi.lock();
    }

    @Override
    public boolean isOnNeverSaveList(List<AuthenticationDomain> authDomains) throws IOException {
        return mApi.isOnNeverSaveList(authDomains);
    }

    @Override
    public void addToNeverSaveList(AuthenticationDomain authDomain) throws IOException {
        mApi.addToNeverSaveList(authDomain);
    }

    @Override
    public void removeFromNeverSaveList(List<AuthenticationDomain> authDomains) throws IOException {
        mApi.removeFromNeverSaveList(authDomains);
    }

    @Override
    public List<String> getNeverSaveList() throws IOException {
        return mApi.getNeverSaveList();
    }

    @Override
    public void clearNeverSaveList() throws IOException {
        mApi.clearNeverSaveList();
    }

    @Override
    public List<AccountHint> getHints() throws IOException {
        return mApi.getHints();
    }

    @Override
    public boolean hasCredentialFor(String authDomain) throws IOException {
        return mApi.hasCredentialFor(authDomain);
    }

    @Override
    public List<Credential> listAllCredentials() throws IOException {
        return mApi.listAllCredentials();
    }

    @Override
    public void upsertCredential(Credential credential) throws IOException {
        mApi.upsertCredential(credential);
    }

    @Override
    public void deleteCredential(Credential credential) throws IOException {
        mApi.deleteCredential(credential);
    }

    @Override
    public List<Credential> listCredentials(List<AuthenticationDomain> authDomains)
            throws IOException {
        return mApi.listCredentials(authDomains);
    }

    /**
     * Callback interface for notifing callers when the client is connected.
     */
    public interface ConnectedCallback {
        /**
         * Invoked when the provided client is connected.
         */
        void onStorageConnected(CredentialStorageClient client);
    }

    private static final class CredentialStorageConnection implements ServiceConnection {

        private String mConnectionId;
        private ConnectedCallback mCallback;
        private long mStartTime;

        CredentialStorageConnection(ConnectedCallback callback) {
            mConnectionId = UUID.randomUUID().toString();
            mCallback = callback;
            mStartTime = System.nanoTime();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(LOG_TAG, mConnectionId + ": enter onServiceConnected");
            if (mCallback != null) {
                CredentialStorageClient client = new CredentialStorageClient(
                        ((CredentialStorageService.LocalBinder)binder).getApi(),
                        this);
                mCallback.onStorageConnected(client);
                mCallback = null;
            }

            long timeTaken = TimeUnit.MILLISECONDS.convert(
                    System.nanoTime() - mStartTime,
                    TimeUnit.NANOSECONDS);
            Log.d(LOG_TAG, mConnectionId + ": Took " + timeTaken + "ms to connect");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, mConnectionId + ": Disconnected from service");
        }
    }
}
