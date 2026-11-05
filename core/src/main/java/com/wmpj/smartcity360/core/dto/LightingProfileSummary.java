package com.wmpj.smartcity360.core.dto;

import com.wmpj.smartcity360.core.entity.StreetlightType;

import java.math.BigDecimal;

public record LightingProfileSummary(
        long id,
        String profileCode,
        String name,
        String manufacturer,
        String model,
        BigDecimal wattage,
        StreetlightType lightType,
        short defaultBrightness,
        String firmwareVersion
) {
}
