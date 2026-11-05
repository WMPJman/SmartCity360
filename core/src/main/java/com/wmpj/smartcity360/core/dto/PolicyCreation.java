package com.wmpj.smartcity360.core.dto;

import com.wmpj.smartcity360.core.entity.CityPolicyType;

public record PolicyCreation(
        String policyCode,
        String name,
        String description,
        Long zoneId,
        CityPolicyType policyType,
        String expression
) {
}
