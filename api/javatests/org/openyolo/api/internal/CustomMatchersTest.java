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

package org.openyolo.api.internal;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;

import android.net.Uri;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.api.AuthenticationMethods;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Battery of tests for the custom Matchers suite extending hamcrest
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CustomMatchersTest {
    @Test
    public void isWebUri_http() throws Exception {
        assertTrue(CustomMatchers.isWebUri().matches(Uri.parse("http://www.blah.com")));
    }

    @Test
    public void isWebUri_https() throws Exception {
        assertTrue(CustomMatchers.isWebUri().matches(Uri.parse("https://www.blah.com")));
    }

    @Test
    public void isWebUri_noproto() throws Exception {
        assertFalse(CustomMatchers.isWebUri().matches(Uri.parse("www.blah.com")));
    }

    @Test
    public void isWebUri_ftp() throws Exception {
        assertFalse(CustomMatchers.isWebUri().matches(Uri.parse("ftp://www.blah.com")));
    }

    @Test
    public void isHttpsUri() throws Exception {
        assertTrue(CustomMatchers.isHttpsUri().matches(Uri.parse("https://www.blah.com")));
    }

    @Test
    public void isHttpsUri_withPath() {
        assertTrue(CustomMatchers.isHttpsUri().matches(
                Uri.parse("https://www.blah.com/resource")));
    }

    @Test
    public void isHttpsUri_withQuery() {
        assertTrue(CustomMatchers.isHttpsUri().matches(
                Uri.parse("https://www.blah.com/resource?a=b")));
    }

    @Test
    public void isHttpsUri_withFragment() {
        assertTrue(CustomMatchers.isHttpsUri().matches(
                Uri.parse("https://www.blah.com/resource#entry")));
    }

    @Test
    public void isHttpsUri_http() {
        assertFalse(CustomMatchers.isHttpsUri().matches(Uri.parse("http://www.blah.com")));
    }

    @Test
    public void isValidAuthenticationDomain_http() throws Exception {
        assertTrue(CustomMatchers.isValidAuthenticationDomain().matches(
                Uri.parse("http://www.blah.com")));
    }

    @Test
    public void isValidAuthenticationDomain_https() throws Exception {
        assertTrue(CustomMatchers.isValidAuthenticationDomain().matches(
                Uri.parse("https://www.blah.com")));
    }

    @Test
    public void isValidAuthenticationDomain_android() throws Exception {
        assertTrue(CustomMatchers.isValidAuthenticationDomain().matches(
                Uri.parse("android://signature@com.blah.app")));
    }

    @Test
    public void isValidAuthenticationDomain() throws Exception {
        assertFalse(CustomMatchers.isValidAuthenticationDomain().matches(
                Uri.parse("http://www.blah.com/resource")));
    }

    @Test
    public void isValidAuthenticationMethod_builtIn() throws Exception {
        assertTrue(CustomMatchers.isValidAuthenticationMethod().matches(
                AuthenticationMethods.ID_AND_PASSWORD));
    }

    @Test
    public void isValidAuthenticationMethod_custom() {
        assertTrue(CustomMatchers.isValidAuthenticationMethod().matches(
                Uri.parse("https://www.blah.com")));
    }

    @Test
    public void isValidAuthenticationMethod_path() {
        assertFalse(CustomMatchers.isValidAuthenticationMethod().matches(
                Uri.parse("http://www.blah.com/resource")));
    }

    @Test
    public void nullOr_null() {
        Matcher<Uri> matcher = CustomMatchers.nullOr(CustomMatchers.isHttpsUri());
        assertTrue(matcher.matches(null));
    }

    @Test
    public void nullOr_Https() {
        Matcher<Uri> matcher = CustomMatchers.nullOr(CustomMatchers.isHttpsUri());
        assertTrue(matcher.matches(Uri.parse("https://www.blah.com")));
    }
}