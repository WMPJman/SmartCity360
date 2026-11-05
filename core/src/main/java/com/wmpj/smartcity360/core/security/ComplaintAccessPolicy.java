package com.wmpj.smartcity360.core.security;

import jakarta.ejb.EJBAccessException;

import java.util.Set;

public final class ComplaintAccessPolicy {

    private ComplaintAccessPolicy() {
    }

    public static void requireViewAccess(
            Set<String> roles,
            boolean residentOwner,
            boolean operationalZoneScope
    ) {
        if (roles.contains(SecurityRoles.ADMIN)) {
            return;
        }

        if (roles.contains(SecurityRoles.RESIDENT) && residentOwner) {
            return;
        }

        requireOperationalAccess(roles, operationalZoneScope);
    }

    public static void requireOperationalAccess(
            Set<String> roles,
            boolean operationalZoneScope
    ) {
        if (roles.contains(SecurityRoles.ADMIN)) {
            return;
        }

        boolean operationalRole = roles.contains(SecurityRoles.CITY_MANAGER)
                || roles.contains(SecurityRoles.SUPERVISOR)
                || roles.contains(SecurityRoles.LOCAL_STAFF);

        if (!operationalRole || !operationalZoneScope) {
            throw new EJBAccessException("Access is denied");
        }
    }

    public static void requireResolutionAccess(
            Set<String> roles,
            boolean operationalZoneScope,
            boolean assignedActor
    ) {
        if (roles.contains(SecurityRoles.ADMIN)) {
            return;
        }

        requireOperationalAccess(roles, operationalZoneScope);

        if (!assignedActor) {
            throw new EJBAccessException("Access is denied");
        }
    }
}
