package com.wmpj.smartcity360.core.streetlight.flyweight;

import com.wmpj.smartcity360.core.entity.StreetlightOperationalStatus;

import java.util.Objects;

public record StreetlightContext(
        long streetlightId,
        String lightCode,
        long zoneId,
        boolean on,
        short brightness,
        StreetlightOperationalStatus operationalStatus,
        LightingProfileFlyweight profile
) {

    public StreetlightContext {
        if (streetlightId <= 0L || zoneId <= 0L) {
            throw new IllegalArgumentException("Streetlight and zone ids must be positive");
        }

        Objects.requireNonNull(lightCode, "lightCode");
        Objects.requireNonNull(operationalStatus, "operationalStatus");
        Objects.requireNonNull(profile, "profile");

        if (lightCode.isBlank()) {
            throw new IllegalArgumentException("lightCode must not be blank");
        }

        if (brightness < 0 || brightness > 100) {
            throw new IllegalArgumentException("brightness must be between 0 and 100");
        }
    }
}
