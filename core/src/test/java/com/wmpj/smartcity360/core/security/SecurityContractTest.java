package com.wmpj.smartcity360.core.security;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityContractTest {

    @Test
    void roleCatalogContainsOnlyTheFiveDatabaseRoleCodes() {
        assertEquals(
                Set.of("ADMIN", "CITY_MANAGER", "SUPERVISOR", "LOCAL_STAFF", "RESIDENT"),
                SecurityRoles.ALL
        );
    }

    @Test
    void authenticatedIdentityDefensivelyCopiesAssignedRoles() {
        Set<String> assignedRoles = new HashSet<>();
        assignedRoles.add(SecurityRoles.RESIDENT);

        AuthenticatedIdentity identity = new AuthenticatedIdentity(
                7L,
                "Test Resident",
                "resident@example.test",
                assignedRoles
        );
        assignedRoles.add(SecurityRoles.ADMIN);

        assertEquals(Set.of(SecurityRoles.RESIDENT), identity.roleCodes());
        assertThrows(
                UnsupportedOperationException.class,
                () -> identity.roleCodes().add(SecurityRoles.ADMIN)
        );
    }

    @Test
    void authenticatedIdentityTextDoesNotContainCredentialFields() {
        AuthenticatedIdentity identity = new AuthenticatedIdentity(
                7L,
                "Test Resident",
                "resident@example.test",
                Set.of(SecurityRoles.RESIDENT)
        );

        String text = identity.toString().toLowerCase();

        assertFalse(text.contains("password"));
        assertFalse(text.contains("hash"));
        assertFalse(text.contains("session"));
    }
}
