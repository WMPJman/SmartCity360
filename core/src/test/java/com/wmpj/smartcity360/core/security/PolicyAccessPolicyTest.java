package com.wmpj.smartcity360.core.security;

import jakarta.ejb.EJBAccessException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PolicyAccessPolicyTest {

    @Test
    void separatesManagementFromScopedEvaluation() {
        assertDoesNotThrow(
                () -> PolicyAccessPolicy.requireManagement(
                        Set.of(SecurityRoles.CITY_MANAGER),
                        true
                )
        );
        assertDoesNotThrow(
                () -> PolicyAccessPolicy.requireEvaluation(
                        Set.of(SecurityRoles.SUPERVISOR),
                        true
                )
        );
        assertThrows(
                EJBAccessException.class,
                () -> PolicyAccessPolicy.requireManagement(
                        Set.of(SecurityRoles.SUPERVISOR),
                        true
                )
        );
        assertThrows(
                EJBAccessException.class,
                () -> PolicyAccessPolicy.requireEvaluation(
                        Set.of(SecurityRoles.CITY_MANAGER),
                        false
                )
        );
        assertThrows(
                EJBAccessException.class,
                () -> PolicyAccessPolicy.requireEvaluation(
                        Set.of(SecurityRoles.RESIDENT),
                        true
                )
        );
    }
}
