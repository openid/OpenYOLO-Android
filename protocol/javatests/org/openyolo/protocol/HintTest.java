/*
 * Copyright 2017 The OpenYOLO Authors. All Rights Reserved.
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

import android.net.Uri;
import android.os.Parcel;
import java.io.IOException;
import java.net.IDN;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.valid4j.errors.RequireViolation;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class HintTest {

    private static final String ALICE_ID = "alice@example.com";
    private static final String ALICE_NAME = "Alice";
    private static final String ALICE_DISPLAY_PICTURE_URI_STR = "https://avatars.example.com/alice";
    private static final Uri ALICE_DISPLAY_PICTURE_URI = Uri.parse(ALICE_DISPLAY_PICTURE_URI_STR);
    private static final String PASSWORD = "correctH0rseBatterySt4ple";
    private static final String ID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
            + "eyJzdWIiOiJ5b2xvIn0."
            + "BI1g9ns0shv6PKfwlhfPKwh5XzxQyg_el_35_wZbtsI";

    @Test
    public void build_minimalData() {
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL).build();
        assertThat(hint.getIdentifier()).isEqualTo(ALICE_ID);
        assertThat(hint.getAuthenticationMethod()).isEqualTo(AuthenticationMethods.EMAIL);
        assertThat(hint.getDisplayName()).isNull();
        assertThat(hint.getDisplayPictureUri()).isNull();
        assertThat(hint.getIdToken()).isNull();
        assertThat(hint.getAdditionalProperties()).isEmpty();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void build_nullId_shouldThrowException() {
        new Hint.Builder(
                null,// id
                AuthenticationMethods.EMAIL)
                .build();
    }

    @Test(expected = RequireViolation.class)
    public void build_emptyId_shouldThrowException() {
        new Hint.Builder(
                "", // id
                AuthenticationMethods.EMAIL)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void build_nullAuthMethod_shouldThrowException() {
        new Hint.Builder(
                ALICE_ID,
                (AuthenticationMethod) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = RequireViolation.class)
    public void build_nullAuthMethodStr_shouldThrowException() {
        new Hint.Builder(
                ALICE_ID,
                (String) null)
                .build();
    }

    @Test(expected = RequireViolation.class)
    public void build_emptyAuthMethodStr_shouldThrowException() {
        new Hint.Builder(
                ALICE_ID,
                "")
                .build();
    }

    @Test
    public void builderSetDisplayName() {
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL)
                .setDisplayName(ALICE_NAME)
                .build();
        assertThat(hint.getDisplayName()).isEqualTo(ALICE_NAME);
    }

    @Test
    public void builderSetDisplayName_nullValue() {
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL)
                .setDisplayName(null)
                .build();

        assertThat(hint.getDisplayName()).isNull();
    }

    @Test
    public void builderSetDisplayName_emptyValue() {
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL)
                .setDisplayName("")
                .build();

        assertThat(hint.getDisplayName()).isNull();
    }

    @Test
    public void builderSetDisplayPictureUri() {
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL)
                .setDisplayPictureUri(ALICE_DISPLAY_PICTURE_URI)
                .build();
        assertThat(hint.getDisplayPictureUri()).isEqualTo(ALICE_DISPLAY_PICTURE_URI);
    }

    @Test
    public void builderSetDisplayPictureUri_nullValue() {
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL)
                .setDisplayPictureUri((Uri) null)
                .build();
        assertThat(hint.getDisplayPictureUri()).isNull();
    }

    @Test(expected=RequireViolation.class)
    public void builderSetDisplayPictureUri_nonHttp_shouldThrowException() {
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL)
                .setDisplayPictureUri(Uri.parse("gopher://hello"))
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void builderSetDisplayPictureUriStr() {
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL)
                .setDisplayPictureUri(ALICE_DISPLAY_PICTURE_URI_STR)
                .build();
        assertThat(hint.getDisplayPictureUri().toString()).isEqualTo(ALICE_DISPLAY_PICTURE_URI_STR);
    }

    @Test
    public void builderSetDisplayPictureUriStr_nullValue() {
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL)
                .setDisplayPictureUri((String) null)
                .build();
        assertThat(hint.getDisplayPictureUri()).isNull();
    }

    @Test
    public void builderSetDisplayPictureUriStr_emptyValue() {
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL)
                .setDisplayPictureUri("")
                .build();
        assertThat(hint.getDisplayPictureUri()).isNull();
    }

    @Test
    public void builderSetGeneratedPassword() {
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL)
                .setGeneratedPassword(PASSWORD)
                .build();

        assertThat(hint.getGeneratedPassword()).isEqualTo(PASSWORD);
    }

    @Test
    public void builderSetGeneratedPassword_nullValue() {
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL)
                .setGeneratedPassword(null)
                .build();

        assertThat(hint.getGeneratedPassword()).isNull();
    }

    @Test
    public void builderSetGeneratedPassword_emptyValue() {
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL)
                .setGeneratedPassword("")
                .build();

        assertThat(hint.getGeneratedPassword()).isNull();
    }

    @Test
    public void builderSetIdToken() {
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL)
                .setIdToken(ID_TOKEN)
                .build();

        assertThat(hint.getIdToken()).isEqualTo(ID_TOKEN);
    }

    @Test
    public void builderSetIdToken_nullValue() {
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL)
                .setIdToken(null)
                .build();

        assertThat(hint.getIdToken()).isNull();
    }

    @Test
    public void builderSetIdToken_emptyValue() {
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL)
                .setIdToken(null)
                .build();

        assertThat(hint.getIdToken()).isNull();
    }

    @Test
    public void builderSetAdditionalProps() {
        String additionalKey = "extra";
        byte[] additionalValue = "value".getBytes();

        Map<String, byte[]> additionalProps = new HashMap<>();
        additionalProps.put(additionalKey, additionalValue);
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL)
                .setAdditionalProperties(additionalProps)
                .build();

        assertThat(hint.getAdditionalProperties()).hasSize(1);
        assertThat(hint.getAdditionalProperties()).containsKey(additionalKey);
        assertThat(hint.getAdditionalProperties().get(additionalKey)).isEqualTo(additionalValue);
    }

    @Test
    public void toProtobuf() {
        Hint hint = new Hint.Builder(
                ALICE_ID,
                AuthenticationMethods.EMAIL)
                .setDisplayName(ALICE_NAME)
                .setDisplayPictureUri(ALICE_DISPLAY_PICTURE_URI)
                .setGeneratedPassword(PASSWORD)
                .setIdToken(ID_TOKEN)
                .build();

        Protobufs.Hint proto = hint.toProtobuf();
        assertThat(proto.getId()).isEqualTo(ALICE_ID);
        assertThat(proto.getAuthMethod().getUri())
                .isEqualTo(AuthenticationMethods.EMAIL.toString());
        assertThat(proto.getDisplayName()).isEqualTo(ALICE_NAME);
        assertThat(proto.getDisplayPictureUri()).isEqualTo(ALICE_DISPLAY_PICTURE_URI_STR);
        assertThat(proto.getGeneratedPassword()).isEqualTo(PASSWORD);
        assertThat(proto.getIdToken()).isEqualTo(ID_TOKEN);
    }

    @Test
    public void fromProtobuf() {
        Protobufs.Hint proto = Protobufs.Hint.newBuilder()
                .setId(ALICE_ID)
                .setAuthMethod(AuthenticationMethods.EMAIL.toProtobuf())
                .setDisplayName(ALICE_NAME)
                .setDisplayPictureUri(ALICE_DISPLAY_PICTURE_URI_STR)
                .setGeneratedPassword(PASSWORD)
                .setIdToken(ID_TOKEN)
                .build();

        Hint hint = Hint.fromProtobuf(proto);
        assertThat(hint.getIdentifier()).isEqualTo(ALICE_ID);
        assertThat(hint.getAuthenticationMethod()).isEqualTo(AuthenticationMethods.EMAIL);
        assertThat(hint.getDisplayName()).isEqualTo(ALICE_NAME);
        assertThat(hint.getDisplayPictureUri()).isEqualTo(ALICE_DISPLAY_PICTURE_URI);
        assertThat(hint.getGeneratedPassword()).isEqualTo(PASSWORD);
        assertThat(hint.getIdToken()).isEqualTo(ID_TOKEN);
    }

    @Test
    public void fromProtobufBytes() throws IOException {
        Protobufs.Hint proto = Protobufs.Hint.newBuilder()
                .setId(ALICE_ID)
                .setAuthMethod(AuthenticationMethods.EMAIL.toProtobuf())
                .setDisplayName(ALICE_NAME)
                .setDisplayPictureUri(ALICE_DISPLAY_PICTURE_URI_STR)
                .setGeneratedPassword(PASSWORD)
                .setIdToken(ID_TOKEN)
                .build();

        Hint hint = Hint.fromProtobufBytes(proto.toByteArray());
        assertThat(hint.getIdentifier()).isEqualTo(ALICE_ID);
        assertThat(hint.getAuthenticationMethod()).isEqualTo(AuthenticationMethods.EMAIL);
        assertThat(hint.getDisplayName()).isEqualTo(ALICE_NAME);
        assertThat(hint.getDisplayPictureUri()).isEqualTo(ALICE_DISPLAY_PICTURE_URI);
        assertThat(hint.getGeneratedPassword()).isEqualTo(PASSWORD);
        assertThat(hint.getIdToken()).isEqualTo(ID_TOKEN);
    }

    @Test
    public void writeToAndReadFromParcel() {
        Hint hint = new Hint.Builder(
                ALICE_ID,
                AuthenticationMethods.EMAIL)
                .setDisplayName(ALICE_NAME)
                .setDisplayPictureUri(ALICE_DISPLAY_PICTURE_URI)
                .setGeneratedPassword(PASSWORD)
                .setIdToken(ID_TOKEN)
                .build();

        Parcel p = Parcel.obtain();
        try {
            hint.writeToParcel(p, 0);
            p.setDataPosition(0);
            Hint readHint = Hint.CREATOR.createFromParcel(p);

            assertThat(hint.getIdentifier()).isEqualTo(ALICE_ID);
            assertThat(hint.getAuthenticationMethod()).isEqualTo(AuthenticationMethods.EMAIL);
            assertThat(hint.getDisplayName()).isEqualTo(ALICE_NAME);
            assertThat(hint.getDisplayPictureUri()).isEqualTo(ALICE_DISPLAY_PICTURE_URI);
            assertThat(hint.getGeneratedPassword()).isEqualTo(PASSWORD);
            assertThat(hint.getIdToken()).isEqualTo(ID_TOKEN);
        } finally {
            p.recycle();
        }
    }

    @Test
    public void describeContents() {
        Hint hint = new Hint.Builder(ALICE_ID, AuthenticationMethods.EMAIL).build();
        assertThat(hint.describeContents()).isEqualTo(0);
    }
}
