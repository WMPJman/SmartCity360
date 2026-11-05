package com.wmpj.smartcity360.core.security;

import jakarta.ejb.EJBAccessException;

import java.util.Set;

public final class StreetlightAccessPolicy {

    private StreetlightAccessPolicy() {
    }

    public static void requireRead(Set<String> roles, boolean assignedToZone) {
        requireOperationalRole(roles, assignedToZone, true);
    }

    public static void requireStatusControl(
            Set<String> roles,
            boolean assignedToZone
    ) {
        requireOperationalRole(roles, assignedToZone, true);
    }

    public static void requireProfileAssignment(
            Set<String> roles,
            boolean assignedToZone
    ) {
        requireOperationalRole(roles, assignedToZone, false);
    }

    private static void requireOperationalRole(
            Set<String> roles,
            boolean assignedToZone,
            boolean allowLocalStaff
    ) {
        if (roles.contains(SecurityRoles.ADMIN)) {
            return;
        }

        boolean permittedRole = roles.contains(SecurityRoles.CITY_MANAGER)
                || roles.contains(SecurityRoles.SUPERVISOR);

        if (allowLocalStaff && roles.contains(SecurityRoles.LOCAL_STAFF)) {
            permittedRole = true;
        }

        if (!permittedRole || !assignedToZone) {
            throw new EJBAccessException("Access is denied");
        }
    }
}
