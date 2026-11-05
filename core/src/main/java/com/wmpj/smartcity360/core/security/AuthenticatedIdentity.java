package com.wmpj.smartcity360.core.security;

import java.util.Objects;
import java.util.Set;

public record AuthenticatedIdentity(
        long userId,
        String displayName,
        String email,
        Set<String> roleCodes
) {

    public AuthenticatedIdentity {
        if (userId <= 0L) {
            throw new IllegalArgumentException("userId must be positive");
        }

        displayName = requireText(displayName, "displayName");
        email = requireText(email, "email");
        roleCodes = Set.copyOf(Objects.requireNonNull(roleCodes, "roleCodes"));
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);

        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }

        return value;
    }
}
