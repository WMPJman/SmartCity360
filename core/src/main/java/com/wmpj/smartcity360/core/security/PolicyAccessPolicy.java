package com.wmpj.smartcity360.core.security;

import jakarta.ejb.EJBAccessException;

import java.util.Set;

public final class PolicyAccessPolicy {

    private PolicyAccessPolicy() {
    }

    public static void requireManagement(Set<String> roles, boolean assignedScope) {
        if (roles.contains(SecurityRoles.ADMIN)) {
            return;
        }

        if (!roles.contains(SecurityRoles.CITY_MANAGER) || !assignedScope) {
            throw new EJBAccessException("Access is denied");
        }
    }

    public static void requireEvaluation(Set<String> roles, boolean assignedScope) {
        if (roles.contains(SecurityRoles.ADMIN)) {
            return;
        }

        boolean permittedRole = roles.contains(SecurityRoles.CITY_MANAGER)
                || roles.contains(SecurityRoles.SUPERVISOR);

        if (!permittedRole || !assignedScope) {
            throw new EJBAccessException("Access is denied");
        }
    }
}
