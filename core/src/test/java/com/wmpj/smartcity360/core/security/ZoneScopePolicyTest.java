package com.wmpj.smartcity360.core.security;

import jakarta.ejb.EJBAccessException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZoneScopePolicyTest {

    @Test
    void appliesRoleAndAssignmentBoundariesConservatively() {
        assertDoesNotThrow(
                () -> ZoneScopePolicy.requireAccess(
                        Set.of(SecurityRoles.ADMIN),
                        false,
                        true
                )
        );
        assertDoesNotThrow(
                () -> ZoneScopePolicy.requireAccess(
                        Set.of(SecurityRoles.SUPERVISOR),
                        true,
                        true
                )
        );
        assertThrows(
                EJBAccessException.class,
                () -> ZoneScopePolicy.requireAccess(
                        Set.of(SecurityRoles.SUPERVISOR),
                        false,
                        true
                )
        );
        assertDoesNotThrow(
                () -> ZoneScopePolicy.requireAccess(
                        Set.of(SecurityRoles.LOCAL_STAFF),
                        true,
                        false
                )
        );
        assertThrows(
                EJBAccessException.class,
                () -> ZoneScopePolicy.requireAccess(
                        Set.of(SecurityRoles.LOCAL_STAFF),
                        true,
                        true
                )
        );
        assertThrows(
                EJBAccessException.class,
                () -> ZoneScopePolicy.requireAccess(
                        Set.of(SecurityRoles.RESIDENT),
                        true,
                        false
                )
        );
    }
}
