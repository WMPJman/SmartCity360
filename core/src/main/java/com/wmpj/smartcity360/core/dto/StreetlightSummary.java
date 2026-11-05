package com.wmpj.smartcity360.core.dto;

import com.wmpj.smartcity360.core.entity.StreetlightOperationalStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record StreetlightSummary(
        long id,
        String lightCode,
        long zoneId,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean on,
        short brightness,
        StreetlightOperationalStatus operationalStatus,
        long profileId,
        String profileCode,
        OffsetDateTime updatedAt
) {
}
