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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.openyolo.api.PasswordSpecification.ALPHANUMERIC;
import static org.openyolo.api.PasswordSpecification.ALPHANUMERIC_DISTINGUISHABLE;
import static org.openyolo.api.PasswordSpecification.LOWER_ALPHA;
import static org.openyolo.api.PasswordSpecification.LOWER_ALPHA_DISTINGUISHABLE;
import static org.openyolo.api.PasswordSpecification.NUMERALS;
import static org.openyolo.api.PasswordSpecification.PASSWORD_CONFORMS;
import static org.openyolo.api.PasswordSpecification.PASSWORD_DISALLOWED_CHARACTER;
import static org.openyolo.api.PasswordSpecification.PASSWORD_LENGTH_MISMATCH;
import static org.openyolo.api.PasswordSpecification.PASSWORD_REQUIRED_CHARACTER_MISSING;
import static org.openyolo.api.PasswordSpecification.UPPER_ALPHA;
import static org.openyolo.api.PasswordSpecification.UPPER_ALPHA_DISTINGUISHABLE;

import android.os.Parcel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Battery of tests for the PasswordSpecification
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class PasswordSpecificationTest {

    @Test(expected = PasswordSpecification.InvalidSpecificationError.class)
    public void testBuild_noSizeSpecified() {
        new PasswordSpecification.Builder()
                .allow("abc")
                .build();
    }

    @Test(expected = PasswordSpecification.InvalidSpecificationError.class)
    public void testBuild_noAllowedSpecified() {
        new PasswordSpecification.Builder()
                .ofLength(3, 4)
                .build();
    }

    @Test(expected = PasswordSpecification.InvalidSpecificationError.class)
    public void testBuild_allow_emptyString() {
        new PasswordSpecification.Builder()
                .ofLength(1, 5)
                .allow("")
                .build();
    }

    @Test(expected = PasswordSpecification.InvalidSpecificationError.class)
    public void testBuild_allow_nonAsciiChar() {
        new PasswordSpecification.Builder()
                .ofLength(1, 5)
                .allow("aéo")
                .build();
    }

    @Test(expected = PasswordSpecification.InvalidSpecificationError.class)
    public void testBuild_ofLength_minimumSizeNotPositive() {
        new PasswordSpecification.Builder()
                .ofLength(0, 5)
                .build();
    }

    @Test(expected = PasswordSpecification.InvalidSpecificationError.class)
    public void testBuild_ofLength_invalidRange() {
        new PasswordSpecification.Builder()
                .ofLength(5, 4)
                .allow("abc")
                .build();
    }

    @Test(expected = PasswordSpecification.InvalidSpecificationError.class)
    public void testBuild_require_countNotPositive() {
        new PasswordSpecification.Builder()
                .ofLength(1, 5)
                .allow("abc")
                .require("a", 0)
                .build();
    }

    @Test(expected = PasswordSpecification.InvalidSpecificationError.class)
    public void testBuild_require_setsNotDisjoint() {
        new PasswordSpecification.Builder()
                .ofLength(1, 5)
                .allow("abc")
                .require("ab", 1)
                .require("bc", 1)
                .build();
    }

    @Test(expected = PasswordSpecification.InvalidSpecificationError.class)
    public void testBuild_require_countGreaterThanMaximum() {
        new PasswordSpecification.Builder()
                .ofLength(1, 5)
                .allow("abc")
                .require("a", 6)
                .build();
    }

    @Test
    public void testCheckConformance_default() throws Exception {
        PasswordSpecification spec = PasswordSpecification.DEFAULT;
        // conformant
        assertEquals(PASSWORD_CONFORMS, spec.checkConformance("LetmeinPeace5"));

        // one character too short
        assertEquals(PASSWORD_LENGTH_MISMATCH, spec.checkConformance("LetmeinPce5"));

        // one character too long
        assertEquals(PASSWORD_LENGTH_MISMATCH, spec.checkConformance("LetmeinNoPeace567"));

        // non-distinguishable lower case letter (l)
        assertEquals(PASSWORD_DISALLOWED_CHARACTER, spec.checkConformance("letmeinPeace5"));

        // non-distinguishable upper case letter (I)
        assertEquals(PASSWORD_DISALLOWED_CHARACTER, spec.checkConformance("LetmeInPeace5"));

        // non-distinguishable number (1)
        assertEquals(PASSWORD_DISALLOWED_CHARACTER, spec.checkConformance("LetmeinPeace14"));

        // non alpha-numeric character (!)
        assertEquals(PASSWORD_DISALLOWED_CHARACTER, spec.checkConformance("L3tmeinPeace!"));

        // empty password
        assertEquals(PASSWORD_LENGTH_MISMATCH | PASSWORD_REQUIRED_CHARACTER_MISSING,
                spec.checkConformance(""));

        // too short, missing a number
        assertEquals(PASSWORD_LENGTH_MISMATCH | PASSWORD_REQUIRED_CHARACTER_MISSING,
                spec.checkConformance("LetmeinPce"));

        // missing a number, contains non-alpha numeric character (!)
        assertEquals(PASSWORD_REQUIRED_CHARACTER_MISSING | PASSWORD_DISALLOWED_CHARACTER,
                spec.checkConformance("LetmeinPeace!"));
    }

    @Test
    public void testCheckConformance_allAlphaNumerals() throws Exception{
        PasswordSpecification spec = createTestSpec();

        assertEquals(PASSWORD_CONFORMS, spec.checkConformance("Letmein1"));
        assertEquals(PASSWORD_CONFORMS, spec.checkConformance("Letmein0"));
        assertEquals(PASSWORD_REQUIRED_CHARACTER_MISSING, spec.checkConformance("letmein"));
        assertEquals(PASSWORD_REQUIRED_CHARACTER_MISSING, spec.checkConformance("letmein1"));
        assertEquals(PASSWORD_REQUIRED_CHARACTER_MISSING, spec.checkConformance("trustno1"));

        assertEquals(PASSWORD_REQUIRED_CHARACTER_MISSING, spec.checkConformance("welcome"));
        assertEquals(PASSWORD_CONFORMS, spec.checkConformance("weLcome1"));
        assertEquals(PASSWORD_CONFORMS, spec.checkConformance("Welcome1"));
        assertEquals(PASSWORD_REQUIRED_CHARACTER_MISSING, spec.checkConformance("WelcomeO"));
        assertEquals(PASSWORD_CONFORMS, spec.checkConformance("Welcome0"));
    }

    @Test
    public void testcheckForConformance() throws Exception{
        
        assertTrue(PasswordSpecification.checkResultForError(
                0B001, PasswordSpecification.PASSWORD_LENGTH_MISMATCH));

        assertTrue(PasswordSpecification.checkResultForError(
                0B010, PasswordSpecification.PASSWORD_REQUIRED_CHARACTER_MISSING));

        assertTrue(PasswordSpecification.checkResultForError(
                0B100, PasswordSpecification.PASSWORD_DISALLOWED_CHARACTER));

        assertTrue(PasswordSpecification.checkResultForError(
                0B111, PasswordSpecification.PASSWORD_DISALLOWED_CHARACTER));

        assertTrue(PasswordSpecification.checkResultForError(
                0B111, PasswordSpecification.PASSWORD_LENGTH_MISMATCH));

        assertTrue(PasswordSpecification.checkResultForError(
                0B111, PasswordSpecification.PASSWORD_REQUIRED_CHARACTER_MISSING));

        assertFalse(PasswordSpecification.checkResultForError(
                0B101, PasswordSpecification.PASSWORD_REQUIRED_CHARACTER_MISSING));

        assertFalse(PasswordSpecification.checkResultForError(
                0B110, PasswordSpecification.PASSWORD_LENGTH_MISMATCH));

        assertFalse(PasswordSpecification.checkResultForError(
                0B010, PasswordSpecification.PASSWORD_DISALLOWED_CHARACTER));
    }

    @Test
    public void testGenerate() throws Exception {
        // as a basic fuzz test, generate and verify 1000 times
        for (int i = 0; i < 1000; i++) {
            String password = PasswordSpecification.DEFAULT.generate();
            int conformanceCode = PasswordSpecification.DEFAULT.checkConformance(password);
            assertThat(conformanceCode)
                    .overridingErrorMessage(
                            "Expected generated password %s to conform, violation code = %d",
                            password,
                            conformanceCode
                            )
                    .isEqualTo(PASSWORD_CONFORMS);
        }
    }

    @Test
    public void testEquals_self() throws Exception {
        assertTrue(PasswordSpecification.DEFAULT.equals(PasswordSpecification.DEFAULT));
    }

    @Test
    public void testEquals_null() throws Exception {
        assertFalse(PasswordSpecification.DEFAULT.equals(null));
    }

    @Test
    public void testEquals() throws Exception {
        assertTrue(createTestSpec().equals(createTestSpec()));
    }

    @Test
    public void testEquals_differentAllowSet() throws Exception {
        PasswordSpecification a = new PasswordSpecification.Builder()
                .ofLength(1, 5)
                .allow("abc")
                .build();
        PasswordSpecification b = new PasswordSpecification.Builder()
                .ofLength(1, 5)
                .allow("abd")
                .build();
        assertFalse(a.equals(b));
    }

    @Test
    public void testEquals_differentMinLength() throws Exception {
        PasswordSpecification a = new PasswordSpecification.Builder()
                .ofLength(5, 10)
                .allow("abc")
                .build();

        PasswordSpecification b = new PasswordSpecification.Builder()
                .ofLength(6, 10)
                .allow("abc")
                .build();
        assertFalse(a.equals(b));
    }

    @Test
    public void testEquals_differentMaxLength() throws Exception {
        PasswordSpecification a = new PasswordSpecification.Builder()
                .ofLength(5, 10)
                .allow("abc")
                .build();

        PasswordSpecification b = new PasswordSpecification.Builder()
                .ofLength(5, 11)
                .allow("abc")
                .build();
        assertFalse(a.equals(b));
    }

    @Test
    public void testEquals_differentRequireSetChars() throws Exception {
        PasswordSpecification a = new PasswordSpecification.Builder()
                .ofLength(1, 10)
                .allow("abc")
                .require("a", 1)
                .build();

        PasswordSpecification b = new PasswordSpecification.Builder()
                .ofLength(1, 10)
                .allow("abc")
                .require("b", 1)
                .build();
        assertFalse(a.equals(b));
    }

    @Test
    public void testEquals_differentRequireSetCharCount() throws Exception {
        PasswordSpecification a = new PasswordSpecification.Builder()
                .ofLength(1, 10)
                .allow("abc")
                .require("a", 1)
                .build();

        PasswordSpecification b = new PasswordSpecification.Builder()
                .ofLength(1, 10)
                .allow("abc")
                .require("a", 2)
                .build();

        assertFalse(a.equals(b));
    }

    @Test
    public void testEquals_differentRequireSets() throws Exception {
        PasswordSpecification a = new PasswordSpecification.Builder()
                .ofLength(1, 10)
                .allow("abc")
                .require("a", 1)
                .build();

        PasswordSpecification b = new PasswordSpecification.Builder()
                .ofLength(1, 10)
                .allow("abc")
                .require("a", 1)
                .require("b", 1)
                .build();

        assertFalse(a.equals(b));
    }

    @Test
    public void testHashCode() {
        assertThat(createTestSpec().hashCode()).isEqualTo(createTestSpec().hashCode());
        assertThat(PasswordSpecification.DEFAULT.hashCode())
                .isNotEqualTo(createTestSpec().hashCode());
    }

    @Test
    public void testSerialization() throws Exception{
        Parcel p = Parcel.obtain();
        try {
            PasswordSpecification.DEFAULT.writeToParcel(p, 0);
            p.setDataPosition(0);
            PasswordSpecification spec =
                    PasswordSpecification.CREATOR.createFromParcel(p);
            assertThat(spec).isEqualTo(PasswordSpecification.DEFAULT);
        } finally {
            p.recycle();
        }
    }

    private PasswordSpecification createTestSpec() {
        return  new PasswordSpecification.Builder()
                .ofLength(6, 10)
                .allow(ALPHANUMERIC)
                .require(LOWER_ALPHA, 1)
                .require(UPPER_ALPHA, 1)
                .require(NUMERALS, 1)
                .build();
    }
}