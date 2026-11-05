package com.wmpj.smartcity360.web.resource;

import com.wmpj.smartcity360.core.energy.EnergyProcessingResult;
import com.wmpj.smartcity360.core.energy.EnergyStreamLayer;
import com.wmpj.smartcity360.core.exception.UnauthenticatedException;
import com.wmpj.smartcity360.core.service.EnergyProcessingService;
import com.wmpj.smartcity360.web.dto.EnergyProcessRequest;
import com.wmpj.smartcity360.web.dto.EnergyProcessResponse;
import com.wmpj.smartcity360.web.security.BusinessApiSecurity;
import com.wmpj.smartcity360.web.security.InvalidRequestException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnergyResourceTest {

    @Test
    void rejectsAnonymousCallersAndInvalidRequests() {
        EnergyProcessingService service = (meterCode, payload, operation) ->
                new EnergyProcessingResult(payload, List.of(EnergyStreamLayer.COMPRESSION));
        EnergyResource anonymousResource = new EnergyResource(
                service,
                new BusinessApiSecurity(context(null, Set.of()))
        );
        EnergyProcessRequest validRequest = request("METER-001", "ENCODE", "reading");

        assertThrows(
                UnauthenticatedException.class,
                () -> anonymousResource.process(validRequest)
        );

        EnergyResource adminResource = new EnergyResource(
                service,
                new BusinessApiSecurity(context("admin", Set.of("ADMIN")))
        );
        EnergyProcessRequest invalidRequest = request("invalid meter!", "ENCODE", "reading");

        assertThrows(
                InvalidRequestException.class,
                () -> adminResource.process(invalidRequest)
        );
    }

    @Test
    void returnsOnlySafeEncodedProcessingMetadata() {
        EnergyProcessingService service = (meterCode, payload, operation) ->
                new EnergyProcessingResult(payload, List.of(EnergyStreamLayer.COMPRESSION));
        EnergyResource resource = new EnergyResource(
                service,
                new BusinessApiSecurity(context("admin", Set.of("ADMIN")))
        );

        EnergyProcessResponse response = resource.process(
                request("METER-001", "ENCODE", "reading")
        );

        assertEquals(
                "reading",
                new String(
                        Base64.getDecoder().decode(response.payloadBase64()),
                        StandardCharsets.UTF_8
                )
        );
        assertEquals(List.of(EnergyStreamLayer.COMPRESSION), response.effectiveOrder());
    }

    private static EnergyProcessRequest request(
            String meterCode,
            String operation,
            String payload
    ) {
        EnergyProcessRequest request = new EnergyProcessRequest();
        request.setMeterCode(meterCode);
        request.setOperation(operation);
        request.setPayloadBase64(
                Base64.getEncoder().encodeToString(
                        payload.getBytes(StandardCharsets.UTF_8)
                )
        );
        return request;
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
