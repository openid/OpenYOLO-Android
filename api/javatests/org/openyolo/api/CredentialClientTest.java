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

import static junit.framework.TestCase.assertNotNull;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.openyolo.protocol.AuthenticationMethods.EMAIL;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.google.bbq.BroadcastQueryClient;
import com.google.bbq.QueryCallback;

import java.util.ArrayList;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openyolo.api.persistence.DeviceState;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.AuthenticationMethods;
import org.openyolo.protocol.Credential;
import org.openyolo.protocol.CredentialRetrieveRequest;
import org.openyolo.protocol.CredentialSaveRequest;
import org.openyolo.protocol.CredentialSaveResult;
import org.openyolo.protocol.HintRetrieveRequest;
import org.openyolo.protocol.CredentialRetrieveResult;
import org.openyolo.protocol.Protobufs;
import org.openyolo.protocol.ProtocolConstants;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Units tests for {@link CredentialClient}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CredentialClientTest {

    private static final String EMAIL_ID = "alice@example.com";
    private static final AuthenticationDomain AUTH_DOMAIN =
            new AuthenticationDomain("https://www.example.com");

    private static final String DASHLANE =
            KnownProviders.DASHLANE_PROVIDER.getAndroidPackageName();

    private static final String ONEPASSWORD =
            KnownProviders.ONEPASSWORD_PROVIDER.getAndroidPackageName();

    private static final String GOOGLE =
            KnownProviders.GOOGLE_PROVIDER.getAndroidPackageName();

    private static final String UNKNOWN_PROVIDER_1 = "com.example.superpassword";

    private static final String UNKNOWN_PROVIDER_2 = "com.example.locker";

    private static final byte[] INVALID_PROTO = new byte[] { 0, 1, 2, 3 };

    @Mock
    private Context mockContext;

    @Mock
    private PackageManager mockPackageManager;

    @Mock
    private BroadcastQueryClient mockBroadcastQueryClient;

    @Mock
    private KnownProviders mockKnownProviders;

    @Mock
    private DeviceState mockDeviceState;

    private CredentialClient credentialClient;
    private Credential testCredential;

    private ArrayList<ResolveInfo> installedProviders;

    @SuppressWarnings("WrongConstant")
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        installedProviders = new ArrayList<>();

        when(mockContext.getApplicationContext()).thenReturn(mockContext);

        testCredential = new Credential.Builder(
                EMAIL_ID,
                EMAIL,
                new AuthenticationDomain("https://www.example.com")).build();

        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
        when(mockPackageManager.queryIntentActivities(any(Intent.class), anyInt()))
                .thenReturn(installedProviders);

        KnownProviders.setApplicationBoundInstance(mockKnownProviders);

        CredentialClientOptions options =
                new CredentialClientOptions.Builder(mockDeviceState).build();
        credentialClient = new CredentialClient(mockContext, mockBroadcastQueryClient, options);
        testCredential = new Credential.Builder(EMAIL_ID, EMAIL, AUTH_DOMAIN).build();
    }

    @After
    public void tearDown() throws Exception {
        KnownProviders.clearApplicationBoundInstance();
    }

    @Test
    public void getInstance_returnsNonNull() throws Exception {
        CredentialClient test = CredentialClient.getInstance(mockContext);
        assertNotNull(test);
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void testGetHintIntent_noProviders() throws Exception {
        when(mockPackageManager.queryIntentActivities(any(Intent.class), anyInt()))
                .thenReturn(Collections.<ResolveInfo>emptyList());
        Intent hintIntent = credentialClient.getHintRetrieveIntent(
                HintRetrieveRequest.forEmailAndPasswordAccount());

        assertThat(hintIntent).isNull();
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void testGetHintIntent_unknownProviderPresent() throws Exception {
        addKnownProviders(DASHLANE);
        addUnknownProviders(UNKNOWN_PROVIDER_1);
        Intent hintIntent = credentialClient.getHintRetrieveIntent(
                HintRetrieveRequest.forEmailAndPasswordAccount());

        // with an unknown provider, the heuristic determines is no preferred provider.
        // Therefore a provider picker should be displayed.
        assertThat(hintIntent).isNotNull();
        assertThat(hintIntent.getComponent().getClassName())
                .endsWith("ProviderPickerActivity");
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void testGetHintIntent_singleKnownProvider() throws Exception {
        addKnownProviders(DASHLANE);
        Intent hintIntent = credentialClient.getHintRetrieveIntent(
                HintRetrieveRequest.forEmailAndPasswordAccount());
        assertThat(hintIntent).isNotNull();
        assertThat(hintIntent.getComponent().getPackageName()).isEqualTo(DASHLANE);
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void testGetHintIntent_twoKnownProviders_googleAndDashlane() throws Exception {
        addKnownProviders(DASHLANE, GOOGLE);
        Intent hintIntent = credentialClient.getHintRetrieveIntent(
                HintRetrieveRequest.forEmailAndPasswordAccount());

        // due to the preferred provider selection heuristics, we expect that Dashlane will be
        // selected for direct invocation.
        assertThat(hintIntent).isNotNull();
        assertThat(hintIntent.getComponent().getPackageName()).isEqualTo(DASHLANE);
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void testGetHintIntent_twoKnownProviders_dashlaneAnd1Password() throws Exception {
        addKnownProviders(DASHLANE, ONEPASSWORD);
        Intent hintIntent = credentialClient.getHintRetrieveIntent(
                HintRetrieveRequest.forEmailAndPasswordAccount());

        // due to the preferred provider selection heuristics, we expect that Dashlane will be
        // selected for direct invocation.
        assertThat(hintIntent).isNotNull();
        assertThat(hintIntent.getComponent().getClassName())
                .endsWith("ProviderPickerActivity");
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void testGetHintIntent_threeKnownProviders() throws Exception {
        addKnownProviders(DASHLANE, ONEPASSWORD, GOOGLE);
        Intent hintIntent = credentialClient.getHintRetrieveIntent(
                HintRetrieveRequest.forEmailAndPasswordAccount());

        // with three known providers, the preferred provider heuristic determines there is no
        // preferred provider. Therefore, a provider picker should be displayed.
        assertThat(hintIntent).isNotNull();
        assertThat(hintIntent.getComponent().getClassName())
                .endsWith("ProviderPickerActivity");
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void testGetHintIntent_noKnownProviders() throws Exception {
        addUnknownProviders(UNKNOWN_PROVIDER_1, UNKNOWN_PROVIDER_2);
        Intent hintIntent = credentialClient.getHintRetrieveIntent(
                HintRetrieveRequest.forEmailAndPasswordAccount());

        // With no known providers, the preferred provider heuristic will determine there is no
        // preferred provider. therefore, a provider picker should be displayed.
        assertThat(hintIntent).isNotNull();
        assertThat(hintIntent.getComponent().getClassName())
                .endsWith("ProviderPickerActivity");
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void testGetSaveIntent_noProviders() throws Exception {
        final CredentialSaveRequest request = CredentialSaveRequest.fromCredential(testCredential);
        final Intent saveIntent = credentialClient.getSaveIntent(request);

        assertThat(saveIntent).isNull();
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void getSaveIntent_singleKnownProvider() throws Exception {
        addKnownProviders(DASHLANE);

        final CredentialSaveRequest request = CredentialSaveRequest.fromCredential(testCredential);
        final Intent saveIntent = credentialClient.getSaveIntent(request);

        assertThat(saveIntent).isNotNull();
        assertThat(saveIntent.getComponent().getPackageName()).isEqualTo(DASHLANE);
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void getSaveIntent_unknownProviderPresent() throws Exception {
        addKnownProviders(DASHLANE);
        addUnknownProviders(UNKNOWN_PROVIDER_1);

        // with an unknown provider, the heuristic determines is no preferred provider.
        // Therefore a provider picker should be displayed.
        final CredentialSaveRequest request = CredentialSaveRequest.fromCredential(testCredential);
        final Intent saveIntent = credentialClient.getSaveIntent(request);

        assertThat(saveIntent).isNotNull();
        assertThat(saveIntent.getComponent().getClassName())
                .endsWith("ProviderPickerActivity");
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void getSaveIntent_twoKnownProviders_googleAndDashlane() throws Exception {
        addKnownProviders(DASHLANE, GOOGLE);

        // due to the preferred provider selection heuristics, we expect that Dashlane will be
        // selected for direct invocation.
        final CredentialSaveRequest request = CredentialSaveRequest.fromCredential(testCredential);
        final Intent saveIntent = credentialClient.getSaveIntent(request);

        assertThat(saveIntent).isNotNull();
        assertThat(saveIntent.getComponent().getPackageName()).isEqualTo(DASHLANE);
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void getSaveIntent_twoKnownProviders_dashlaneAnd1Password() throws Exception {
        addKnownProviders(DASHLANE, ONEPASSWORD);

        // as there are two known providers, and neither is Google, the preferred provider
        // heuristics should determine there are no preferred providers. Therefore, a provider
        // picker should be launched.
        final CredentialSaveRequest request = CredentialSaveRequest.fromCredential(testCredential);
        final Intent saveIntent = credentialClient.getSaveIntent(request);

        assertThat(saveIntent).isNotNull();
        assertThat(saveIntent.getComponent().getClassName())
                .endsWith("ProviderPickerActivity");
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void getSaveIntent_threeKnownProviders() throws Exception {
        addKnownProviders(DASHLANE, ONEPASSWORD, GOOGLE);

        // with three known providers, the preferred provider heuristic determines there is no
        // preferred provider. Therefore, a provider picker should be displayed.
        final CredentialSaveRequest request = CredentialSaveRequest.fromCredential(testCredential);
        final Intent saveIntent = credentialClient.getSaveIntent(request);

        assertThat(saveIntent).isNotNull();
        assertThat(saveIntent.getComponent().getClassName())
                .endsWith("ProviderPickerActivity");
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void getSaveIntent_noKnownProviders() throws Exception {
        addUnknownProviders(UNKNOWN_PROVIDER_1, UNKNOWN_PROVIDER_2);

        // With no known providers, the preferred provider heuristic will determine there is no
        // preferred provider. therefore, a provider picker should be displayed.
        final CredentialSaveRequest request = CredentialSaveRequest.fromCredential(testCredential);
        final Intent saveIntent = credentialClient.getSaveIntent(request);

        assertThat(saveIntent).isNotNull();
        assertThat(saveIntent.getComponent().getClassName())
                .endsWith("ProviderPickerActivity");
    }

    @Test
    public void getCredentialSaveResult_withNullIntent_returnsDefaultResult() {
        final CredentialSaveResult result =
                credentialClient.getCredentialSaveResult(null /* intent*/);

        assertThat(result.getResultCode()).isEqualTo(CredentialSaveResult.CODE_UNKNOWN);
        assertThat(result.getAdditionalProperties()).isEmpty();
    }

    @Test
    public void getCredentialSaveResult_missingExtra_returnsDefaultResult() {
        final Intent intent = new Intent();
        final CredentialSaveResult result = credentialClient.getCredentialSaveResult(intent);

        assertThat(result.getResultCode()).isEqualTo(CredentialSaveResult.CODE_UNKNOWN);
        assertThat(result.getAdditionalProperties()).isEmpty();
    }

    @Test
    public void getCredentialSaveResult_withInvalidProto_returnsDefaultResult() {
        final Intent intent = new Intent();
        intent.putExtra(ProtocolConstants.EXTRA_SAVE_RESULT, INVALID_PROTO);
        final CredentialSaveResult result = credentialClient.getCredentialSaveResult(intent);

        assertThat(result.getResultCode()).isEqualTo(CredentialSaveResult.CODE_UNKNOWN);
        assertThat(result.getAdditionalProperties()).isEmpty();
    }

    @Test
    public void getCredentialSaveResult_validResult_returnsResult() {
        final CredentialSaveResult result =
                credentialClient.getCredentialSaveResult(
                        CredentialSaveResult.SAVED.toResultDataIntent());

        assertThat(result.getResultCode()).isEqualTo(CredentialSaveResult.CODE_SAVED);
        assertThat(result.getAdditionalProperties()).isEmpty();
    }

    @Test
    public void getCredentialRetrieveResult_withErrorResult_returnsErrorResult() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ProtocolConstants.EXTRA_RETRIEVE_RESULT,
                new CredentialRetrieveResult.Builder(
                        CredentialRetrieveResult.CODE_NO_CREDENTIALS_AVAILABLE)
                        .build()
                        .toProtobuf()
                        .toByteArray());

        CredentialRetrieveResult result = credentialClient.getCredentialRetrieveResult(intent);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getResultCode()).isEqualTo(
                CredentialRetrieveResult.CODE_NO_CREDENTIALS_AVAILABLE);
        assertThat(result.getCredential()).isNull();
        assertThat(result.getAdditionalProps()).isEmpty();

        verifyNoMoreInteractions(mockDeviceState);
    }

    @Test
    public void getCredentialRetrieveResult_withSuccessfulResult_returnsSuccessfulResult() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(ProtocolConstants.EXTRA_RETRIEVE_RESULT,
                new CredentialRetrieveResult.Builder(
                        CredentialRetrieveResult.CODE_CREDENTIAL_SELECTED)
                        .setCredential(testCredential)
                        .build()
                        .toProtobuf()
                        .toByteArray());

        CredentialRetrieveResult result = credentialClient.getCredentialRetrieveResult(intent);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getResultCode()).isEqualTo(
                CredentialRetrieveResult.CODE_CREDENTIAL_SELECTED);

        assertThat(result.getCredential()).isNotNull();
        assertThat(result.getCredential().getIdentifier())
                .isEqualTo(testCredential.getIdentifier());
        assertThat(result.getAdditionalProps()).isEmpty();

        verify(mockDeviceState).setIsAutoSignInDisabled(eq(false));
    }

    @Test
    public void testGetCredentialRetrieveResult_noExtra() throws Exception {
        Intent intent = new Intent();
        CredentialRetrieveResult result = credentialClient.getCredentialRetrieveResult(intent);
        assertThat(result).isNotNull();
        assertThat(result.getResultCode()).isEqualTo(CredentialRetrieveResult.CODE_UNKNOWN);
        assertThat(result.getCredential()).isNull();
        assertThat(result.getAdditionalProps()).isEmpty();
    }

    @Test
    public void retrieve_autoSignInDisabled_requestRequiresUserMediation() {
        CredentialRetrieveRequest request =
                CredentialRetrieveRequest.forAuthenticationMethods(AuthenticationMethods.EMAIL);

        when(mockDeviceState.isAutoSignInDisabled()).thenReturn(true);

        // Act
        credentialClient.retrieve(request, null /* callBack */);

        // Assert
        ArgumentCaptor<Protobufs.CredentialRetrieveRequest> requestArgumentCaptor =
                ArgumentCaptor.forClass(Protobufs.CredentialRetrieveRequest.class);
        verify(mockBroadcastQueryClient)
                .queryFor(anyString(), requestArgumentCaptor.capture(), any(QueryCallback.class));
        assertThat(requestArgumentCaptor.getValue().getRequireUserMediation()).isTrue();
    }

    @Test
    public void retrieve_autoSignInEnabled_requestDoesNotRequireUserMediation() {
        CredentialRetrieveRequest request =
                new CredentialRetrieveRequest.Builder(AuthenticationMethods.EMAIL)
                        .setRequireUserMediation(false)
                        .build();

        when(mockDeviceState.isAutoSignInDisabled()).thenReturn(false);

        // Act
        credentialClient.retrieve(request, null /* callBack */);

        // Assert
        ArgumentCaptor<Protobufs.CredentialRetrieveRequest> requestArgumentCaptor =
                ArgumentCaptor.forClass(Protobufs.CredentialRetrieveRequest.class);
        verify(mockBroadcastQueryClient)
                .queryFor(anyString(), requestArgumentCaptor.capture(), any(QueryCallback.class));
        assertThat(requestArgumentCaptor.getValue().getRequireUserMediation()).isFalse();
    }

    private void addKnownProviders(String... packageNames) {
        for (String packageName : packageNames) {
            installedProviders.add(createResolveInfo(packageName, packageName));
            when(mockKnownProviders.isKnown(packageName)).thenReturn(true);
        }
    }

    private void addUnknownProviders(String... packageNames) {
        for (String packageName : packageNames) {
            installedProviders.add(createResolveInfo(packageName, packageName));
            when(mockKnownProviders.isKnown(packageName)).thenReturn(false);
        }
    }

    private ResolveInfo createResolveInfo(String packageName, String name) {
        ResolveInfo resolveInfo = new ResolveInfo();
        ActivityInfo activityInfo = new ActivityInfo();
        activityInfo.packageName = packageName;
        activityInfo.name =  name;
        resolveInfo.activityInfo = activityInfo;

        return resolveInfo;
    }
}
