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

package org.openyolo.spi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.bbq.Protobufs.BroadcastQuery;
import com.google.protobuf.ByteString;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openyolo.protocol.AuthenticationDomain;
import org.openyolo.protocol.AuthenticationMethod;
import org.openyolo.protocol.CredentialRetrieveRequest;
import org.openyolo.protocol.Protobufs;
import org.openyolo.spi.Constants.ValidApplication;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Unit tests for {@link BaseCredentialQueryReceiver}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE,
        shadows = {
                ShadowQueryResponseSender.class,
                ShadowAuthenticationDomain.class
        })
public class BaseCredentialQueryReceiverTest {

    private BaseCredentialQueryReceiver credentialQueryReceiver;

    @Mock
    Context mockContext;

    private static final BroadcastQuery QUERY_WITH_NULL_MESSAGE =
            makeQueryFromRequest(null /* request */);

    private static final BroadcastQuery QUERY_WITH_INVALID_REQUEST =
            makeQueryFromRequest(makeInvalidCredentialRetrieveRequest());

    private static final BroadcastQuery QUERY_WITH_VALID_REQUEST =
            makeQueryFromRequest(makeValidCredentialRetrieveRequest());

    private static BroadcastQuery makeQueryFromRequest(
            @Nullable Protobufs.CredentialRetrieveRequest request) {
        BroadcastQuery.Builder builder = BroadcastQuery.newBuilder()
                .setRequestingApp(ValidApplication.PACKAGE_NAME)
                .setDataType("blah")
                .setRequestId(101L)
                .setResponseId(102L);

        if (request != null) {
            builder.setQueryMessage(ByteString.copyFrom(request.toByteArray()));
        }

        return builder.build();
    }

    private static Protobufs.CredentialRetrieveRequest makeInvalidCredentialRetrieveRequest() {
        // Invalid because it does not specify a non-empty set of authentication method
        return Protobufs.CredentialRetrieveRequest.newBuilder().build();
    }

    private static Protobufs.CredentialRetrieveRequest makeValidCredentialRetrieveRequest() {
        return Protobufs.CredentialRetrieveRequest.newBuilder()
            .addAuthMethods(new AuthenticationMethod("custom://one").toProtobuf())
            .addAuthMethods(new AuthenticationMethod("custom://two").toProtobuf())
            .build();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ShadowQueryResponseSender.intializeForTest();

        credentialQueryReceiver = spy(
                new BaseCredentialQueryReceiver("TestCredentialQueryReceiver") {
                    @Override
                    protected void processCredentialRequest(
                            @NonNull Context context,
                            @NonNull BroadcastQuery query,
                            @NonNull CredentialRetrieveRequest request,
                            @NonNull Set<AuthenticationDomain> requestorDomains) {
                        // Do nothing
                    }
                });

        // ShadowAuthenticationDomain.(VALID_AUTHENTICATION_DOMAINS);
        ShadowAuthenticationDomain.reset();
    }

    @Test
    public void processQuery_withNullQuery_sendsNullResponse() throws Exception {
        credentialQueryReceiver.processQuery(mockContext, QUERY_WITH_NULL_MESSAGE);

        verifyProcessCredentialRequestWasNotCalled();
        verifyQueryResponseIsSentWith(null /* response */);
    }

    @Test
    public void processQuery_withInvalidRequest_sendsNullResponse() throws Exception {
        credentialQueryReceiver.processQuery(mockContext, QUERY_WITH_INVALID_REQUEST);

        verifyProcessCredentialRequestWasNotCalled();
        verifyQueryResponseIsSentWith(null /* response */);
    }

    @Test
    public void processQuery_withValidRequest_processesRequest() throws Exception {
        ShadowAuthenticationDomain.setAuthDomainForPackage(
                ValidApplication.PACKAGE_NAME,
                ValidApplication.AUTHENTICATION_DOMAIN);

        credentialQueryReceiver.processQuery(mockContext, QUERY_WITH_VALID_REQUEST);

        verifyProcessCredentialRequestWasCalled();
        verifyQueryResponseIsNotSent();
    }

    @Test
    public void processQuery_withValidRequestAndUnableToDetermineAuthenticationDomain_sendsNullResponse()
            throws Exception {
        credentialQueryReceiver.processQuery(mockContext, QUERY_WITH_VALID_REQUEST);

        verifyProcessCredentialRequestWasNotCalled();
        verifyQueryResponseIsSentWith(null /* response */);
    }

    private void verifyQueryResponseIsNotSent() {
        verify(ShadowQueryResponseSender.mockQueryResponseSender, times(0))
                .sendResponse((BroadcastQuery) any(), (byte[]) any());
    }

    private void verifyQueryResponseIsSentWith(@Nullable byte[] response) {
        if (null == response) {
            verify(ShadowQueryResponseSender.mockQueryResponseSender, times(1))
                    .sendResponse((BroadcastQuery) any(), (byte[]) isNull());
            return;
        }

        verify(ShadowQueryResponseSender.mockQueryResponseSender, times(1))
                .sendResponse((BroadcastQuery) any(), eq(response));
    }

    private void verifyProcessCredentialRequestWasCalled() {
        verify(credentialQueryReceiver, times(1))
                .processCredentialRequest(
                        (Context) any(),
                        (BroadcastQuery) any(),
                        (CredentialRetrieveRequest) any(),
                        (Set<AuthenticationDomain>) any());
    }

    private void verifyProcessCredentialRequestWasNotCalled() {
        verify(credentialQueryReceiver, times(0))
                .processCredentialRequest(
                        (Context) any(),
                        (BroadcastQuery) any(),
                        (CredentialRetrieveRequest) any(),
                        (Set<AuthenticationDomain>) any());
    }
}