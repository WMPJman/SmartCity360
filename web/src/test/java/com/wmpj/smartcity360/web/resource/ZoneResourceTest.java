package com.wmpj.smartcity360.web.resource;

import com.wmpj.smartcity360.core.dto.PageRequest;
import com.wmpj.smartcity360.core.dto.PageResponse;
import com.wmpj.smartcity360.core.dto.ZoneSummary;
import com.wmpj.smartcity360.core.entity.ZoneType;
import com.wmpj.smartcity360.core.service.ZoneQueryService;
import com.wmpj.smartcity360.core.service.ZoneCompositeService;
import com.wmpj.smartcity360.web.security.InvalidRequestException;
import com.wmpj.smartcity360.core.exception.UnauthenticatedException;
import com.wmpj.smartcity360.web.security.BusinessApiSecurity;
import jakarta.security.enterprise.SecurityContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZoneResourceTest {

    @Test
    void delegatesValidatedPaginationWithoutExposingEntities() {
        AtomicReference<PageRequest> requestReceived = new AtomicReference<>();
        ZoneQueryService service = (zoneType, pageRequest) -> {
            requestReceived.set(pageRequest);
            return new PageResponse<>(List.of(), pageRequest.offset(), pageRequest.limit());
        };
        ZoneResource resource = new ZoneResource(service, allowedSecurity());

        PageResponse<ZoneSummary> response = resource.findZones("district", 5, 25);

        assertEquals(new PageRequest(5, 25), requestReceived.get());
        assertEquals(5, response.offset());
        assertEquals(25, response.limit());
        assertThrows(
                InvalidRequestException.class,
                () -> resource.findZones("unknown", 0, 20)
        );
    }

    @Test
    void compositeEndpointsRejectAnonymousCallersBeforeServiceInvocation() {
        ZoneQueryService queryService = (zoneType, pageRequest) ->
                new PageResponse<>(List.of(), pageRequest.offset(), pageRequest.limit());
        ZoneResource resource = new ZoneResource(
                queryService,
                unusedCompositeService(),
                new BusinessApiSecurity(anonymousContext())
        );

        assertThrows(
                UnauthenticatedException.class,
                () -> resource.getHierarchy(1L)
        );
    }

    @Test
    private static BusinessApiSecurity allowedSecurity() {
        SecurityContext securityContext = new SecurityContext() {
            @Override
            public java.security.Principal getCallerPrincipal() {
                return () -> "manager@example.test";
            }

            @Override
            public <T extends java.security.Principal> Set<T> getPrincipalsByType(
                    Class<T> principalType
            ) {
                return Set.of();
            }

            @Override
            public boolean isCallerInRole(String role) {
                return true;
            }

            @Override
            public boolean hasAccessToWebResource(String resource, String... methods) {
                return false;
            }

            @Override
            public jakarta.security.enterprise.AuthenticationStatus authenticate(
                    jakarta.servlet.http.HttpServletRequest request,
                    jakarta.servlet.http.HttpServletResponse response,
                    jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters parameters
            ) {
                return jakarta.security.enterprise.AuthenticationStatus.NOT_DONE;
            }
        };

        return new BusinessApiSecurity(securityContext);
    }

    private static SecurityContext anonymousContext() {
        return new SecurityContext() {
            @Override
            public java.security.Principal getCallerPrincipal() {
                return null;
            }

            @Override
            public <T extends java.security.Principal> Set<T> getPrincipalsByType(
                    Class<T> principalType
            ) {
                return Set.of();
            }

            @Override
            public boolean isCallerInRole(String role) {
                return false;
            }

            @Override
            public boolean hasAccessToWebResource(String resource, String... methods) {
                return false;
            }

            @Override
            public jakarta.security.enterprise.AuthenticationStatus authenticate(
                    jakarta.servlet.http.HttpServletRequest request,
                    jakarta.servlet.http.HttpServletResponse response,
                    jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters parameters
            ) {
                return jakarta.security.enterprise.AuthenticationStatus.NOT_DONE;
            }
        };
    }

    private static ZoneCompositeService unusedCompositeService() {
        return (ZoneCompositeService) Proxy.newProxyInstance(
                ZoneCompositeService.class.getClassLoader(),
                new Class<?>[]{ZoneCompositeService.class},
                (proxy, method, arguments) -> null
        );
    }
}
