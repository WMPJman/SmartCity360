package com.wmpj.smartcity360.core.streetlight.flyweight;

import com.wmpj.smartcity360.core.entity.StreetlightType;

import java.math.BigDecimal;
import java.util.Objects;

public record LightingProfileDefinition(
        long profileId,
        String profileCode,
        String name,
        String manufacturer,
        String model,
        BigDecimal wattage,
        StreetlightType lightType,
        short defaultBrightness,
        String firmwareVersion
) {

    public LightingProfileDefinition {
        if (profileId <= 0L) {
            throw new IllegalArgumentException("profileId must be positive");
        }

        profileCode = requireText(profileCode, "profileCode");
        name = requireText(name, "name");
        wattage = Objects.requireNonNull(wattage, "wattage");
        lightType = Objects.requireNonNull(lightType, "lightType");

        if (wattage.signum() < 0) {
            throw new IllegalArgumentException("wattage must not be negative");
        }

        if (defaultBrightness < 0 || defaultBrightness > 100) {
            throw new IllegalArgumentException(
                    "defaultBrightness must be between 0 and 100"
            );
        }
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);

        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value;
    }
}
