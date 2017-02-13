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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.openyolo.api.internal.Constants.HASH_PRIME;
import static org.valid4j.Assertive.require;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Specification of an acceptable set of passwords, expressed in terms of allowed and required
 * sets of characters and a size range. Only ASCII printable characters are supported.
 *
 * <p>
 * Example Specifications:
 *
 * <ul>
 *     <li>
 *     A 12-16 character password that must contain at least one digit and one upper-case letter:
 *     {@code
 *     new PasswordSpecification.Builder()
 *             .ofLength(12, 16)
 *             .allow(ALL_PRINTABLE)
 *             .require(NUMERALS, 1)
 *             .require(UPPER_ALPHA, 1)
 *             .build();
 *     }
 *     </li>
 *     <li>
 *     A 6-digit pin number:
 *     {@code
 *     new PasswordSpecification.Builder()
 *             .allow(NUMERALS)
 *             .ofLength(6,6)
 *             .build();
 *     }
 *     </li>
 *     <li>
 *     An 8-12 character password permitting only alphanumeric characters
 *     {@code
 *     new PasswordSpecification.Builder()
 *             .ofLength(8,12)
 *             .allow(LOWER_ALPHA)
 *             .allow(UPPER_ALPHA)
 *             .allow(NUMERALS)
 *             .build();
 *     }
 *     </li>
 * </ul>
 * </p>
 */
public final class PasswordSpecification implements Parcelable {

    /**
     * Parcelable {@link android.os.Parcelable.Creator Creator} for PasswordSpecification instances.
     */
    public static final Creator<PasswordSpecification> CREATOR = new PasswordSpecificationCreator();

    /**
     * The set of lower case ASCII characters (a-z).
     */
    public static final String LOWER_ALPHA = "abcdefghijklmnopqrstuvwxyz";

    /**
     * The set of lower case ASCII characters, omitting those which are difficult to distinguish
     * from other alphanumeric characters. The omitted characters are:
     * <ul>
     * <li>l (lima), often confused with 1 (one) and I (india, upper case)</li>
     * <li>u (uniform), often confused with v (victor)</li>
     * <li>v (victor), often confused with u (uniform)</li>
     * <li>w (whiskey), often confused with vv (victor victor)</li>
     * </ul>
     */
    public static final String LOWER_ALPHA_DISTINGUISHABLE = "abcdefghijkmnopqrstxyz";

    /**
     * The set of upper case ASCII characters (A-Z).
     */
    public static final String UPPER_ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * The set of upper case ASCII characters, omitting those which can be difficult to distinguish
     * from other alphanumeric characters. The omitted characters are:
     * <ul>
     * <li>I (india), often confused with 1 (one) and l (lima, lower case)</li>
     * <li>O (oscar), often confused with 0 (zero)</li>
     * <li>U (uniform), often confused with v (victor)</li>
     * <li>V (victor), often confused with u (uniform)</li>
     * <li>W (whiskey), often confused with vv (victor victor)</li>
     * <li>Z (zulu), often confused with 2 (two)</li>
     * </ul>
     */
    public static final String UPPER_ALPHA_DISTINGUISHABLE = "ABCDEFGHJKLMNPQRSTXY";

    /**
     * The set of ASCII numerals (0-9).
     */
    public static final String NUMERALS = "1234567890";

    /**
     * The set of ASCII numerals, omitting those which can be difficult to distinguish from other
     * alphanumeric characters. The omitted characters are:
     * <ul>
     * <li>0 (zero), often confused with O (oscar, upper case)</li>
     * <li>1 (one), often confused with I (india, upper case) and l (lima, lower case)</li>
     * <li>2 (two), often confused with Z (zulu, upper case)</li>
     * </ul>
     */
    public static final String NUMERALS_DISTINGUISHABLE = "3456789";

    /**
     * The set of all alpha-numeric ASCII characters (a-z, A-Z, 0-9).
     */
    public static final String ALPHANUMERIC = LOWER_ALPHA + UPPER_ALPHA + NUMERALS;

    /**
     * The set of all printable ASCII symbols.
     */
    public static final String SYMBOLS = " !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";

    /**
     * The set of all printable ASCII characters.
     */
    public static final String ALL_PRINTABLE =
            LOWER_ALPHA + UPPER_ALPHA + NUMERALS + SYMBOLS;

