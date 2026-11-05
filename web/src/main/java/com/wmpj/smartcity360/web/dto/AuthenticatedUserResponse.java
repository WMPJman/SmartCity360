package com.wmpj.smartcity360.web.dto;

import com.wmpj.smartcity360.core.security.AuthenticatedIdentity;

import java.util.List;

public record AuthenticatedUserResponse(
        long userId,
        String displayName,
        String email,
        List<String> roleCodes
) {

    public static AuthenticatedUserResponse from(AuthenticatedIdentity identity) {
        return new AuthenticatedUserResponse(
                identity.userId(),
                identity.displayName(),
                identity.email(),
                identity.roleCodes().stream().sorted().toList()
        );
    }
}
