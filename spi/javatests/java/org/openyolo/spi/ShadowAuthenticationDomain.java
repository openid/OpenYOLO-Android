package org.openyolo.spi;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import org.openyolo.api.AuthenticationDomain;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link AuthenticationDomain}
 */
@Implements(AuthenticationDomain.class)
public class ShadowAuthenticationDomain {

    private static List<AuthenticationDomain> authenticationDomains = Collections.EMPTY_LIST;

    public static void setListForPackageResponse(
            List<AuthenticationDomain> authenticationDomainsResponse) {
        authenticationDomains = authenticationDomainsResponse;
    }

    @Implementation
    public static List<AuthenticationDomain> listForPackage(
            @NonNull Context context,
            @Nullable String packageName) {
        return authenticationDomains;
    }
}
