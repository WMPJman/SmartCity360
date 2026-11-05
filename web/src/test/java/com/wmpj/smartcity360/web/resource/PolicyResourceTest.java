package com.wmpj.smartcity360.web.resource;

import com.wmpj.smartcity360.core.exception.UnauthenticatedException;
import com.wmpj.smartcity360.core.service.PolicyManagementService;
import com.wmpj.smartcity360.web.dto.PolicyCreationRequest;
import com.wmpj.smartcity360.web.dto.PolicyEvaluationRequest;
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
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PolicyResourceTest {

    @Test
    void rejectsAnonymousPolicyAccessBeforeServiceInvocation() {
        PolicyResource resource = new PolicyResource(
                unusedService(),
                new BusinessApiSecurity(securityContext(false))
        );

        assertThrows(UnauthenticatedException.class, () -> resource.list(0, 20));
        assertThrows(
                UnauthenticatedException.class,
                () -> resource.evaluate(1L, new PolicyEvaluationRequest())
        );
    }

    @Test
    void rejectsMalformedRequestsAtRestBoundary() {
        PolicyResource resource = new PolicyResource(
                unusedService(),
                new BusinessApiSecurity(securityContext(true))
        );
        PolicyCreationRequest creationRequest = new PolicyCreationRequest();
        creationRequest.setPolicyCode("INVALID CODE");
        PolicyEvaluationRequest evaluationRequest = new PolicyEvaluationRequest();
        evaluationRequest.setNumericValues(Map.of());
        evaluationRequest.setBooleanValues(Map.of());

        assertThrows(
                InvalidRequestException.class,
                () -> resource.create(creationRequest)
        );
        assertThrows(InvalidRequestException.class, () -> resource.get(0L));
        assertThrows(
                InvalidRequestException.class,
                () -> resource.list(0, 101)
        );
        assertThrows(
                InvalidRequestException.class,
                () -> resource.evaluate(0L, evaluationRequest)
        );
    }

    private static PolicyManagementService unusedService() {
        return (PolicyManagementService) Proxy.newProxyInstance(
                PolicyManagementService.class.getClassLoader(),
                new Class<?>[]{PolicyManagementService.class},
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
                    return () -> "manager@example.test";
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
