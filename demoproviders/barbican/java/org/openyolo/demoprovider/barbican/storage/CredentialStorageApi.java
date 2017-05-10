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

import java.io.IOException;
import java.util.List;
import org.openyolo.demoprovider.barbican.Protobufs.AccountHint;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.Protobufs.Credential;

/**
 * Credential storage operations which are available via the service interface.
 */
public interface CredentialStorageApi {

    /**
     * Determines whether a credential store exists in the initialized location.
     */
    boolean isCreated();

    /**
     * Creates a credential store at the initialized location, deriving an encryption key from
     * the specified password.
     * @throws IOException if the store could not be created.
     */
    void create(String password) throws IOException;

    /**
     * Determines whether the credential store is unlocked, meaning that the encryption key has
     * been derived from the password and is resident in memory.
     */
    boolean isUnlocked();

    /**
     * Unlocks the credential store, deriving an encryption key from the specified password and
     * validates it.
     * @throws IOException if the store could not be unlocked.
     */
    boolean unlock(String password) throws IOException;

    /**
     * Locks the credential store, meaning that the derived encryption key is released from
     * memory if necessary.
     */
    void lock();

    /**
     * Determines whether any of the provided authentication domains are on the "never save"
     * list.
     * @throws IOException if the credential store metadata could not be read.
     */
    boolean isOnNeverSaveList(List<AuthenticationDomain> authDomains) throws IOException;

    /**
     * Adds the specified authentication domain to the "never save" list.
     * @throws IOException if the credential store metadata could not be modified.
     */
    void addToNeverSaveList(AuthenticationDomain authDomain) throws IOException;

    /**
     * Removes the specified authentication domains from the "never save" list.
     * @throws IOException if the credential store metadata could not be modified.
     */
    void removeFromNeverSaveList(List<AuthenticationDomain> authDomains) throws IOException;

    /**
     * Returns the list of all account hints.
     * @throws IOException if the credential store metadata could not be modified.
     */
    List<AccountHint> getHints() throws IOException;

    /**
     * Returns a {@link List} of {@link AuthenticationDomain}s which are in the "never save" list.
     * @return a list of "never save" domains
     * @throws IOException - if the credential store metadata could not be read.
     */
    List<String> getNeverSaveList() throws IOException;

    /**
     * Clears the "never save" list.
     * @throws IOException - if the credential store metadata could not be read.
     */
    void clearNeverSaveList() throws IOException;

    /**
     * Determines whether any credentials are stored for the given authentication domain. This
     * does not require the credential store to be unlocked.
     * @throws IOException if the credential store could not be read.
     */
    boolean hasCredentialFor(String authDomain) throws IOException;

    /**
     * Returns a list of credentials for the specified authentication domain. Requires
     * that the credential store is unlocked. May be empty.
     * @throws IOException if the credentials could not be read.
     */
    List<Credential> listCredentials(List<AuthenticationDomain> authDomains) throws IOException;

    /**
     * Returns a list of all stored credentials. Requires that the credential store is unlocked.
     * @throws IOException if the credentials could not be read.
     */
    List<Credential> listAllCredentials() throws IOException;

    /**
     * Writes (or overwrites) the provided credential to the store. Requires that the credential
     * store is unlocked. May be empty.
     * @throws IOException if the credential could not be written.
     */
    void upsertCredential(Credential credential) throws IOException;

    /**
     * Removes the specified credential (or one with the same authentication domain, identifier
     * and "type") from the store. Requires that the store is unlocked.
     * @throws IOException if the credential could not be deleted.
     */
    void deleteCredential(Credential credential) throws IOException;

    /**
     * Determines whether the store contains a credential with the same authentication domain,
     * authentication method and identifier as the credential specified.
     * @throws IOException if the credential store could not be read.
     */
    boolean hasCredential(Credential credential) throws IOException;
}
