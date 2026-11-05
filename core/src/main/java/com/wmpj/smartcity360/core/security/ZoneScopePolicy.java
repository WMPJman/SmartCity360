package com.wmpj.smartcity360.core.security;

import jakarta.ejb.EJBAccessException;

import java.util.Set;

public final class ZoneScopePolicy {

    private ZoneScopePolicy() {
    }

    public static void requireAccess(
            Set<String> roles,
            boolean assignedToTargetSubtree,
            boolean writeOperation
    ) {
        if (roles.contains(SecurityRoles.ADMIN)) {
            return;
        }

        boolean eligibleRole;

        if (writeOperation) {
            eligibleRole = roles.contains(SecurityRoles.CITY_MANAGER)
                    || roles.contains(SecurityRoles.SUPERVISOR);
        } else {
            eligibleRole = roles.contains(SecurityRoles.CITY_MANAGER)
                    || roles.contains(SecurityRoles.SUPERVISOR)
                    || roles.contains(SecurityRoles.LOCAL_STAFF);
        }

        if (!eligibleRole || !assignedToTargetSubtree) {
            throw new EJBAccessException("Access is denied");
        }
    }
}
