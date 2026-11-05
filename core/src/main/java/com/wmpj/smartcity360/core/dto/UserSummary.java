package com.wmpj.smartcity360.core.dto;

import com.wmpj.smartcity360.core.entity.UserStatus;

import java.time.OffsetDateTime;

public record UserSummary(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        UserStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime lastLoginAt
) {
}
