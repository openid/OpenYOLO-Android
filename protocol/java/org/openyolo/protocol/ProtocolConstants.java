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

package org.openyolo.protocol;

/**
 * Defines key constants that are used to identify data carried in intents.
 */
public final class ProtocolConstants {

    /**
     * The data type used for credential queries using the BBQ protocol.
     */
    public static final String CREDENTIAL_DATA_TYPE = "org.openyolo.credential";

    /**
     * The extra value key used to store the credential in a retrieve intent response.
     */
    public static final String EXTRA_CREDENTIAL = "org.openyolo.retrieve.credential";

    /**
     * The extra value key used to carry a credential retrieve request.
     */
    public static final String EXTRA_RETRIEVE_REQUEST = "org.openyolo.credential.retrieve.request";

    /**
     * The extra value key used to carry a credential retrieve result.
     */
    public static final String EXTRA_RETRIEVE_RESULT = "org.openyolo.credential.retrieve.result";

    /**
     * The extra key value used to carry a hint request.
     */
    public static final String EXTRA_HINT_REQUEST = "org.openyolo.hint.request";

    /**
     * The extra key value used to carry a hint result.
     */
    public static final String EXTRA_HINT_RESULT = "org.openyolo.hint.result";

    /**
     * The category used for all OpenYOLO-related intents.
     */
    public static final String OPENYOLO_CATEGORY = "org.openyolo";

    /**
     * The action used for credential retrieval request intents.
     */
    public static final String RETRIEVE_CREDENTIAL_ACTION = "org.openyolo.credential.retrieve";

    /**
     * The action used for hint intents.
     */
    public static final String HINT_CREDENTIAL_ACTION = "org.openyolo.hint";

    /**
     * The action used for save intents.
     */
    public static final String SAVE_CREDENTIAL_ACTION = "org.openyolo.save";


    private ProtocolConstants() {
        throw new IllegalStateException("not intended to be constructed");
    }
}
