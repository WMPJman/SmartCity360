package com.wmpj.smartcity360.core.dto;

import com.wmpj.smartcity360.core.entity.CityPolicyStatus;
import com.wmpj.smartcity360.core.entity.CityPolicyType;

import java.time.OffsetDateTime;

public record PolicySummary(
        long id,
        String policyCode,
        String name,
        String description,
        Long zoneId,
        CityPolicyType policyType,
        String expression,
        CityPolicyStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