    /**
     * The set of all alphanumeric characters (and space) which are easily
     * distinguished. Specifically, this excludes:
     * <ul>
     * <li>I (india, upper case), often confused with 1 (one) and l (lima, lower case)</li>
     * <li>l (lima, lower case), often confused with 1 (one) and I (india, upper case)</li>
     * <li>O (oscar, upper case), often confused with 0 (zero)</li>
     * <li>U (uniform, lower and upper case), often confused with v (victor)</li>
     * <li>V (victor, lower and upper case), often confused with u (uniform)</li>
     * <li>W (whiskey, lower and upper case), often confused with vv (victor victor)</li>
     * <li>Z (zulu), often confused with 2 (two)</li>
     * <li>0 (zero), often confused with O (oscar, upper case)</li>
     * <li>1 (one), often confused with I (india, upper case) and l (lima, lower case)</li>
     * <li>2 (two), often confused with Z (zulu, upper case)</li>
     * </ul>
     */
    public static final String ALPHANUMERIC_DISTINGUISHABLE =
            LOWER_ALPHA_DISTINGUISHABLE + UPPER_ALPHA_DISTINGUISHABLE + NUMERALS_DISTINGUISHABLE;

    /**
     * The minimum password length used by {@link #DEFAULT PasswordSpecification.DEFAULT}.
     */
    public static final int DEFAULT_MIN_PASSWORD_LENGTH = 12;

