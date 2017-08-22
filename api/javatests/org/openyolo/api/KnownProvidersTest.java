/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openyolo.api;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openyolo.protocol.AuthenticationDomain;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Battery of tests for KnownProviders.
 * Future-proofing the predefined values for the hardcoded providers.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class KnownProvidersTest {

    @Mock
    private Context mockContext;
    @Mock
    private PackageManager mockPackageManager;

    private KnownProviders underTest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
        PackageInfo mockPackageInfo = new PackageInfo();
        Signature[] signatures = new Signature[1];
        signatures[0] = mock(Signature.class);
        when(signatures[0].toByteArray()).thenReturn(new byte[10]);
        mockPackageInfo.signatures = signatures;
        when(mockPackageManager.getPackageInfo(anyString(), anyInt())).thenReturn(mockPackageInfo);
        underTest = KnownProviders.getInstance(mockContext);
    }

    /**
     * Future-proofing the default providers.
     * This test exist ONLY to future proof the sourcecode and make sure the above info doesn't
     * change by chance. The signatures here should never change, so in order to make sure of that
     * I added a second copy of them in this test.
     * @throws Exception
     */
    @Test
    public void testGetKnownProviders() throws Exception {
        Set<AuthenticationDomain> all = underTest.getKnownProviders();
        assertTrue(all.containsAll(DEFAULT_KNOWN_PROVIDERS));
        assertEquals(all.size(), DEFAULT_KNOWN_PROVIDERS.size());
    }


    public void testIsKnown_null() throws Exception {
        underTest.isKnown(null);
    }

    @Ignore // too dificult to unit test this, as it works with the byte[] of the actual signature
    @Test
    public void testIsKnown_positive() throws Exception {
        assertFalse(underTest.isKnown("com.dashlane"));
    }

    @Test
    public void testIsKnown_negative() throws Exception {
        assertFalse(underTest.isKnown("bogus.tld"));
    }

    @Test
    public void testResetKnownProvidersToDefault() throws Exception {
        int originalSize = underTest.getKnownProviders().size();
        AuthenticationDomain barbican = new AuthenticationDomain("https://barbican.com");
        underTest.addKnownProvider(barbican);
        int newsize = underTest.getKnownProviders().size();
        assertEquals(newsize, originalSize+1);
        underTest.resetKnownProvidersToDefault();
        Set<AuthenticationDomain> all = underTest.getKnownProviders();
        assertTrue(all.containsAll(DEFAULT_KNOWN_PROVIDERS));
        assertEquals(all.size(), DEFAULT_KNOWN_PROVIDERS.size());
    }

    @Test
    public void testAddKnownProvider() throws Exception {
        int originalSize = underTest.getKnownProviders().size();
        AuthenticationDomain barbican = new AuthenticationDomain("https://barbican.com");
        underTest.addKnownProvider(barbican);
        int newsize = underTest.getKnownProviders().size();
        assertEquals(newsize, originalSize+1);
    }

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


}