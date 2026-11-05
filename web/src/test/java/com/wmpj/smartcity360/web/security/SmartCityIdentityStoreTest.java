package com.wmpj.smartcity360.web.security;

import com.wmpj.smartcity360.core.security.AuthenticatedIdentity;
import com.wmpj.smartcity360.core.security.SecurityRoles;
import com.wmpj.smartcity360.core.service.AuthenticationService;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SmartCityIdentityStoreTest {

    @Test
    void returnsTheDatabaseIdentityAndGroupsForValidCredentials() {
        AuthenticatedIdentity identity = new AuthenticatedIdentity(
                7L,
                "Test Resident",
                "resident@example.test",
                Set.of(SecurityRoles.RESIDENT)
        );
        SmartCityIdentityStore store = new SmartCityIdentityStore(
                (email, password) -> Optional.of(identity)
        );

        CredentialValidationResult result = store.validate(
                new UsernamePasswordCredential(
                        "resident@example.test",
                        "correct password"
                )
        );

        assertEquals(CredentialValidationResult.Status.VALID, result.getStatus());
        assertEquals("resident@example.test", result.getCallerPrincipal().getName());
        assertEquals(Set.of(SecurityRoles.RESIDENT), result.getCallerGroups());
    }

    @Test
    void returnsOnlyGenericInvalidResultsForInvalidUnsupportedOrFailingCredentials() {
        SmartCityIdentityStore invalidStore = new SmartCityIdentityStore(
                (email, password) -> Optional.empty()
        );
        SmartCityIdentityStore failingStore = new SmartCityIdentityStore(
                new FailingAuthenticationService()
        );

        assertEquals(
                CredentialValidationResult.Status.INVALID,
                invalidStore.validate(
                        new UsernamePasswordCredential("unknown@example.test", "wrong")
                ).getStatus()
        );
        assertEquals(
                CredentialValidationResult.Status.NOT_VALIDATED,
                invalidStore.validate(new UnsupportedCredential()).getStatus()
        );
        assertEquals(
                CredentialValidationResult.Status.INVALID,
                failingStore.validate(
                        new UsernamePasswordCredential("resident@example.test", "password")
                ).getStatus()
        );
    }

    private static final class FailingAuthenticationService implements AuthenticationService {

        @Override
        public Optional<AuthenticatedIdentity> authenticate(String email, char[] password) {
            throw new IllegalStateException("synthetic authentication failure");
        }
    }

    private static final class UnsupportedCredential implements Credential {
    }
}
