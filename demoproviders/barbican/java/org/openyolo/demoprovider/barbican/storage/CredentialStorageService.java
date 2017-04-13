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

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openyolo.demoprovider.barbican.CredentialListActivity;
import org.openyolo.demoprovider.barbican.LockActivity;
import org.openyolo.demoprovider.barbican.Protobufs.AccountHint;
import org.openyolo.demoprovider.barbican.R;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.Protobufs.Credential;

/**
 * A service which retains an instance of {@link CredentialStorage} beyond the lifecycle of the
 * activities that interact with it. This reduces the frequency at which the user would have
 * to enter their password to unlock the keystore.
 */
public class CredentialStorageService extends Service implements CredentialStorageApi {

    private static final String LOG_TAG = "CredentialStorage";

    private static final int UNLOCKED_NOTIFICATION_ID = 1;

    private final IBinder mLocalBinder = new LocalBinder();
    private CredentialStorage mStorage;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "enter onBind");
        return mLocalBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "enter onCreate");
        try {
            mStorage = new CredentialStorage(this);
        } catch (IOException ex) {
            Log.wtf(LOG_TAG, "Failed to open credential storage", ex);
            return;
        }
        Log.d(LOG_TAG, "after create storage");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean isCreated() {
        return mStorage.isCreated();
    }

    @Override
    public void create(final String password) throws IOException {
        mStorage.create(password);
        becomeForeground();
    }

    @Override
    public boolean isUnlocked() {
        return mStorage.isUnlocked();
    }

    @Override
    public boolean unlock(String password) throws IOException {
        if (mStorage.unlock(password)) {
            // when unlocked, we become foreground in order to retain the unlock key for longer,
            // and also to notify the user of the associated security risk.
            becomeForeground();
            return true;
        }

        return false;
    }

    @Override
    public void lock() {
        if (!mStorage.isUnlocked()) {
            return;
        }

        mStorage.lock();
        stopForeground(true);
    }

    @Override
    public boolean isOnNeverSaveList(List<AuthenticationDomain> authDomains) throws IOException {
        return mStorage.isOnNeverSaveList(authDomains);
    }

    @Override
    public void addToNeverSaveList(AuthenticationDomain authDomain) throws IOException {
        mStorage.addToNeverSaveList(authDomain);
    }

    @Override
    public void removeFromNeverSaveList(List<AuthenticationDomain> authDomains) throws IOException {
        mStorage.removeFromNeverSaveList(authDomains);
    }

    @Override
    public List<String> getNeverSaveList() throws IOException {
        return mStorage.retrieveNeverSaveList();
    }

    @Override
    public void clearNeverSaveList() throws IOException {
        mStorage.clearNeverSaveList();
    }

    @Override
    public List<AccountHint> getHints() throws IOException {
        return mStorage.getHints();
    }

    @Override
    public boolean hasCredentialFor(String authDomain) throws IOException {
        return mStorage.hasCredentialFor(authDomain);
    }

    @Override
    public List<Credential> listCredentials(List<AuthenticationDomain> authDomains)
            throws IOException {
        checkUnlocked();
        ArrayList<Credential> matchingCredentials = new ArrayList<>();
        for (AuthenticationDomain domain : authDomains) {
            matchingCredentials.addAll(mStorage.listCredentials(domain.toString()));
        }

        return matchingCredentials;
    }

    @Override
    public List<Credential> listAllCredentials() throws IOException {
        checkUnlocked();
        return mStorage.listAllCredentials();
    }

    @Override
    public void upsertCredential(Credential credential) throws IOException {
        checkUnlocked();
        mStorage.upsertCredential(credential);
    }

    @Override
    public void deleteCredential(Credential credential) throws IOException {
        checkUnlocked();
        mStorage.deleteCredential(credential);
    }

    private void becomeForeground() {
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.unlocked_notification_icon)
                .setContentTitle(getString(R.string.unlocked_notification_title))
                .setContentText(getString(R.string.unlocked_notification_text))
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentIntent(PendingIntent.getActivity(
                        this, 0, new Intent(this, CredentialListActivity.class), 0))
                .addAction(
                        R.drawable.lock_notification_icon,
                        getString(R.string.unlocked_notification_lock_action),
                        PendingIntent.getActivity(
                                this, 0, new Intent(this, LockActivity.class), 0
                ))
                .setLocalOnly(true)
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build();
        startForeground(UNLOCKED_NOTIFICATION_ID, notification);
    }

    private void checkUnlocked() {
        if (!mStorage.isUnlocked()) {
            throw new IllegalStateException("Storage is locked");
        }
    }

    class LocalBinder extends Binder {
        CredentialStorageApi getApi() {
            return CredentialStorageService.this;
        }
    }
}
