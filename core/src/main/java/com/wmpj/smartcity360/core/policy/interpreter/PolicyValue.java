package com.wmpj.smartcity360.core.policy.interpreter;

import java.math.BigDecimal;
import java.util.Objects;

public final class PolicyValue {

    private final PolicyValueType type;
    private final BigDecimal numericValue;
    private final Boolean booleanValue;

    private PolicyValue(
            PolicyValueType type,
            BigDecimal numericValue,
            Boolean booleanValue
    ) {
        this.type = type;
        this.numericValue = numericValue;
        this.booleanValue = booleanValue;
    }

    public static PolicyValue numeric(BigDecimal value) {
        return new PolicyValue(
                PolicyValueType.NUMERIC,
                Objects.requireNonNull(value, "value"),
                null
        );
    }

    public static PolicyValue bool(boolean value) {
        return new PolicyValue(PolicyValueType.BOOLEAN, null, value);
    }

    public PolicyValueType type() {
        return type;
    }

    public BigDecimal asNumeric() {
        if (type != PolicyValueType.NUMERIC) {
            throw new PolicyEvaluationException("Numeric value is required");
        }

        return numericValue;
    }

    public boolean asBoolean() {
        if (type != PolicyValueType.BOOLEAN) {
            throw new PolicyEvaluationException("Boolean value is required");
        }

        return booleanValue;
    }
}
