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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.protobuf.ByteString;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;
import org.openyolo.demoprovider.barbican.CredentialQualityScore;
import org.openyolo.demoprovider.barbican.Protobufs.AccountHint;
import org.openyolo.demoprovider.barbican.Protobufs.CredentialMeta;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.Protobufs.Credential;
import org.openyolo.protocol.Protobufs.CredentialList;
import org.spongycastle.crypto.digests.Blake2bDigest;
import org.spongycastle.crypto.generators.BCrypt;
import org.spongycastle.crypto.io.CipherInputStream;
import org.spongycastle.crypto.io.CipherOutputStream;

/**
 * An encrypted file-system based credential store. Uses bcrypt to derive an encryption key from
 * a password. Creates a directory for each authentication domain (using a hash of the domain)
 * under which the set of credentials for that domain is stored. This allows determining whether
 * a credential is available for a given authentication domain without the encryption key, which
 * is useful for answering OpenYOLO requests.
 */
public final class CredentialStorage {

    private static final String LOG_TAG = "CredentialStorage";

    private static final int SALT_SIZE = 16;
    private static final int BCRYPT_COST = 10;

    private static final int AUTH_DOMAIN_HASH_SIZE = 32;

    private static final char[] HEX_CHARS =
            { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static final byte[] KEY_VERIFICATION_BYTES =
            { 0, 1, 2, 3, 4, 5, 6, 5, 4, 3, 2, 1, 0 };
    private static final int HALF_BYTE_WIDTH = 4;
    private static final int HALF_BYTE_MASK = 0xF;

    @NonNull
    private SecureRandom mRandom;

    @NonNull
    private File mStoreDir;

    private byte[] mSalt;

    @Nullable
    private byte[] mKey;

    /**
     * Initializes a credential store in the default location for the application.
     * @throws IOException if the store could not be initialized.
     */
    public CredentialStorage(Context context) throws IOException {
        this(new SecureRandom(), new File(context.getFilesDir(), "store"));
    }

    /**
     * Initializes a credential store in the specified location.
     * @throws IOException if the store could not be initialized.
     */
    public CredentialStorage(
            @NonNull SecureRandom random,
            @NonNull File storeDir)
            throws IOException {
        mRandom = random;
        mStoreDir = storeDir;

        if (mStoreDir.exists()) {
            CredentialMeta credentialMeta = readCredentialMeta();
            mSalt = credentialMeta.getSalt().toByteArray();
        }
    }

    /**
     * Determines whether a credential store exists in the initialized location.
     */
    public boolean isCreated() {
        return mStoreDir.exists();
    }

    /**
     * Determines whether the credential store is unlocked, meaning that the encryption key has
     * been derived from the password and is resident in memory.
     */
    public boolean isUnlocked() {
        return mKey != null;
    }

    /**
     * Locks the credential store, meaning that the derived encryption key is released from
     * memory if necessary.
     */
    public void lock() {
        // overwrite the key with random data, then release it for garbage collection
        mRandom.nextBytes(mKey);
        mKey = null;
    }

    /**
     * Determines whether any of the specified authentication domains are on the never save list.
     * This operation can be performed without unlocking the credential store.
     * @throws IOException if the credential storage metadata cannot be read.
     */
    @Nullable
    public List<String> retrieveNeverSaveList() throws IOException {
        CredentialMeta meta = readCredentialMeta();
        return meta.getNeverSaveList();
    }

    /**
     * Clears the "never save" list.
     * @throws IOException - in the case when the storage fails to be written
     */
    public void clearNeverSaveList() throws IOException {
        CredentialMeta meta = readCredentialMeta();
        writeCredentialMeta(meta.toBuilder().clearNeverSave().build());
    }

    /**
     * Determines whether any of the specified authentication domains are on the never save list.
     * This operation can be performed without unlocking the credential store.
     * @throws IOException if the credential storage metadata cannot be read.
     */
    public boolean isOnNeverSaveList(List<AuthenticationDomain> authDomains) throws IOException {
        CredentialMeta meta = readCredentialMeta();
        if (meta.getNeverSaveCount() < 1) {
            return false;
        }

        HashSet<String> authDomainsAsStrings = new HashSet<>();
        for (AuthenticationDomain authDomain : authDomains) {
            authDomainsAsStrings.add(authDomain.toString());
        }

        for (String neverSaveDomain : meta.getNeverSaveList()) {
            if (authDomainsAsStrings.contains(neverSaveDomain)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds the specified authentication domain to the never save list.
     * @throws IOException if the credential storage metadata cannot be modified.
     */
    public void addToNeverSaveList(AuthenticationDomain authDomain) throws IOException {
        CredentialMeta meta = readCredentialMeta();

        TreeSet<String> neverSaveDomains = new TreeSet<>();
        for (String neverSaveDomain : meta.getNeverSaveList()) {
            neverSaveDomains.add(neverSaveDomain);
        }

        neverSaveDomains.add(authDomain.toString());

        writeCredentialMeta(
                meta.toBuilder()
                    .clearNeverSave()
                    .addAllNeverSave(neverSaveDomains)
                    .build());
    }

    /**
     * Removes the specified list of authentication domains from the never save list.
     * @throws IOException if the credential storage metadata cannot be modified.
     */
    public void removeFromNeverSaveList(List<AuthenticationDomain> authDomains) throws IOException {
        CredentialMeta meta = readCredentialMeta();

        TreeSet<String> neverSaveDomains = new TreeSet<>();
        for (String neverSaveDomain : meta.getNeverSaveList()) {
            neverSaveDomains.add(neverSaveDomain);
        }

        for (AuthenticationDomain authDomain : authDomains) {
            neverSaveDomains.remove(authDomain.toString());
        }

        writeCredentialMeta(
                meta.toBuilder()
                    .clearNeverSave()
                    .addAllNeverSave(neverSaveDomains)
                    .build());
    }

    /**
     * Returns all account hints. The list of hints is automatically maintained as part of the
     * other credential save / delete operations.
     * @throws IOException if the credential storage metadata cannot be modified.
     */
    public List<AccountHint> getHints() throws IOException {
        CredentialMeta meta = readCredentialMeta();
        return meta.getHintsList();
    }

    /**
     * Determines whether any credentials are stored for the given authentication domain. This
     * does not require the credential store to be unlocked.
     */
    public boolean hasCredentialFor(@NonNull String authDomain) {
        return getAuthDomainDir(authDomain).exists();
    }

    /**
     * Returns a list of all stored credentials. Requires that the credential store is unlocked.
     * @throws IOException if the credentials could not be read.
     */
    @NonNull
    public List<Credential> listAllCredentials() throws IOException {
        ArrayList<Credential> allCredentials = new ArrayList<>();
        checkUnlocked();
        for (File authDomainDir : mStoreDir.listFiles()) {
            if (!authDomainDir.isDirectory()) {
                continue;
            }

            List<Credential> credentials = readCredentials(authDomainDir);
            allCredentials.addAll(credentials);
        }

        return allCredentials;
    }

    /**
     * Returns a list of credentials for the specified authentication domain. Requires
     * that the credential store is unlocked. May be empty.
     * @throws IOException if the credentials could not be read.
     */
    @NonNull
    public List<Credential> listCredentials(String authDomain) throws IOException {
        checkUnlocked();
        return readCredentials(authDomain);
    }

    /**
     * Writes (or overwrites) the provided credential to the store. Requires that the credential
     * store is unlocked. May be empty.
     * @throws IOException if the credential could not be written.
     */
    public void upsertCredential(Credential credential) throws IOException {
        checkUnlocked();
        List<Credential> credentials = new ArrayList<>(
                readCredentials(credential.getAuthDomain().getUri()));

        // replace any existing equivalent
        ListIterator<Credential> it = credentials.listIterator();
        boolean existingFound = false;
        while (it.hasNext() && !existingFound) {
            Credential existing = it.next();
            if (credential.getId().equals(existing.getId())
                    && eq(credential.getAuthDomain(), existing.getAuthDomain())) {
                existingFound = true;
                it.set(credential);
            }
        }

        if (!existingFound) {
            it.add(credential);
        }

        writeCredentials(credential.getAuthDomain().getUri(), credentials);
        upsertHint(credential);
    }

    /**
     * Removes the specified credential (or one with the same authentication domain, identifier
     * and "type") from the store. Requires that the store is unlocked.
     * @throws IOException if the credential could not be deleted.
     */
    public void deleteCredential(Credential credential) throws IOException {
        checkUnlocked();
        List<Credential> credentials = new ArrayList<>(readCredentials(
                credential.getAuthDomain().getUri()));

        ListIterator<Credential> it = credentials.listIterator();
        boolean existingFound = false;
        while (it.hasNext() && !existingFound) {
            Credential existing = it.next();
            if (credentialsAreEquivalent(credential, existing)) {
                existingFound = true;
                it.remove();
            }
        }

        if (existingFound) {
            writeCredentials(credential.getAuthDomain().getUri(), credentials);
        }
    }

    private boolean credentialsAreEquivalent(Credential c1, Credential c2) {
        if (c1 == null) {
            return c2 == null;
        }

        if (c2 == null) {
            return false;
        }

        return c1.getId().equals(c2.getId())
                && c1.getAuthDomain().equals(c2.getAuthDomain())
                && c1.getAuthMethod().equals(c2.getAuthMethod());
    }

    private boolean areEqual(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    /* **************************** store initialization ******************************************/

    /**
     * Creates a credential store at the initialized location, deriving an encryption key from
     * the specified password.
     * @throws IOException if the store could not be created.
     */
    public void create(String password) throws IOException {
        if (!mStoreDir.mkdirs()) {
            throw new IllegalStateException("unable to create credential store directory");
        }

        CredentialMeta meta = generateCredentialMeta();
        writeCredentialMeta(meta);

        mSalt = meta.getSalt().toByteArray();
        mKey = keyFromPassword(password, meta.getCost());
        generateKeyTestFile();
    }

    /**
     * Unlocks the credential store, deriving an encryption key from the specified password and
     * validates it.
     * @throws IOException if the store could not be unlocked.
     */
    public boolean unlock(String password) throws IOException {
        if (!isCreated()) {
            throw new IllegalStateException("Store has not been created");
        }

        CredentialMeta credentialMeta = readCredentialMeta();
        mSalt = credentialMeta.getSalt().toByteArray();
        mKey = keyFromPassword(password, credentialMeta.getCost());
        return verifyKey();
    }

    /* **************************** credential meta handling **************************************/

    private File getCredentialMetaFile() {
        return new File(mStoreDir, "meta");
    }

    private CredentialMeta generateCredentialMeta() {
        return CredentialMeta.newBuilder()
                .setSalt(ByteString.copyFrom(generateRandomBytes(SALT_SIZE)))
                .setCost(BCRYPT_COST)
                .build();
    }

    private void writeCredentialMeta(CredentialMeta meta) throws IOException {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(getCredentialMetaFile());
            meta.writeTo(stream);
        } finally {
            IoUtil.closeQuietly(stream, LOG_TAG);
        }
    }

    private CredentialMeta readCredentialMeta() throws IOException {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(getCredentialMetaFile());
            return CredentialMeta.parseFrom(stream);
        } finally {
            IoUtil.closeQuietly(stream, LOG_TAG);
        }
    }

    private void upsertHint(Credential credential) throws IOException {
        CredentialMeta meta = readCredentialMeta();

        // create a copy of the hints list for modification
        List<AccountHint> hints = new ArrayList<>(readCredentialMeta().getHintsList());

        ListIterator<AccountHint> hintIter = hints.listIterator();
        boolean found = false;
        while (!found && hintIter.hasNext()) {
            AccountHint existingHint = hintIter.next();

            if (existingHint.getIdentifier().equals(credential.getId())
                    && existingHint.getAuthMethod().equals(credential.getAuthMethod())
                    && CredentialQualityScore.getScore(existingHint)
                            <= CredentialQualityScore.getScore(credential)) {
                hintIter.remove();
                hintIter.add(convertCredentialToHint(credential));
                found = true;
            }
        }

        if (!found) {
            hintIter.add(convertCredentialToHint(credential));
        }

        writeCredentialMeta(meta.toBuilder()
                .clearHints()
                .addAllHints(hints)
                .build());
    }

    private AccountHint convertCredentialToHint(Credential credential) {
        return AccountHint.newBuilder()
                .setIdentifier(credential.getId())
                .setAuthMethod(credential.getAuthMethod().getUri())
                .setName(credential.getDisplayName())
                .setPictureUri(credential.getDisplayPictureUri())
                .build();
    }

    /* **************************** key validation ************************************************/

    private File getKeyTestFile() {
        return new File(mStoreDir, "test");
    }

    private void generateKeyTestFile() throws IOException {
        CipherOutputStream stream = null;
        try {
            stream = IoUtil.encryptTo(getKeyTestFile(), mKey);
            stream.write(KEY_VERIFICATION_BYTES);
        } finally {
            IoUtil.closeQuietly(stream, LOG_TAG);
        }
    }

    private boolean verifyKey() throws IOException {
        CipherInputStream stream = null;
        try {
            stream = IoUtil.decryptFrom(getKeyTestFile(), mKey);
            byte[] bytes = new byte[KEY_VERIFICATION_BYTES.length];
            int numRead = stream.read(bytes);
            return numRead == KEY_VERIFICATION_BYTES.length
                && bytesEqual(KEY_VERIFICATION_BYTES, bytes);
        } catch (IOException ex) {
            Log.i(LOG_TAG, "Key verification failed");
            return false;
        } finally {
            IoUtil.closeQuietly(stream, LOG_TAG);
        }
    }

    /* ************************** auth domain directory management ********************************/

    private File getAuthDomainDir(String authDomain) {
        return new File(mStoreDir, toBlakeHash(authDomain));
    }

    private File initAuthDomainDir(String authDomain) throws IOException {
        File authorityDir = getAuthDomainDir(authDomain);
        if (!authorityDir.exists()) {
            if (!authorityDir.mkdirs()) {
                throw new IOException("failed to create authority dir");
            }
        }

        return authorityDir;
    }

    private void deleteAuthDomainDir(String authDomain) throws IOException {
        File credsFile = getCredentialsFile(authDomain);
        if (credsFile.exists()) {
            if (!credsFile.delete()) {
                throw new IOException("failed to delete credentials file");
            }
        }

        File authorityDir = getAuthDomainDir(authDomain);
        if (authorityDir.exists()) {
            if (!authorityDir.delete()) {
                throw new IOException("failed to delete authority directory");
            }
        }
    }

    private File getCredentialsFile(String authDomain) {
        return getCredentialsFile(getAuthDomainDir(authDomain));
    }

    private File getCredentialsFile(File authDomainDir) {
        return new File(authDomainDir, "creds");
    }

    /* **************************** credential manipulation ***************************************/

    private void writeCredentials(String authDomain, List<Credential> credentials)
            throws IOException {
        if (credentials.size() == 0) {
            deleteAuthDomainDir(authDomain);
            return;
        }

        initAuthDomainDir(authDomain);
        CipherOutputStream stream = null;
        try {
            stream = IoUtil.encryptTo(getCredentialsFile(authDomain), mKey);
            CredentialList proto = CredentialList.newBuilder()
                    .addAllCredentials(credentials)
                    .build();

            proto.writeTo(stream);
        } finally {
            IoUtil.closeQuietly(stream, LOG_TAG);
        }
    }

    private List<Credential> readCredentials(String authDomain) throws IOException {
        return readCredentials(getAuthDomainDir(authDomain));
    }

    private List<Credential> readCredentials(File authDomain) throws IOException {
        File credentialsFile = getCredentialsFile(authDomain);
        if (!credentialsFile.exists()) {
            return Collections.emptyList();
        }

        CipherInputStream stream = null;
        try {
            stream = IoUtil.decryptFrom(credentialsFile, mKey);
            return CredentialList.parseFrom(stream).getCredentialsList();
        } finally {
            IoUtil.closeQuietly(stream, LOG_TAG);
        }
    }

    /* *********************************** utils **************************************************/

    private void checkUnlocked() {
        if (!isUnlocked()) {
            throw new IllegalStateException("credential store is locked");
        }
    }

    private byte[] keyFromPassword(String password, int cost) {
        return BCrypt.generate(toUtf8Bytes(password), mSalt, cost);
    }

    private static byte[] toUtf8Bytes(String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 character set not supported, against spec");
        }
    }

    private byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        mRandom.nextBytes(bytes);
        return bytes;
    }

    private boolean bytesEqual(byte[] b1, byte[] b2) {
        if (b1 == null) {
            return b2 == null;
        }

        if (b2 == null || b1.length != b2.length) {
            return false;
        }

        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i]) {
                return false;
            }
        }

        return true;
    }

    private String toBlakeHash(String message) {
        Blake2bDigest digest = new Blake2bDigest(
                null, /* no key */
                AUTH_DOMAIN_HASH_SIZE,
                mSalt,
                null); /* no personalization of the hashing function */
        byte[] messageBytes = toUtf8Bytes(message);
        digest.update(messageBytes, 0, messageBytes.length);
        byte[] result = new byte[AUTH_DOMAIN_HASH_SIZE];
        digest.doFinal(result, 0);
        return toHexString(result);
    }

    private String toHexString(byte[] bytes) {
        char[] result = new char[bytes.length * 2];
        for (int i = 0, j = 0; i < bytes.length; i++, j += 2) {
            byte nextByte = bytes[i];

            result[j] = HEX_CHARS[(nextByte >>> HALF_BYTE_WIDTH) & HALF_BYTE_MASK];
            result[j + 1] = HEX_CHARS[nextByte & HALF_BYTE_MASK];
        }
        return new String(result);
    }

    private boolean eq(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        }
        return o2 != null && o1.equals(o2);
    }
}
