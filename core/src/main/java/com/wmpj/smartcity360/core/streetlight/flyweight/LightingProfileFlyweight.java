package com.wmpj.smartcity360.core.streetlight.flyweight;

import com.wmpj.smartcity360.core.entity.StreetlightType;

import java.math.BigDecimal;

public final class LightingProfileFlyweight {

    private final LightingProfileDefinition definition;

    LightingProfileFlyweight(LightingProfileDefinition definition) {
        this.definition = definition;
    }

    public long profileId() {
        return definition.profileId();
    }

    public String profileCode() {
        return definition.profileCode();
    }

    public String name() {
        return definition.name();
    }

    public String manufacturer() {
        return definition.manufacturer();
    }

    public String model() {
        return definition.model();
    }

    public BigDecimal wattage() {
        return definition.wattage();
    }

    public StreetlightType lightType() {
        return definition.lightType();
    }

    public short defaultBrightness() {
        return definition.defaultBrightness();
    }

    public String firmwareVersion() {
        return definition.firmwareVersion();
    }
}
