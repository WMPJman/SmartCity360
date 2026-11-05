package com.wmpj.smartcity360.core.policy.interpreter;

import java.util.Map;

public enum PolicyVariable {
    NOISE_LEVEL("noiseLevel", PolicyValueType.NUMERIC),
    PARKING_OCCUPANCY("parkingOccupancy", PolicyValueType.NUMERIC),
    ZONE_POWER_ON("zonePowerOn", PolicyValueType.BOOLEAN),
    MAINTENANCE_MODE("maintenanceMode", PolicyValueType.BOOLEAN);

    private static final Map<String, PolicyVariable> BY_NAME = Map.of(
            NOISE_LEVEL.variableName,
            NOISE_LEVEL,
            PARKING_OCCUPANCY.variableName,
            PARKING_OCCUPANCY,
            ZONE_POWER_ON.variableName,
            ZONE_POWER_ON,
            MAINTENANCE_MODE.variableName,
            MAINTENANCE_MODE
    );

    private final String variableName;
    private final PolicyValueType valueType;

    PolicyVariable(String variableName, PolicyValueType valueType) {
        this.variableName = variableName;
        this.valueType = valueType;
    }

    public String variableName() {
        return variableName;
    }

    public PolicyValueType valueType() {
        return valueType;
    }

    public static PolicyVariable requireKnown(String variableName) {
        PolicyVariable variable = BY_NAME.get(variableName);

        if (variable == null) {
            throw new PolicyEvaluationException("Unknown policy variable");
        }

        return variable;
    }
}