    /**
     * The maximum password length used by {@link #DEFAULT PasswordSpecification.DEFAULT}.
     */
    public static final int DEFAULT_MAX_PASSWORD_LENGTH = 16;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PASSWORD_CONFORMS,
            PASSWORD_LENGTH_MISMATCH,
            PASSWORD_REQUIRED_CHARACTER_MISSING,
            PASSWORD_DISALLOWED_CHARACTER})
    public @interface ConformanceMask {}

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PASSWORD_LENGTH_MISMATCH,
            PASSWORD_REQUIRED_CHARACTER_MISSING,
            PASSWORD_DISALLOWED_CHARACTER})
    public @interface ConformanceError {}

    /**
     * BitMask to be used to check the result of {@link #checkConformance(String) checkConformance}
     * that indicates that the supplied password conforms to the specification.
     */
    public static final int PASSWORD_CONFORMS = 0B0000;

    /**
     * BitMask to be used to check the result of {@link #checkConformance(String) checkConformance}
     * that indicates that the supplied password is not within the required size bounds.
     */
    public static final int PASSWORD_LENGTH_MISMATCH = 0B0001;

    /**
     * BitMask to be used to check the result of {@link #checkConformance(String) checkConformance}
     * that indicates that the supplied password is missing one or more required characters.
     */
    public static final int PASSWORD_REQUIRED_CHARACTER_MISSING = 0B0010;

    /**
     * BitMask to be used to check the result of {@link #checkConformance(String) checkConformance}
     * that indicates that the supplied password contains a disallowed character.
     */
    public static final int PASSWORD_DISALLOWED_CHARACTER = 0B0100;

    /**
     * The default password specification used by the credentials API when no alternative is
     * provided. This specification should be compatible with the majority of password based
     * authentication systems and has sufficient entropy to be secure against offline attacks.
     * The specification permits passwords between 12 and 16 characters in length, composed of at
     * least one lower case letter, one upper case letter, and one numeral. All other characters
     * must be alphanumeric.
     *
     * <p>Characters which are difficult to distinguish are disallowed in this specification,
     * as it is tailored towards password generation rather than validation - see
     * {@link #DEFAULT_FOR_VALIDATION} for a password specification suited to validating user
     * entered passwords.
     */
    public static final PasswordSpecification DEFAULT = new PasswordSpecification.Builder()
            .ofLength(DEFAULT_MIN_PASSWORD_LENGTH, DEFAULT_MAX_PASSWORD_LENGTH)
            .allow(ALPHANUMERIC_DISTINGUISHABLE)
            .require(LOWER_ALPHA_DISTINGUISHABLE, 1)
            .require(UPPER_ALPHA_DISTINGUISHABLE, 1)
            .require(NUMERALS_DISTINGUISHABLE, 1)
            .build();

    /**
     * A default password specification, intended for use in validating user entered passwords.
     * This differs from {@link #DEFAULT} in that characters which are difficult to distinguish
     * are permitted.
     */
    public static final PasswordSpecification DEFAULT_FOR_VALIDATION =
            new PasswordSpecification.Builder()
                    .ofLength(DEFAULT_MIN_PASSWORD_LENGTH, DEFAULT_MAX_PASSWORD_LENGTH)
                    .allow(ALPHANUMERIC)
                    .require(LOWER_ALPHA, 1)
                    .require(UPPER_ALPHA, 1)
                    .require(NUMERALS, 1)
                    .build();

    private static final int ASCII_PRINTABLE_RANGE_LOWER = 32;
    private static final int ASCII_PRINTABLE_RANGE_UPPER = 126;

    /**
     * Utility method to check if the password is conformant to the specification defined.
     * @param result the result of the @link #checkConformance(String) checkConformance} method.
     * @return true if password is conforming to specs.
     */
    public static boolean checkResultForError(final int result, @ConformanceError final int mask) {
        return (result & mask) != 0;
    }

    /**
     * The set of allowed characters, represented as a string.
     */
    public final String allowedChars;

    /**
     * The required character sets for this specification.
     */
    public final SortedSet<RequiredCharSet> requiredCharSets;

    /**
     * The maximum acceptable size of a password.
     */
    public final int minimumSize;

    /**
     * The minimum acceptable size of a password.
     */
    public final int maximumSize;

    // the below fields are created on-demand, and are not serialized. They should not be
    // directly referenced, instead, use the associated getters to ensure they are correctly
    // instantiated.
    private Integer mHash;
    private Random mRandom;
    private int[] mRequiredSpecMapping;
    private int[] mRequiredCharCounts;

    private PasswordSpecification(
            String allowedChars,
            SortedSet<RequiredCharSet> requiredCharSets,
            int minimumSize,
            int maximumSize) {
        this.allowedChars = allowedChars;
        this.requiredCharSets = Collections.unmodifiableSortedSet(requiredCharSets);
        this.minimumSize = minimumSize;
        this.maximumSize = maximumSize;
    }

    /**
     * Determines whether the provided password conforms to the specification.
     *
     * @return a bit field which represents the status of the checks on this password.
     *     values are {@link #PASSWORD_CONFORMS} - 0 if the password conforms to the specification.
     *     Otherwise, an error code will be returned in the form of a bit field.
     *     (one comprised of the of the bits representing issues with this password
     *     {@link #PASSWORD_LENGTH_MISMATCH},
     *     {@link #PASSWORD_DISALLOWED_CHARACTER} or
     *     {@link #PASSWORD_REQUIRED_CHARACTER_MISSING}).
     *
     *     @see <a href="https://en.wikipedia.org/wiki/Bit_field">Bit Field</a> -
     *     A bit field is a data structure used in computer programming, to store information in
     *     the bits of a byte.
     */
    @ConformanceMask
    public int checkConformance(String password) {
        int result = PASSWORD_CONFORMS;
        if (TextUtils.isEmpty(password)) {
            result |= PASSWORD_LENGTH_MISMATCH;
        }

        if (isOutsideRange(password.length(), minimumSize, maximumSize)) {
            result |= PASSWORD_LENGTH_MISMATCH;
        }

        // in order to check the required and allowed characters, we attempt to match
        // each character against a required character set. If the requisite number of
        // characters have been matched against that required set, then they are checked
        // to see if they are allowed beyond this count (which allows for password
        // specifications with exact required counts, by having a disjoint set of
        // required characters from allowed characters).
        int[] requiredSpecMapping = getRequiredSpecMapping();
        int[] requiredSpecRemainingCounts = extractRequiredCharCounts();

        for (char c : password.toCharArray()) {
            if (!containsCharacter(allowedChars, c)) {
                result |= PASSWORD_DISALLOWED_CHARACTER;
            }

            int requiredSpecIndex = requiredSpecMapping[getCharMappingIndex(c)];
            if (requiredSpecIndex >= 0) {
                requiredSpecRemainingCounts[requiredSpecIndex]--;
            }
        }

        for (int remaining : requiredSpecRemainingCounts) {
            if (remaining > 0) {
                result |= PASSWORD_REQUIRED_CHARACTER_MISSING;
                break;
            }
        }

        return result;
    }

    /**
     * Generates a random password in conformance with the specification.
     */
    @NonNull
    public String generate() {
        int size = minimumSize + getRandom().nextInt(maximumSize - minimumSize + 1);
        ArrayList<Character> chars = new ArrayList<>(size);

        // select all the required characters
        for (RequiredCharSet set : requiredCharSets) {
            selectCharacters(set.chars, set.count, chars);
        }

        // select all remaining characters from the allowed characters set
        int remaining = size - chars.size();
        selectCharacters(allowedChars, remaining, chars);

        // randomly shuffle the characters, so the required characters do not occur in a
        // predictable position
        Collections.shuffle(chars);
        return charCollectionToString(chars);
    }

    /**
     * Translates the specification into a protocol buffer, for transmission or storage.
     */
    @NonNull
    public org.openyolo.proto.PasswordSpecification toProtocolBuffer() {
        return new org.openyolo.proto.PasswordSpecification.Builder()
                .minSize(minimumSize)
                .maxSize(maximumSize)
                .allowed(allowedChars)
                .requiredSets(convertRequiredCharSetsToProto())
                .build();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        byte[] encoded = toProtocolBuffer().encode();
        out.writeInt(encoded.length);
        out.writeByteArray(encoded);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof PasswordSpecification)) {
            return false;
        }

        PasswordSpecification other = (PasswordSpecification)obj;

        if (this.minimumSize != other.minimumSize
                || this.maximumSize != other.maximumSize
                || !this.allowedChars.equals(other.allowedChars)) {
            return false;
        }

        if (this.requiredCharSets.size() != other.requiredCharSets.size()) {
            return false;
        }

        Iterator<RequiredCharSet> thisRequiredSetIter = this.requiredCharSets.iterator();
        Iterator<RequiredCharSet> otherRequiredSetIter = other.requiredCharSets.iterator();

        while (thisRequiredSetIter.hasNext()) {
            if (!thisRequiredSetIter.next().equals(otherRequiredSetIter.next())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        if (mHash != null) {
            return mHash;
        }

        int hash = minimumSize;
        hash = hash * HASH_PRIME + maximumSize;
        hash = hash * HASH_PRIME + allowedChars.hashCode();

        for (RequiredCharSet set : requiredCharSets) {
            hash = hash * HASH_PRIME + set.chars.hashCode();
            hash = hash * HASH_PRIME + set.count;
        }

        mHash = hash;
        return mHash;
    }

    private List<org.openyolo.proto.RequiredCharSet> convertRequiredCharSetsToProto() {
        List<org.openyolo.proto.RequiredCharSet> convertedRequiredCharSets = new ArrayList<>();

        for (RequiredCharSet set : requiredCharSets) {
            convertedRequiredCharSets.add(
                    new org.openyolo.proto.RequiredCharSet.Builder()
                            .chars(set.chars)
                            .count(set.count)
                            .build());
        }

        return convertedRequiredCharSets;
    }

    /**
     * Extracts a mapping from an adjusted character index (where the lowest printable ascii
     * character is given index 0) to the required character spec that it corresponds to,
     * or -1 if the character is not part of any required set.
     */
    private int[] getRequiredSpecMapping() {
        if (mRequiredSpecMapping != null) {
            return mRequiredSpecMapping;
        }

        int[] requiredSpecMapping =
                new int[ASCII_PRINTABLE_RANGE_UPPER - ASCII_PRINTABLE_RANGE_LOWER + 1];
        Arrays.fill(requiredSpecMapping, -1);

        int specIndex = 0;
        for (RequiredCharSet set : requiredCharSets) {
            for (char c : set.chars.toCharArray()) {
                requiredSpecMapping[getCharMappingIndex(c)] = specIndex;
            }
            specIndex++;
        }

        mRequiredSpecMapping = requiredSpecMapping;
        return mRequiredSpecMapping;
    }

    /**
     * Determines the provided character's adjusted index in the required character spec mapping,
     * where the lowest printable ascii character is given index 0.
     */
    private static int getCharMappingIndex(char ch) {
        return ch - ASCII_PRINTABLE_RANGE_LOWER;
    }

    private void selectCharacters(String chars, int count, ArrayList<Character> result) {
        Random random = getRandom();
        for (int i = 0; i < count; i++) {
            result.add(chars.charAt(random.nextInt(chars.length())));
        }
    }

    private boolean containsCharacter(String chars, char ch) {
        int pos = Arrays.binarySearch(chars.toCharArray(), ch);
        return (pos >= 0 && pos < chars.length() && chars.charAt(pos) == ch);
    }

    private int[] extractRequiredCharCounts() {
        if (mRequiredCharCounts == null) {
            mRequiredCharCounts = new int[requiredCharSets.size()];
            int index = 0;
            for (RequiredCharSet set : requiredCharSets) {
                mRequiredCharCounts[index] = set.count;
                index++;
            }
        }

        int[] requiredCharCounts = new int[mRequiredCharCounts.length];
        System.arraycopy(
                mRequiredCharCounts,
                0, // source index
                requiredCharCounts,
                0, // destination index
                mRequiredCharCounts.length);
        return requiredCharCounts;
    }

    private Random getRandom() {
        if (mRandom != null) {
            return mRandom;
        }

        mRandom = new SecureRandom();
        return mRandom;
    }

    private static String charCollectionToString(Collection<Character> chars) {
        char[] charArray = new char[chars.size()];
        int pos = 0;
        for (char c : chars) {
            charArray[pos++] = c;
        }

        return new String(charArray);
    }

    private static boolean isOutsideRange(int val, int lowerBound, int upperBound) {
        return val < lowerBound || val > upperBound;
    }

    /**
     * Produces {@link PasswordSpecification} instances. At least one call to
     * {@link #allow(String)} or {@link #require(String,int)} must be made to specify the set of
     * characters that the password can contain. The password size range will default to
     * {@link #DEFAULT_MIN_PASSWORD_LENGTH} to {@link #DEFAULT_MAX_PASSWORD_LENGTH},
     * which may be overridden.
     */
    public static class Builder {

        private final TreeSet<Character> mAllowedCharSet = new TreeSet<>();
        private final TreeSet<RequiredCharSet> mRequiredCharSets = new TreeSet<>();
        private int mMinimumSize = -1;
        private int mMaximumSize = -1;

        /**
         * Recreates a password specification from its protocol buffer form.
         */
        public Builder(org.openyolo.proto.PasswordSpecification proto) {
            this.ofLength(proto.minSize, proto.maxSize);
            this.allow(proto.allowed);
            for (org.openyolo.proto.RequiredCharSet set : proto.requiredSets) {
                this.require(set.chars, set.count);
            }
        }

        /**
         * Starts the process of defining a password specification.
         */
        public Builder() {}

        /**
         * Declares that the provided set of characters may occur in a password,
         * zero or more times.
         * @param allowedChars A set of characters which may occur in a password. Duplicate
         *     characters in the string, or provided over multiple calls to allow,
         *     will be ignored.
         * @throws InvalidSpecificationError if the string is null or empty, or contains
         *     characters which are not in the ascii printable range.
         */
        public Builder allow(@NonNull String allowedChars) {
            mAllowedCharSet.addAll(checkAndSortChars(allowedChars, "allowedChars"));
            return this;
        }

        /**
         * Declares that at least as many characters as specified must exist in the password,
         * drawn from the provided set. The character set must not overlap with any
         * other required character set, and must be a subset of the allowed character set.
         *
         * @param requiredChars A set of characters from which the required count will be selected.
         *     Duplicate characters will be ignored.
         * @param count The number of characters which will be selected from the required character
         *     set. Must be at least 1.
         * @throws InvalidSpecificationError if the string is null or empty, contains
         *     characters which are not in the ascii printable range, or exist within another
         *     required set of characters.
         */
        public Builder require(@NonNull String requiredChars, int count) {
            if (count < 1) {
                throw new InvalidSpecificationError("count must be at least 1");
            }

            TreeSet<Character> requiredCharSet = checkAndSortChars(requiredChars, "requiredChars");
            mAllowedCharSet.addAll(requiredCharSet);
            mRequiredCharSets.add(
                    new RequiredCharSet(
                            charCollectionToString(requiredCharSet),
                            count));
            return this;
        }

        /**
         * Declares that the password must be within a certain size range.
         * @param minimumSize
         *     The minimum size of the password. Must be at least 1.
         * @param  maximumSize
         *     The maximum size of the password. Must be at least as large as {@code minimumSize}.
         */
        public Builder ofLength(int minimumSize, int maximumSize) {
            if (minimumSize < 1) {
                throw new InvalidSpecificationError("minimumSize must be at least 1");
            }

            if (minimumSize > maximumSize) {
                throw new InvalidSpecificationError(
                        "maximumSize must be greater than or equal to minimumSize");
            }

            mMinimumSize = minimumSize;
            mMaximumSize = maximumSize;

            return this;
        }

        /**
         * Produces a {@link PasswordSpecification} instance, if the configuration specified on the
         * builder is valid.
         *
         * @throws InvalidSpecificationError if the specification is invalid.
         */
        public PasswordSpecification build() {
            if (mAllowedCharSet.isEmpty()) {
                throw new InvalidSpecificationError("no allowed characters specified");
            }

            if (mMinimumSize < 0) {
                // the length of the password was never set
                throw new InvalidSpecificationError(
                        "minimum and maximum size of password not specified");
            }

            checkRequiredCharsTotalCount();
            checkRequiredCharsDisjoint();

            return new PasswordSpecification(
                    charCollectionToString(mAllowedCharSet),
                    mRequiredCharSets,
                    mMinimumSize,
                    mMaximumSize);
        }

        private TreeSet<Character> checkAndSortChars(String chars, String paramName) {
            if (TextUtils.isEmpty(chars)) {
                throw new InvalidSpecificationError(paramName + " cannot be null or empty");
            }

            TreeSet<Character> filteredChars = new TreeSet<>();

            for (char c : chars.toCharArray()) {
                if (isOutsideRange(c, ASCII_PRINTABLE_RANGE_LOWER, ASCII_PRINTABLE_RANGE_UPPER)) {
                    throw new InvalidSpecificationError(
                            paramName + " must only contain ASCII printable characters");
                }
                filteredChars.add(c);
            }

            return filteredChars;
        }

        private void checkRequiredCharsTotalCount() {
            int count = 0;
            for (RequiredCharSet set : mRequiredCharSets) {
                count += set.count;
            }

            if (count > mMaximumSize) {
                throw new InvalidSpecificationError(
                        "required character count cannot be greater than the max password size");
            }
        }

        private void checkRequiredCharsDisjoint() {
            boolean[] charsUsed =
                    new boolean[ASCII_PRINTABLE_RANGE_UPPER - ASCII_PRINTABLE_RANGE_LOWER + 1];

            for (RequiredCharSet set : mRequiredCharSets) {
                for (char c : set.chars.toCharArray()) {
                    if (charsUsed[c - ASCII_PRINTABLE_RANGE_LOWER]) {
                        throw new InvalidSpecificationError("character " + c
                                + " occurs in more than one required character set");
                    }
                    charsUsed[c - ASCII_PRINTABLE_RANGE_LOWER] = true;
                }
            }
        }
    }

    /**
     * The error type that is thrown in an attempt is made to construct an invalid specification.
     */
    public static class InvalidSpecificationError extends Error {
        /**
         * Creates an InvalidSpecificationError with the provided reason.
         */
        public InvalidSpecificationError(String reason) {
            super(reason);
        }
    }

    /**
     * Represents a required character set, where a password must contain at least as many
     * characters as specified, drawn from the specified set.
     */
    private static final class RequiredCharSet implements Comparable<RequiredCharSet> {

        /**
         * The set of characters from which the required characters must be drawn.
         */
        public final String chars;

        /**
         * The minimum number of characters that must occur in the password, drawn from
         * {@link #chars}.
         */
        public final int count;

        private RequiredCharSet(String chars, int count) {
            this.chars = chars;
            this.count = count;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null || !(obj instanceof RequiredCharSet)) {
                return false;
            }

            RequiredCharSet other = (RequiredCharSet) obj;
            return this.chars.equals(other.chars) && this.count == other.count;
        }

        @Override
        public int hashCode() {
            return count * HASH_PRIME + chars.hashCode();
        }

        @Override
        public int compareTo(@NonNull RequiredCharSet other) {
            require(other, notNullValue());

            // sort by count then chars
            if (this.count < other.count) {
                return -1;
            }

            if (this.count > other.count) {
                return 1;
            }

            return chars.compareTo(other.chars);
        }
    }

    private static final class PasswordSpecificationCreator
            implements Creator<PasswordSpecification> {

        @Override
        public PasswordSpecification createFromParcel(Parcel parcel) {
            int encodedSize = parcel.readInt();
            byte[] encoded = new byte[encodedSize];
            parcel.readByteArray(encoded);

            org.openyolo.proto.PasswordSpecification proto;
            try {
                proto = org.openyolo.proto.PasswordSpecification.ADAPTER.decode(encoded);
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to read proto from parcel", ex);
            }

            return new PasswordSpecification.Builder(proto).build();
        }

        @Override
        public PasswordSpecification[] newArray(int size) {
            return new PasswordSpecification[size];
        }
    }
}
