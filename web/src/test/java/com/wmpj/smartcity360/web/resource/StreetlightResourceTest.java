package com.wmpj.smartcity360.web.resource;

import com.wmpj.smartcity360.core.exception.UnauthenticatedException;
import com.wmpj.smartcity360.core.service.StreetlightManagementService;
import com.wmpj.smartcity360.web.security.BusinessApiSecurity;
import com.wmpj.smartcity360.web.security.InvalidRequestException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

class StreetlightResourceTest {

    @Test
    void rejectsAnonymousAccessBeforeServiceInvocation() {
        StreetlightResource resource = new StreetlightResource(
                unusedService(),
                new BusinessApiSecurity(securityContext(false))
        );

        assertThrows(
                UnauthenticatedException.class,
                () -> resource.list(null, 0, 20)
        );
        assertThrows(
                UnauthenticatedException.class,
                () -> resource.updateStatus(1L, null)
        );
    }

    @Test
    void rejectsInvalidIdentifiersAndUnboundedPages() {
        StreetlightResource resource = new StreetlightResource(
                unusedService(),
                new BusinessApiSecurity(securityContext(true))
        );

        assertThrows(InvalidRequestException.class, () -> resource.get(0L));
        assertThrows(
                InvalidRequestException.class,
                () -> resource.list(null, 0, 101)
        );
        assertThrows(
                InvalidRequestException.class,
                () -> resource.assignProfile(1L, null)
        );
    }

    private static StreetlightManagementService unusedService() {
        return (StreetlightManagementService) Proxy.newProxyInstance(
                StreetlightManagementService.class.getClassLoader(),
                new Class<?>[]{StreetlightManagementService.class},
                (proxy, method, arguments) -> {
                    throw new AssertionError("Service must not be invoked");
                }
        );
    }

    private static SecurityContext securityContext(boolean authenticated) {
        return new SecurityContext() {
            @Override
            public Principal getCallerPrincipal() {
                if (authenticated) {
                    return () -> "staff@example.test";
                }

                return null;
            }

            @Override
            public <T extends Principal> Set<T> getPrincipalsByType(Class<T> principalType) {
                return Set.of();
            }

            @Override
            public boolean isCallerInRole(String role) {
                return authenticated;
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
