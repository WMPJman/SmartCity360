package com.wmpj.smartcity360.web.security;

import com.wmpj.smartcity360.core.security.AuthenticatedIdentity;
import com.wmpj.smartcity360.core.service.AuthenticationService;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.CallerPrincipal;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class SmartCityIdentityStore implements IdentityStore {

    private static final Logger LOGGER = Logger.getLogger(SmartCityIdentityStore.class.getName());

    @EJB
    private AuthenticationService authenticationService;

    public SmartCityIdentityStore() {
    }

    SmartCityIdentityStore(AuthenticationService authenticationService) {
        this.authenticationService = Objects.requireNonNull(
                authenticationService,
                "authenticationService"
        );
    }

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (!(credential instanceof UsernamePasswordCredential usernamePassword)) {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }

        char[] password = usernamePassword.getPassword().getValue();
        char[] passwordCopy = Arrays.copyOf(password, password.length);

        try {
            Optional<AuthenticatedIdentity> result = authenticationService.authenticate(
                    usernamePassword.getCaller(),
                    passwordCopy
            );

            if (result.isEmpty()) {
                return CredentialValidationResult.INVALID_RESULT;
            }

            AuthenticatedIdentity identity = result.orElseThrow();

            return new CredentialValidationResult(
                    new CallerPrincipal(identity.email()),
                    identity.roleCodes()
            );
        } catch (RuntimeException exception) {
            LOGGER.log(
                    Level.WARNING,
                    "event=IDENTITY_STORE_FAILURE exceptionType={0}",
                    exception.getClass().getName()
            );

            return CredentialValidationResult.INVALID_RESULT;
        } finally {
            Arrays.fill(passwordCopy, '\0');
            usernamePassword.clearCredential();
        }
    }
}
