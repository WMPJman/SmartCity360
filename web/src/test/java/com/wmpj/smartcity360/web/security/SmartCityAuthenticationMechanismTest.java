package com.wmpj.smartcity360.web.security;

import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class SmartCityAuthenticationMechanismTest {

    @Test
    void notifiesTheContainerWhenProgrammaticCredentialsAreValid() {
        CredentialValidationResult validResult = new CredentialValidationResult(
                "resident@example.test",
                Set.of("RESIDENT")
        );
        SmartCityAuthenticationMechanism mechanism = new SmartCityAuthenticationMechanism(
                credential -> validResult
        );
        AtomicReference<CredentialValidationResult> notifiedResult = new AtomicReference<>();
        HttpMessageContext context = context(
                AuthenticationParameters.withParams().credential(
                        new UsernamePasswordCredential(
                                "resident@example.test",
                                "correct password"
                        )
                ),
                notifiedResult
        );

        AuthenticationStatus status = mechanism.validateRequest(null, null, context);

        assertEquals(AuthenticationStatus.SUCCESS, status);
        assertSame(validResult, notifiedResult.get());
    }

    @Test
    void rejectsInvalidCredentialsAndIgnoresOrdinaryRequests() {
        IdentityStoreHandler invalidHandler = credential -> CredentialValidationResult.INVALID_RESULT;
        SmartCityAuthenticationMechanism mechanism = new SmartCityAuthenticationMechanism(
                invalidHandler
        );
        AtomicBoolean unauthorizedResponseCommitted = new AtomicBoolean();
        HttpMessageContext loginContext = context(
                AuthenticationParameters.withParams().credential(
                        new UsernamePasswordCredential("unknown@example.test", "wrong")
                ),
                new AtomicReference<>(),
                unauthorizedResponseCommitted
        );
        HttpMessageContext ordinaryContext = context(
                AuthenticationParameters.withParams(),
                new AtomicReference<>()
        );

        assertEquals(
                AuthenticationStatus.SEND_FAILURE,
                mechanism.validateRequest(null, null, loginContext)
        );
        assertEquals(
                AuthenticationStatus.NOT_DONE,
                mechanism.validateRequest(null, null, ordinaryContext)
        );
        assertFalse(unauthorizedResponseCommitted.get());
    }

    @Test
    void challengesAnonymousRequestsForProtectedResources() {
        SmartCityAuthenticationMechanism mechanism = new SmartCityAuthenticationMechanism(
                credential -> CredentialValidationResult.INVALID_RESULT
        );
        HttpMessageContext protectedContext = context(
                AuthenticationParameters.withParams(),
                new AtomicReference<>(),
                new AtomicBoolean(),
                true
        );

        AuthenticationStatus status = mechanism.validateRequest(
                null,
                null,
                protectedContext
        );

        assertEquals(AuthenticationStatus.SEND_FAILURE, status);
    }

    private static HttpMessageContext context(
            AuthenticationParameters parameters,
            AtomicReference<CredentialValidationResult> notifiedResult
    ) {
        return context(parameters, notifiedResult, new AtomicBoolean());
    }

    private static HttpMessageContext context(
            AuthenticationParameters parameters,
            AtomicReference<CredentialValidationResult> notifiedResult,
            AtomicBoolean unauthorizedResponseCommitted
    ) {
        return context(
                parameters,
                notifiedResult,
                unauthorizedResponseCommitted,
                false
        );
    }

    private static HttpMessageContext context(
            AuthenticationParameters parameters,
            AtomicReference<CredentialValidationResult> notifiedResult,
            AtomicBoolean unauthorizedResponseCommitted,
            boolean protectedResource
    ) {
        return (HttpMessageContext) Proxy.newProxyInstance(
                HttpMessageContext.class.getClassLoader(),
                new Class<?>[]{HttpMessageContext.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("getAuthParameters")) {
                        return parameters;
                    }

                    if (method.getName().equals("isProtected")) {
                        return protectedResource;
                    }

                    if (method.getName().equals("notifyContainerAboutLogin")) {
                        notifiedResult.set((CredentialValidationResult) arguments[0]);
                        return AuthenticationStatus.SUCCESS;
                    }

                    if (method.getName().equals("responseUnauthorized")) {
                        unauthorizedResponseCommitted.set(true);
                        return AuthenticationStatus.SEND_FAILURE;
                    }

                    if (method.getName().equals("doNothing")) {
                        return AuthenticationStatus.NOT_DONE;
                    }

                    return null;
                }
        );
    }
}
