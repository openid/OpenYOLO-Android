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

import android.content.Context;
import android.os.Parcel;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.TestConstants.ValidEmailHint;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/** Unit tests for {@link Hint}*/
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public final class HintTest {

    private static final String ID = "coold00d@aol.gov";

    Context mContext;

    @Before
    public void initializeMocks() throws Exception {
        mContext = TestConstants.ValidApplication.install(RuntimeEnvironment.application);
    }

    @Test
    public void build_withRequiredParamsOnly() {
        Hint hint = new Hint.Builder(ID, AuthenticationMethods.EMAIL).build();

        assertThat(hint.getIdentifier()).isEqualTo(ID);
        assertThat(hint.getAuthenticationMethod()).isEqualTo(AuthenticationMethods.EMAIL);
        assertThat(hint.getDisplayName()).isNull();
        assertThat(hint.getDisplayPicture()).isNull();
        assertThat(hint.getIdToken()).isNull();
        assertThat(hint.getAdditionalProperties()).isEmpty();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void build_nullId_shouldThrowException() {
        new Hint.Builder(
                null,// id
                AuthenticationMethods.EMAIL)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void build_emptyId_shouldThrowException() {
        new Hint.Builder(
                "", // id
                AuthenticationMethods.EMAIL)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void build_nullAuthMethod_shouldThrowException() {
        new Hint.Builder(
                ID,
                (AuthenticationMethod) null)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void build_nullAuthMethodStr_shouldThrowException() {
        new Hint.Builder(
                ID,
                (String) null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void build_emptyAuthMethodStr_shouldThrowException() {
        new Hint.Builder(
                ID,
                "")
                .build();
    }

    @Test
    public void builderSetAdditionalProps() {
        String additionalKey = "extra";
        byte[] additionalValue = "value".getBytes();

        Map<String, byte[]> additionalProps = new HashMap<>();
        additionalProps.put(additionalKey, additionalValue);
        Hint hint = new Hint.Builder(ValidEmailHint.make())
                .setAdditionalProperties(additionalProps)
                .build();

        assertThat(hint.getAdditionalProperties()).hasSize(1);
        assertThat(hint.getAdditionalProperties()).containsKey(additionalKey);
        assertThat(hint.getAdditionalProperties().get(additionalKey)).isEqualTo(additionalValue);
    }

    @Test
    public void builder_withHintContructor_isEquivalent() {
        Hint hint = new Hint.Builder(ValidEmailHint.make()).build();

        ValidEmailHint.assertEquals(hint);
    }

    @Test
    public void toProtobuf_isEquivalent() {
        Protobufs.Hint proto = ValidEmailHint.make().toProtobuf();

        ValidEmailHint.assertEquals(proto);
    }

    @Test
    public void fromProtobuf_isEquivalent() throws Exception {
        Protobufs.Hint proto = ValidEmailHint.make().toProtobuf();
        Hint hint = Hint.fromProtobuf(proto);

        ValidEmailHint.assertEquals(hint);
    }

    @Test
    public void fromProtobufBytes_isEquivalent() throws Exception {
        Protobufs.Hint proto = ValidEmailHint.make().toProtobuf();
        Hint hint = Hint.fromProtobufBytes(proto.toByteArray());

        ValidEmailHint.assertEquals(hint);
    }

    @Test
    public void writeToAndReadFromParcel() {
        Hint hint = ValidEmailHint.make();

        Parcel p = Parcel.obtain();
        try {
            hint.writeToParcel(p, 0);
            p.setDataPosition(0);
            Hint readHint = Hint.CREATOR.createFromParcel(p);

            ValidEmailHint.assertEquals(readHint);
        } finally {
            p.recycle();
        }
    }

    @Test
    public void toCredentialBuilder_isEquivalent() {
        Credential credential = ValidEmailHint.make().toCredentialBuilder(mContext).build();

        ValidEmailHint.assertEquals(mContext, credential);
    }

    @Test
    public void describeContents() {
        Hint hint = ValidEmailHint.make();
        assertThat(hint.describeContents()).isEqualTo(0);
    }
}
