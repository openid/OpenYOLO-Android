package org.openyolo.spi;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import org.openyolo.protocol.AuthenticationDomain;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link AuthenticationDomain}.
 */
@Implements(AuthenticationDomain.class)
public class ShadowAuthenticationDomain {

    private static Map<String, AuthenticationDomain> sAuthDomainLookup = new HashMap<>();

    public static void reset() {
        sAuthDomainLookup.clear();
    }

    public static void setAuthDomainForPackage(String packageName, AuthenticationDomain authDomain) {
        sAuthDomainLookup.put(packageName, authDomain);
    }

    @Implementation
    public static AuthenticationDomain fromPackageName(
            @NonNull Context context,
            @NonNull String packageName) {
        return sAuthDomainLookup.get(packageName);
    }
}
