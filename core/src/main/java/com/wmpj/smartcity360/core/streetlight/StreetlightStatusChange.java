package com.wmpj.smartcity360.core.streetlight;

import com.wmpj.smartcity360.core.entity.StreetlightOperationalStatus;

import java.util.Objects;

public record StreetlightStatusChange(
        boolean on,
        short brightness,
        StreetlightOperationalStatus operationalStatus
) {

    public StreetlightStatusChange {
        Objects.requireNonNull(operationalStatus, "operationalStatus");

        if (brightness < 0 || brightness > 100) {
            throw new IllegalArgumentException("brightness must be between 0 and 100");
        }
    }
}
