package com.wmpj.smartcity360.web.security;

import com.wmpj.smartcity360.core.exception.UnauthenticatedException;
import jakarta.ejb.EJBAccessException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessApiSecurityTest {

    @Test
    void distinguishesAnonymousUnauthorizedAndAllowedCallers() {
        assertThrows(
                UnauthenticatedException.class,
                () -> new BusinessApiSecurity(context(null, Set.of()))
                        .requireAnyRole("ADMIN")
        );
        assertThrows(
                EJBAccessException.class,
                () -> new BusinessApiSecurity(context("resident", Set.of("RESIDENT")))
                        .requireAnyRole("ADMIN")
        );
        assertDoesNotThrow(
                () -> new BusinessApiSecurity(context("admin", Set.of("ADMIN")))
                        .requireAnyRole("ADMIN")
        );
    }

    private static SecurityContext context(String callerName, Set<String> roles) {
        return new SecurityContext() {
            @Override
            public Principal getCallerPrincipal() {
                if (callerName == null) {
                    return null;
                }

                return () -> callerName;
            }

            @Override
            public <T extends Principal> Set<T> getPrincipalsByType(Class<T> principalType) {
                return Set.of();
            }

            @Override
            public boolean isCallerInRole(String role) {
                return roles.contains(role);
            }

            @Override
            public boolean hasAccessToWebResource(String resource, String... methods) {
                return false;
            }

            @Override
            public AuthenticationStatus authenticate(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    AuthenticationParameters parameters
            ) {
                return AuthenticationStatus.NOT_DONE;
            }
        };
    }
}
