package com.wmpj.smartcity360.web.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.AutoApplySession;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Objects;

@ApplicationScoped
@AutoApplySession
public class SmartCityAuthenticationMechanism implements HttpAuthenticationMechanism {

    @Inject
    private IdentityStoreHandler identityStoreHandler;

    public SmartCityAuthenticationMechanism() {
    }

    SmartCityAuthenticationMechanism(IdentityStoreHandler identityStoreHandler) {
        this.identityStoreHandler = Objects.requireNonNull(
                identityStoreHandler,
                "identityStoreHandler"
        );
    }

    @Override
    public AuthenticationStatus validateRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpMessageContext messageContext
    ) {
        AuthenticationParameters parameters = messageContext.getAuthParameters();
        Credential credential = parameters.getCredential();

        if (credential == null) {
            if (messageContext.isProtected()) {
                return messageContext.responseUnauthorized();
            }

            return messageContext.doNothing();
        }

        CredentialValidationResult validationResult = identityStoreHandler.validate(credential);

        if (validationResult.getStatus() == CredentialValidationResult.Status.VALID) {
            return messageContext.notifyContainerAboutLogin(validationResult);
        }

        return AuthenticationStatus.SEND_FAILURE;
    }
}
