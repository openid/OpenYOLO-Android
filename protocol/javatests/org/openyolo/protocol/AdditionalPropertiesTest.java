/*
 * Copyright 2017 The OpenYOLO Authors. All Rights Reserved.
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

package org.openyolo.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_ANOTHER_KEY;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_STRING_VALUE;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_TEST_KEY;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_TWO_BYTE_VALUE;
import static org.openyolo.protocol.TestConstants.ADDITIONAL_PROP_ZERO_BYTE_VALUE;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openyolo.protocol.TestConstants.ValidAdditionalProperties;
import org.openyolo.protocol.TestConstants.ValidFacebookCredential;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;
import org.robolectric.annotation.Config;

/**
 * Tests additional property storage and retrieval, for all types that implement the
 * {@link AdditionalPropertiesContainer} interface.
 */
@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AdditionalPropertiesTest {

    private final String mContainerClassName;

    /**
     * Classloader shenanigans within Robolectric mean that we can't just directly instantiate
     * and provide the instances of AdditionalPropertiesBuilder via the @Parameters annotated
     * method. We must ensure that the instances are only created once the test methods are
     * actually executing, as it is only at this point that the robolectric shadows have been
     * loaded. So, as an awkward workaround, this map holds lambdas to create the builders
     * on-demand, and the tests are parameterized by the keys of the map.
     */
    private static final Map<String, Callable<AdditionalPropertiesBuilder>> buildersByContainerType =
            ImmutableMap.<String, Callable<AdditionalPropertiesBuilder>>builder()
                    .put("Credential",
                            () -> new Credential.Builder(
                                    ValidFacebookCredential.ID,
                                    ValidFacebookCredential.AUTHENTICATION_METHOD,
                                    ValidFacebookCredential.AUTHENTICATION_DOMAIN))
                    .put("CredentialDeleteRequest",
                            () -> new CredentialDeleteRequest.Builder(
                                    ValidFacebookCredential.make()))
                    .put("CredentialDeleteResult",
                            () -> new CredentialDeleteResult.Builder(
                                    CredentialDeleteResult.CODE_DELETED))
                    .put("CredentialRetrieveRequest",
                            () -> new CredentialRetrieveRequest.Builder(
                                    AuthenticationMethods.EMAIL))
                    .put("CredentialRetrieveResult",
                            () -> new CredentialRetrieveResult.Builder(
                                    CredentialRetrieveResult.CODE_NO_CREDENTIALS_AVAILABLE))
                    .put("CredentialSaveRequest",
                            () -> new CredentialSaveRequest.Builder(
                                    ValidFacebookCredential.make()))
                    .put("CredentialSaveResult",
                            () -> new CredentialSaveResult.Builder(
                                    CredentialSaveResult.CODE_SAVED))
                    .put("Hint",
                            () -> new Hint.Builder(
                                    ValidFacebookCredential.ID,
                                    ValidFacebookCredential.AUTHENTICATION_METHOD))
                    .put("HintRetrieveRequest",
                            () -> new HintRetrieveRequest.Builder(
                                    AuthenticationMethods.EMAIL))
                    .put("HintRetrieveResult",
                            () -> new HintRetrieveResult.Builder(
                                    HintRetrieveResult.CODE_NO_HINTS_AVAILABLE))
            .build();

    @Parameters(name = "AdditionalPropertiesTest for {0}")
    public static Collection params() {
        ArrayList<Object[]> params = new ArrayList<>();

        for (String containerTypeName : buildersByContainerType.keySet()) {
            params.add(new Object[] { containerTypeName });
        }

        return params;
    }

    private AdditionalPropertiesBuilder<?, ?> mBuilder;

    public AdditionalPropertiesTest(String containerClassName) {
        mContainerClassName = containerClassName;
    }

    @Before
    public void setUp() throws Exception {
        mBuilder = buildersByContainerType.get(mContainerClassName).call();
    }

    @Test
    public void testBuilder_setAdditionalProperties() {
        AdditionalPropertiesContainer container = mBuilder
                .setAdditionalProperties(ValidAdditionalProperties.make())
                .build();
        ValidAdditionalProperties.assertEquals(container.getAdditionalProperties());
    }

    @Test
    public void testBuilder_setAdditionalProperties_withNull() {
        AdditionalPropertiesContainer container = mBuilder
                .setAdditionalProperties(ValidAdditionalProperties.make())
                .setAdditionalProperties(null)
                .build();

        assertThat(container.getAdditionalProperties()).isEmpty();
    }

    @Test
    public void testBuilder_setAdditionalProperties_nullKey() {
        final Map<String, byte[]> mapWithNullKey = Maps.newHashMap(null, new byte[0]);
        assertThatThrownBy(() -> mBuilder.setAdditionalProperties(mapWithNullKey))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testBuild_setAdditionalProperty_nullValue() {
        final Map<String, byte[]> mapWithNullValue = Maps.newHashMap("a", null);
        assertThatThrownBy(() -> mBuilder.setAdditionalProperties(mapWithNullValue))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testBuilder_setAdditionalProperty() {
        AdditionalPropertiesContainer container = mBuilder
                .setAdditionalProperty(ADDITIONAL_PROP_TEST_KEY, ADDITIONAL_PROP_TWO_BYTE_VALUE)
                .setAdditionalProperty(ADDITIONAL_PROP_ANOTHER_KEY, ADDITIONAL_PROP_ZERO_BYTE_VALUE)
                .build();

        Map<String, byte[]> additionalProps = container.getAdditionalProperties();
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
        AdditionalPropertiesContainer container = mBuilder
                .setAdditionalProperty(ADDITIONAL_PROP_TEST_KEY, ADDITIONAL_PROP_TWO_BYTE_VALUE)
                .setAdditionalProperty(ADDITIONAL_PROP_TEST_KEY, ADDITIONAL_PROP_ZERO_BYTE_VALUE)
                .build();

        Map<String, byte[]> additionalProps = container.getAdditionalProperties();
        assertThat(additionalProps.size()).isEqualTo(1);
        assertThat(additionalProps.containsKey(ADDITIONAL_PROP_TEST_KEY));
        assertThat(additionalProps.get(ADDITIONAL_PROP_TEST_KEY))
                .isEqualTo(ADDITIONAL_PROP_ZERO_BYTE_VALUE);
    }

    @Test
    public void testBuilder_setAdditionalPropertyAsString() {
        AdditionalPropertiesContainer container = mBuilder
                .setAdditionalPropertyAsString(
                        ADDITIONAL_PROP_TEST_KEY,
                        ADDITIONAL_PROP_STRING_VALUE)
                .build();

        Map<String, byte[]> additionalProps = container.getAdditionalProperties();
        assertThat(additionalProps.size()).isEqualTo(1);
        assertThat(additionalProps.containsKey(ADDITIONAL_PROP_TEST_KEY));
        assertThat(additionalProps.get(ADDITIONAL_PROP_TEST_KEY))
                .isEqualTo(AdditionalPropertiesHelper.encodeStringValue(
                        ADDITIONAL_PROP_STRING_VALUE));
    }

    @Test
    public void testGetAdditionalProperty() {
        AdditionalPropertiesContainer container = mBuilder
                .setAdditionalProperties(ImmutableMap.of(
                        ADDITIONAL_PROP_TEST_KEY, ADDITIONAL_PROP_TWO_BYTE_VALUE))
                .build();

        assertThat(container.getAdditionalProperty(ADDITIONAL_PROP_TEST_KEY))
                .isEqualTo(ADDITIONAL_PROP_TWO_BYTE_VALUE);
    }

    @Test
    public void testGetAdditionalProperty_withMissingKey() {
        AdditionalPropertiesContainer container = mBuilder.build();
        assertThat(container.getAdditionalProperty("missingKey")).isNull();
    }

    @Test
    public void testGetAdditionalPropertyAsString() {
        AdditionalPropertiesContainer container = mBuilder
                .setAdditionalPropertyAsString(
                        ADDITIONAL_PROP_TEST_KEY, ADDITIONAL_PROP_STRING_VALUE)
                .build();

        assertThat(container.getAdditionalPropertyAsString(ADDITIONAL_PROP_TEST_KEY))
                .isEqualTo(ADDITIONAL_PROP_STRING_VALUE);
    }
}
