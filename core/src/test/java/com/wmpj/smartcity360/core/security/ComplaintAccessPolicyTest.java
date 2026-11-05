package com.wmpj.smartcity360.core.security;

import jakarta.ejb.EJBAccessException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComplaintAccessPolicyTest {

    @Test
    void permitsOwnerAndScopedOperationalUsers() {
        assertDoesNotThrow(
                () -> ComplaintAccessPolicy.requireViewAccess(
                        Set.of(SecurityRoles.RESIDENT),
                        true,
                        false
                )
        );
        assertDoesNotThrow(
                () -> ComplaintAccessPolicy.requireOperationalAccess(
                        Set.of(SecurityRoles.LOCAL_STAFF),
                        true
                )
        );
        assertDoesNotThrow(
                () -> ComplaintAccessPolicy.requireResolutionAccess(
                        Set.of(SecurityRoles.SUPERVISOR),
                        true,
                        true
                )
        );
    }

    @Test
    void rejectsNonOwnerOutOfScopeAndUnassignedUsers() {
        assertThrows(
                EJBAccessException.class,
                () -> ComplaintAccessPolicy.requireViewAccess(
                        Set.of(SecurityRoles.RESIDENT),
                        false,
                        false
                )
        );
        assertThrows(
                EJBAccessException.class,
                () -> ComplaintAccessPolicy.requireOperationalAccess(
                        Set.of(SecurityRoles.LOCAL_STAFF),
                        false
                )
        );
        assertThrows(
                EJBAccessException.class,
                () -> ComplaintAccessPolicy.requireResolutionAccess(
                        Set.of(SecurityRoles.SUPERVISOR),
                        true,
                        false
                )
        );
    }
}
