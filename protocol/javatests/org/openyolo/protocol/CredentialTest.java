/*
 * Copyright 2016 The OpenYOLO Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openyolo.protocol;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.linesOf;
import static org.openyolo.protocol.AuthenticationMethods.EMAIL;

import android.net.Uri;
import android.os.Parcel;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.TestConstants.ValidFacebookCredential;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Tests for {@link Credential}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CredentialTest {

    private static final String EMAIL_ID = "alice@example.com";
    private static final String AUTH_DOMAIN_STR = "https://www.example.com";
    private static final AuthenticationDomain AUTH_DOMAIN =
            new AuthenticationDomain(AUTH_DOMAIN_STR);
    private static final String ID_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NSIsImF1ZCI"
                    + "6IlhZWi5jbGllbnQuaWQiLCJpc3MiOiJpZHAuZXhhbXBsZS5jb20iLCJlbWFpbCI"
                    + "6ImFsaWNlQGV4YW1wbGUuY29tIiwibmFtZSI6IkFsaWNlIE1jVGVzdGVyc29uIiw"
                    + "icGljdHVyZSI6Imh0dHBzOi8vcm9ib2hhc2gub3JnL2FsaWNlIn0.2-D7AZ1C7mv"
                    + "dLRf6Q7aqH8Ah4rlK1uuHPSU2HPImtyk";

    private static final String ADDITIONAL_PROP_TEST_KEY = "testKey";
    private static final String ADDITIONAL_PROP_ANOTHER_KEY = "anotherKey";

    private static final byte[] ADDITIONAL_PROP_TWO_BYTE_VALUE = new byte[] { 0, 1 };
    private static final byte[] ADDITIONAL_PROP_ZERO_BYTE_VALUE = new byte[] {};
    private static final String ADDITIONAL_PROP_STRING_VALUE = "value";

    @Test
    public void fromProtobuf_withValidCredential_returnsEquivalentCredential() throws Exception {
        Credential credential =
                Credential.fromProtobuf(ValidFacebookCredential.make().toProtobuf());

        ValidFacebookCredential.assertEqualTo(credential);
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("ConstantConditions")
    public void testBuilder_nullIdentifier() {
        new Credential.Builder(null, EMAIL, AUTH_DOMAIN);
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("ConstantConditions")
    public void testBuilder_nullAuthMethod() {
        new Credential.Builder(EMAIL_ID, null, AUTH_DOMAIN);
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("ConstantConditions")
    public void testBuilder_nullAuthDomain() {
        new Credential.Builder(EMAIL_ID, EMAIL, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilder_emptyIdentifier() {
        new Credential.Builder("", EMAIL, AUTH_DOMAIN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilder_emptyAuthMethod() {
        new Credential.Builder(EMAIL_ID, "", AUTH_DOMAIN_STR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilder_authMethodWithoutScheme() {
        new Credential.Builder(EMAIL_ID, "www.example.com", AUTH_DOMAIN_STR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilder_authMethodWithPath() {
        new Credential.Builder(EMAIL_ID, "https://www.example.com/path", AUTH_DOMAIN_STR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilder_authMethodWithQuery() {
        new Credential.Builder(EMAIL_ID, "https://www.example.com?a=b", AUTH_DOMAIN_STR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilder_authMethodWithFragment() {
        new Credential.Builder(EMAIL_ID, "https://www.example.com#a", AUTH_DOMAIN_STR);
    }

    @Test
    public void testBuilder_mandatoryPropsOnly() {
        Credential cr = new Credential.Builder(
                EMAIL_ID,
                EMAIL,
                AUTH_DOMAIN).build();
        assertThat(cr).isNotNull();
        assertThat(cr.getIdentifier()).isEqualTo(EMAIL_ID);
        assertThat(cr.getAuthenticationMethod()).isEqualTo(EMAIL);
        assertThat(cr.getAuthenticationDomain()).isEqualTo(AUTH_DOMAIN);
        assertThat(cr.getDisplayName()).isNull();
        assertThat(cr.getDisplayPicture()).isNull();
        assertThat(cr.getAdditionalProperties()).isEmpty();
    }

    @Test
    public void testBuilder_setIdentifier() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setIdentifier("bob@example.com")
                .build();
        assertThat(cr.getIdentifier()).isEqualTo("bob@example.com");
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("ConstantConditions")
    public void testBuilder_setIdentifier_toNull() {
        new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
            .setIdentifier(null)
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilder_setIdentifier_toEmpty() {
        new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setIdentifier("")
                .build();
    }

    @Test
    public void testBuilder_setPassword() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setPassword("CorrectHorseBatteryStaple")
                .build();
        assertThat(cr.getPassword()).isEqualTo("CorrectHorseBatteryStaple");
    }

    @Test
    public void testBuilder_setPassword_twice() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setPassword("CorrectHorseBatteryStaple")
                .setPassword("password1")
                .build();
        assertThat(cr.getPassword()).isEqualTo("password1");
    }

    @Test
    public void testBuilder_setPassword_toNull() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setPassword("CorrectHorseBatteryStaple")
                .setPassword(null)
                .build();
        assertThat(cr.getPassword()).isNull();
    }

    @Test
    public void testBuilder_setPassword_toEmpty() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setPassword("")
                .build();

        assertThat(cr.getDisplayName()).isNull();
    }

    @Test
    public void builderSetIdToken_validInput_shouldSucceed() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setIdToken(ID_TOKEN)
                .build();

        assertThat(cr.getIdToken()).isEqualTo(ID_TOKEN);
    }

    @Test
    public void builderSetIdToken_nullValue_shouldSucceed() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setIdToken(null)
                .build();

        assertThat(cr.getIdToken()).isNull();
    }

    @Test
    public void builderSetIdToken_emptyValue_shouldSucceed() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setIdToken("")
                .build();

        assertThat(cr.getIdToken()).isNull();
    }

        @Test
    public void testBuilder_setDisplayName() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setDisplayName("Alice")
                .build();
        assertThat(cr.getDisplayName()).isEqualTo("Alice");
    }

    @Test
    public void testBuilder_setDisplayName_twice() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setDisplayName("Alice")
                .setDisplayName("Alicia")
                .build();
        assertThat(cr.getDisplayName()).isEqualTo("Alicia");
    }

    @Test
    public void testBuilder_setDisplayName_toNull() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setDisplayName("Alice")
                .setDisplayName(null)
                .build();
        assertThat(cr.getDisplayName()).isNull();
    }

    @Test
    public void testBuilder_setDisplayName_toEmpty() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setDisplayName("")
                .build();

        assertThat(cr.getDisplayName()).isNull();
    }

    public void testBuilder_setDisplayPicture() {
        Uri pictureUri = Uri.parse("https://robohash.org/alice@example.com");
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setDisplayPicture(pictureUri)
                .build();

        assertThat(cr.getDisplayPicture()).isSameAs(pictureUri);
    }

    public void testBuilder_setDisplayPicture_twice() {
        Uri first = Uri.parse("https://robohash.org/alice@example.com");
        Uri second = Uri.parse("https://robohash.org/bob@example.com");
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setDisplayPicture(first)
                .setDisplayPicture(second)
                .build();

        assertThat(cr.getDisplayPicture()).isSameAs(second);
    }

    public void testBuilder_setDisplayPicture_toNull() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setDisplayPicture(Uri.parse("https://robohash.org/alice@example.com"))
                .setDisplayPicture((Uri)null)
                .build();

        assertThat(cr.getDisplayPicture()).isNull();
    }

    @Test
    public void testBuilder_setAuthenticationMethod() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setAuthenticationMethod(AuthenticationMethods.GOOGLE)
                .build();

        assertThat(cr.getAuthenticationMethod()).isEqualTo(AuthenticationMethods.GOOGLE);
    }

    @Test
    public void testBuilder_setAdditionalProperty() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setAdditionalProperty(ADDITIONAL_PROP_TEST_KEY, ADDITIONAL_PROP_TWO_BYTE_VALUE)
                .setAdditionalProperty(ADDITIONAL_PROP_ANOTHER_KEY, ADDITIONAL_PROP_ZERO_BYTE_VALUE)
                .build();

        Map<String, byte[]> additionalProps = cr.getAdditionalProperties();
        assertThat(additionalProps.size()).isEqualTo(2);
        assertThat(additionalProps.containsKey(ADDITIONAL_PROP_TEST_KEY));
        assertThat(additionalProps.get(ADDITIONAL_PROP_TEST_KEY))
                .isEqualTo(ADDITIONAL_PROP_TWO_BYTE_VALUE);
        assertThat(additionalProps.containsKey(ADDITIONAL_PROP_ANOTHER_KEY));
        assertThat(additionalProps.get(ADDITIONAL_PROP_ANOTHER_KEY))
                .isEqualTo(ADDITIONAL_PROP_ZERO_BYTE_VALUE);
    }

    @Test
    public void testBuilder_setAdditionalProperty_overwriteExistingValue() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setAdditionalProperty(ADDITIONAL_PROP_TEST_KEY, ADDITIONAL_PROP_TWO_BYTE_VALUE)
                .setAdditionalProperty(ADDITIONAL_PROP_TEST_KEY, ADDITIONAL_PROP_ZERO_BYTE_VALUE)
                .build();

        Map<String, byte[]> additionalProps = cr.getAdditionalProperties();
        assertThat(additionalProps.size()).isEqualTo(1);
        assertThat(additionalProps.containsKey(ADDITIONAL_PROP_TEST_KEY));
        assertThat(additionalProps.get(ADDITIONAL_PROP_TEST_KEY))
                .isEqualTo(ADDITIONAL_PROP_ZERO_BYTE_VALUE);
    }

    @Test
    public void testBuilder_setAdditionalPropertyAsString() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setAdditionalPropertyAsString(
                        ADDITIONAL_PROP_TEST_KEY,
                        ADDITIONAL_PROP_STRING_VALUE)
                .build();

        Map<String, byte[]> additionalProps = cr.getAdditionalProperties();
        assertThat(additionalProps.size()).isEqualTo(1);
        assertThat(additionalProps.containsKey(ADDITIONAL_PROP_TEST_KEY));
        assertThat(additionalProps.get(ADDITIONAL_PROP_TEST_KEY))
                .isEqualTo(AdditionalPropertiesHelper.encodeStringValue(
                        ADDITIONAL_PROP_STRING_VALUE));
    }

    @Test
    public void testGetProto() {
        Credential cr = new Credential.Builder(
                EMAIL_ID,
                EMAIL,
                new AuthenticationDomain("https://www.example.com")).build();
        Protobufs.Credential proto = cr.toProtobuf();
        assertThat(proto).isNotNull();
    }

    @Test
    public void testWriteAndRead(){
        Credential cr = new Credential.Builder(
                EMAIL_ID,
                EMAIL,
                new AuthenticationDomain("https://www.example.com"))
                .setDisplayName("Alice")
                .setPassword("h4ckm3")
                .setDisplayPicture("https://www.robohash.org/alice")
                .build();

        // TODO: add some additional props and ensure these are preserved

        Parcel p = Parcel.obtain();
        cr.writeToParcel(p, 0);
        p.setDataPosition(0);
        Credential readCredential = Credential.CREATOR.createFromParcel(p);

        assertThat(readCredential.getIdentifier())
                .isEqualTo(cr.getIdentifier());
        assertThat(readCredential.getAuthenticationMethod())
                .isEqualTo(cr.getAuthenticationMethod());
        assertThat(readCredential.getAuthenticationDomain())
                .isEqualTo(cr.getAuthenticationDomain());
        assertThat(readCredential.getDisplayName())
                .isEqualTo(cr.getDisplayName());
        assertThat(readCredential.getDisplayPicture())
                .isEqualTo(cr.getDisplayPicture());
        assertThat(readCredential.getPassword())
                .isEqualTo(cr.getPassword());
    }

    @Test
    public void testGetAdditionalProperty() {
        String key = "testKey";
        byte[] value = new byte[] { 0, 1 };
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setAdditionalProperties(ImmutableMap.of(key, value))
                .build();

        assertThat(cr.getAdditionalProperty(key)).isEqualTo(value);
    }

    @Test
    public void testGetAdditionalProperty_withMissingKey() {
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN).build();
        assertThat(cr.getAdditionalProperty("missingKey")).isNull();
    }

    @Test
    public void testGetAdditionalPropertyAsString() {
        String key = "testKey";
        byte[] value = AdditionalPropertiesHelper.encodeStringValue("testValue");
        Credential cr = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN)
                .setAdditionalProperties(ImmutableMap.of(key, value))
                .build();

        assertThat(cr.getAdditionalPropertyAsString(key)).isEqualTo("testValue");
    }
}
