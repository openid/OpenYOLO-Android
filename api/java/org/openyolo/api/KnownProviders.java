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

package org.openyolo.api;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.openyolo.protocol.AuthenticationDomain;

/**
 * Manages a list of known providers, identified by their package name and a hash of their
 * signing certificate. This class will attempt to dynamically retrieve a list of known providers
 * from openyolo.org if possible, otherwise it will fall back to the list of known providers at
 * compile time.
 */
public class KnownProviders {

    /**
     * The known package name and certificate hash for
     * <a href="https://www.dashlane.com">Dashlane</a>.
     */
    public static final AuthenticationDomain DASHLANE_PROVIDER =
            new AuthenticationDomain("android://"
                    + "DcxjRReUBVOOF1ztasdT8TO_5z-2aFWBTliZC8pMuy0r"
                    + "QomVAPv88RfGomI4dJS2CEVNJuu1jSIGBamB1Ni9iw=="
                    + "@com.dashlane");

    /**
     * The known package name and certificate hash for Google's
     * <a href="https://developers.google.com/identity/smartlock-passwords/android/">
     * Smart Lock for Passwords</a>.
     */
    public static final AuthenticationDomain GOOGLE_PROVIDER =
            new AuthenticationDomain("android://"
                    + "7fmduHKTdHHrlMvldlEqAIlSfii1tl35bxj1OXN5Ve8c"
                    + "4lU6URVu4xtSHc3BVZxS6WWJnxMDhIfQN0N0K2NDJg=="
                    + "@com.google.android.gms");

    /**
     * The known package name and certificate hash for
     * <a href="https://keepersecurity.com/">Keeper</a>.
     */
    public static final AuthenticationDomain KEEPER_PROVIDER =
            new AuthenticationDomain("android://"
                    + "qLhgSEs508k28WNBOalEFKqiNiUsWQ81o-OKOc9i__pf"
                    + "APc-eCrhdbQe9Gak2DopEEsI6rc12KwmPYoaNg-zEg=="
                    + "@com.callpod.android_apps.keeper");

    /**
     * The known package name and certificate hash for
     * <a href="https://www.lastpass.com/">LastPass</a>.
     */
    public static final AuthenticationDomain LASTPASS_PROVIDER =
            new AuthenticationDomain("android://"
                    + "d5XXKGMGcVvMZ7bw3-Aotgq035ClbqO7RwDQG7x6P7of"
                    + "wLxW42VRYL8jScbFfyW7hLyXYZEmrPrPsYqkJfDeNQ=="
                    + "@com.lastpass.lpandroid");

    /**
     * The known package name and certificate hash for
     * <a href="https://1password.com/">1Password</a>.
     */
    public static final AuthenticationDomain ONEPASSWORD_PROVIDER =
            new AuthenticationDomain("android://"
                    + "13u4RbkHxfV1nNgX9TJADGCzjyANu3HBL6IPPj8LO82U"
                    + "iGvPNYngjSJfIWT-FsxaaEGz0QKEqrhgtlxM-DF8ow=="
                    + "@com.agilebits.onepassword");

    /**
     * The known package name and certificate hash for
     * <a href="https://www.roboform.com/">Roboform</a>.
     */
    public static final AuthenticationDomain ROBOFORM_PROVIDER =
            new AuthenticationDomain("android://"
                    + "JY5BCpB1lKVw_KSpeji4Pp9znAYiho9rDyETFaAC-nCM"
                    + "hNpekHTlp45wMt7YDwe8FcMW5wrSBYLWeKEIdes77g=="
                    + "@com.siber.roboform");

    /**
     * The list of known providers at the time this library was compiled.
     */
    public static final Set<AuthenticationDomain> DEFAULT_KNOWN_PROVIDERS =
            Collections.unmodifiableSet(new HashSet<AuthenticationDomain>(Arrays.asList(
                    DASHLANE_PROVIDER,
                    GOOGLE_PROVIDER,
                    KEEPER_PROVIDER,
                    LASTPASS_PROVIDER,
                    ONEPASSWORD_PROVIDER,
                    ROBOFORM_PROVIDER
            )));

    private static final AtomicReference<KnownProviders> INSTANCE_REF =
            new AtomicReference<>();

    /**
     * Retrieves the singleton instance of the known provider list.
     */
    public static KnownProviders getApplicationBoundInstance(Context context) {
        KnownProviders providers = new KnownProviders(context);
        if (!INSTANCE_REF.compareAndSet(null, providers)) {
            providers = INSTANCE_REF.get();
        }

        return providers;
    }

    /**
     * FOR TESTING ONLY - Overrides the KnownProvider instance. It is *strongly discouraged*
     * to use this method in anything other than unit tests, as it can compromise the security
     * of your app and the user's credentials.
     */
    @VisibleForTesting
    static void setApplicationBoundInstance(KnownProviders instance) {
        INSTANCE_REF.set(instance);
    }

    /**
     * FOR TESTING ONLY - Clears the current instance of KnownProvider.
     */
    @VisibleForTesting
    static void clearApplicationBoundInstance() {
        INSTANCE_REF.set(null);
    }

    private final Context mApplicationContext;
    private Set<AuthenticationDomain> mKnownProviders;

    private KnownProviders(Context context) {
        mApplicationContext = context.getApplicationContext();

        // TODO: implement dynamic retrieval of known providers from
        // https://www.openyolo.org/known-providers
        // and fall back to the static list only if this fails
        mKnownProviders = DEFAULT_KNOWN_PROVIDERS;
    }

    public Set<AuthenticationDomain> getKnownProviders() {
        return new HashSet<>(mKnownProviders);
    }

    /**
     * Determines whether the application with the specified package name (if installed) is
     * on the known provider list.
     */
    public boolean isKnown(String packageName) {
        List<AuthenticationDomain> packageAuthDomains =
                AuthenticationDomain.listForPackage(mApplicationContext, packageName);

        for (AuthenticationDomain packageAuthDomain : packageAuthDomains) {
            if (mKnownProviders.contains(packageAuthDomain)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Reset the list of known providers to the default provider set. If possible, this will be
     * dynamically updated with a list retrieved from openyolo.org.
     *
     * <p><em>NOTE</em>: it is not typically necessary to ever call this method for anything other
     * than a unit test or test app.
     */
    @VisibleForTesting
    public void resetKnownProvidersToDefault() {
        mKnownProviders = DEFAULT_KNOWN_PROVIDERS;
    }

    /**
     * Adds a provider to the known provider list.
     *
     * <p><em>NOTE</em>: it is not typically necessary to ever call this method for anything other
     * than a unit test or test app.
     */
    @VisibleForTesting
    public void addKnownProvider(AuthenticationDomain provider) {
        HashSet<AuthenticationDomain> knownProviders = new HashSet<>(mKnownProviders);
        knownProviders.add(provider);
        mKnownProviders = Collections.unmodifiableSet(knownProviders);
    }
}
