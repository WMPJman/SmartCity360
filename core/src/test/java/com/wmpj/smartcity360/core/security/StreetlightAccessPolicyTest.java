package com.wmpj.smartcity360.core.security;

import jakarta.ejb.EJBAccessException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StreetlightAccessPolicyTest {

    @Test
    void permitsScopedStaffAccordingToOperationSensitivity() {
        assertDoesNotThrow(
                () -> StreetlightAccessPolicy.requireRead(
                        Set.of(SecurityRoles.LOCAL_STAFF),
                        true
                )
        );
        assertDoesNotThrow(
                () -> StreetlightAccessPolicy.requireStatusControl(
                        Set.of(SecurityRoles.LOCAL_STAFF),
                        true
                )
        );
        assertDoesNotThrow(
                () -> StreetlightAccessPolicy.requireProfileAssignment(
                        Set.of(SecurityRoles.SUPERVISOR),
                        true
                )
        );
    }

    @Test
    void deniesResidentsOutOfScopeStaffAndLocalProfileAssignment() {
        assertThrows(
                EJBAccessException.class,
                () -> StreetlightAccessPolicy.requireRead(
                        Set.of(SecurityRoles.RESIDENT),
                        true
                )
        );
        assertThrows(
                EJBAccessException.class,
                () -> StreetlightAccessPolicy.requireStatusControl(
                        Set.of(SecurityRoles.SUPERVISOR),
                        false
                )
        );
        assertThrows(
                EJBAccessException.class,
                () -> StreetlightAccessPolicy.requireProfileAssignment(
                        Set.of(SecurityRoles.LOCAL_STAFF),
                        true
                )
        );
    }
}
