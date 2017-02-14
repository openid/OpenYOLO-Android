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

package org.openyolo.testapp;

import android.support.annotation.NonNull;
import java.util.Locale;
import java.util.Random;
import okio.ByteString;

/**
 * Generates random data useful for populating credentials.
 */
public class RandomData {

    /**
     * The top-100 given (aka "first") names, from US Census Bureau data.
     */
    public static final String[] GIVEN_NAMES = {
            "Amanda",
            "Amy",
            "Andrew",
            "Angela",
            "Ann",
            "Anna",
            "Anthony",
            "Arthur",
            "Barbara",
            "Betty",
            "Brenda",
            "Brian",
            "Carl",
            "Carol",
            "Carolyn",
            "Catherine",
            "Charles",
            "Christine",
            "Christopher",
            "Cynthia",
            "Daniel",
            "David",
            "Deborah",
            "Debra",
            "Dennis",
            "Diane",
            "Donald",
            "Donna",
            "Dorothy",
            "Douglas",
            "Edward",
            "Elizabeth",
            "Eric",
            "Frances",
            "Frank",
            "Gary",
            "George",
            "Gregory",
            "Harold",
            "Helen",
            "Henry",
            "James",
            "Janet",
            "Jason",
            "Jeffrey",
            "Jennifer",
            "Jerry",
            "Jessica",
            "John",
            "Jose",
            "Joseph",
            "Joshua",
            "Joyce",
            "Karen",
            "Kathleen",
            "Kenneth",
            "Kevin",
            "Kimberly",
            "Larry",
            "Laura",
            "Linda",
            "Lisa",
            "Margaret",
            "Maria",
            "Marie",
            "Mark",
            "Martha",
            "Mary",
            "Matthew",
            "Melissa",
            "Michael",
            "Michelle",
            "Nancy",
            "Pamela",
            "Patricia",
            "Patrick",
            "Paul",
            "Peter",
            "Raymond",
            "Rebecca",
            "Richard",
            "Robert",
            "Roger",
            "Ronald",
            "Ruth",
            "Ryan",
            "Sandra",
            "Sarah",
            "Scott",
            "Sharon",
            "Shirley",
            "Stephanie",
            "Stephen",
            "Steven",
            "Susan",
            "Thomas",
            "Timothy",
            "Virginia",
            "Walter",
            "William",
    };

    /**
     * The top-100 family names, from US Census Bureau data.
     */
    public static final String[] FAMILY_NAMES = {
            "Adams",
            "Alexander",
            "Allen",
            "Anderson",
            "Bailey",
            "Baker",
            "Barnes",
            "Bell",
            "Bennett",
            "Brooks",
            "Brown",
            "Bryant",
            "Butler",
            "Campbell",
            "Carter",
            "Clark",
            "Coleman",
            "Collins",
            "Cook",
            "Cooper",
            "Cox",
            "Davis",
            "Diaz",
            "Edwards",
            "Evans",
            "Flores",
            "Foster",
            "Garcia",
            "Gonzales",
            "Gonzalez",
            "Gray",
            "Green",
            "Griffin",
            "Hall",
            "Harris",
            "Hayes",
            "Henderson",
            "Hernandez",
            "Hill",
            "Howard",
            "Hughes",
            "Jackson",
            "James",
            "Jenkins",
            "Johnson",
            "Jones",
            "Kelly",
            "King",
            "Lee",
            "Lewis",
            "Long",
            "Lopez",
            "Martin",
            "Martinez",
            "Miller",
            "Mitchell",
            "Moore",
            "Morgan",
            "Morris",
            "Murphy",
            "Nelson",
            "Parker",
            "Patterson",
            "Perez",
            "Perry",
            "Peterson",
            "Phillips",
            "Powell",
            "Price",
            "Ramirez",
            "Reed",
            "Richardson",
            "Rivera",
            "Roberts",
            "Robinson",
            "Rodriguez",
            "Rogers",
            "Ross",
            "Russell",
            "Sanchez",
            "Sanders",
            "Scott",
            "Simmons",
            "Smith",
            "Stewart",
            "Taylor",
            "Thomas",
            "Thompson",
            "Torres",
            "Turner",
            "Walker",
            "Ward",
            "Washington",
            "Watson",
            "White",
            "Williams",
            "Wilson",
            "Wood",
            "Wright",
            "Young"
    };

    private static final int ROBOHASH_LENGTH = 32;

    private final Random mRandom;

    /**
     * Instantiates a random user data source.
     */
    public RandomData() {
        mRandom = new Random();
    }

    /**
     * Generates a given (aka "first") name.
     */
    @NonNull
    public String generateGivenName() {
        return GIVEN_NAMES[mRandom.nextInt(GIVEN_NAMES.length)];
    }

    /**
     * Generates a family name.
     */
    @NonNull
    public String generateFamilyName() {
        return FAMILY_NAMES[mRandom.nextInt(FAMILY_NAMES.length)];
    }

    /**
     * Generates a full display name composed of a given name and family name.
     */
    public String generateDisplayName() {
        return getDisplayName(generateGivenName(), generateFamilyName());
    }

    /**
     * Generates a display name from the provided given and family names.
     */
    public String getDisplayName(String givenName, String familyName) {
        return givenName + " " + familyName;
    }

    /**
     * Generates an email address.
     */
    public String generateEmailAddress() {
        return getEmailAddress(generateGivenName(), generateFamilyName());
    }

    /**
     * Generates an email address using the provided given and family names for the user.
     */
    public String getEmailAddress(String givenName, String surname) {
        return givenName.toLowerCase(Locale.US).trim()
                + "." + surname.toLowerCase(Locale.US).trim()
                + "@openyolo.org";
    }

    /**
     * Generates a hexadecimal string from a random byte array of the provided length.
     */
    public String generateHexString(int numBytes) {
        byte[] bytes = new byte[numBytes];
        mRandom.nextBytes(bytes);
        return ByteString.of(bytes).hex();
    }

    /**
     * Generates a profile picture URI.
     */
    public String generateProfilePictureUri() {
        return "https://robohash.org/" + generateHexString(ROBOHASH_LENGTH) + "?set=set3";
    }
}
