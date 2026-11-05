package com.wmpj.smartcity360.core.security;

import java.util.Set;

public final class SecurityRoles {

    public static final String ADMIN = "ADMIN";
    public static final String CITY_MANAGER = "CITY_MANAGER";
    public static final String SUPERVISOR = "SUPERVISOR";
    public static final String LOCAL_STAFF = "LOCAL_STAFF";
    public static final String RESIDENT = "RESIDENT";

    public static final Set<String> ALL = Set.of(
            ADMIN,
            CITY_MANAGER,
            SUPERVISOR,
            LOCAL_STAFF,
            RESIDENT
    );

    private SecurityRoles() {
    }
}
