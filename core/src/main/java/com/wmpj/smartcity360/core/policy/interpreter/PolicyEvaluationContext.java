package com.wmpj.smartcity360.core.policy.interpreter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class PolicyEvaluationContext {

    public static final int MAXIMUM_VALUES = 32;

    private final Map<String, PolicyValue> values;

    public PolicyEvaluationContext(Map<String, PolicyValue> values) {
        Objects.requireNonNull(values, "values");

        if (values.size() > MAXIMUM_VALUES) {
            throw new PolicyEvaluationException("Policy context is too large");
        }

        Map<String, PolicyValue> validatedValues = new HashMap<>();

        for (Map.Entry<String, PolicyValue> entry : values.entrySet()) {
            PolicyVariable variable = PolicyVariable.requireKnown(entry.getKey());
            PolicyValue value = Objects.requireNonNull(entry.getValue(), "context value");

            if (variable.valueType() != value.type()) {
                throw new PolicyEvaluationException("Policy context type is invalid");
            }

            validatedValues.put(entry.getKey(), value);
        }

        this.values = Map.copyOf(validatedValues);
    }

    public static PolicyEvaluationContext fromTypedValues(
            Map<String, BigDecimal> numericValues,
            Map<String, Boolean> booleanValues
    ) {
        Objects.requireNonNull(numericValues, "numericValues");
        Objects.requireNonNull(booleanValues, "booleanValues");
        Map<String, PolicyValue> values = new HashMap<>();

        for (Map.Entry<String, BigDecimal> entry : numericValues.entrySet()) {
            values.put(entry.getKey(), PolicyValue.numeric(entry.getValue()));
        }

        for (Map.Entry<String, Boolean> entry : booleanValues.entrySet()) {
            if (values.containsKey(entry.getKey())) {
                throw new PolicyEvaluationException("Policy variable is duplicated");
            }

            values.put(entry.getKey(), PolicyValue.bool(entry.getValue()));
        }

        return new PolicyEvaluationContext(values);
    }

    public PolicyValue requireValue(String variableName) {
        PolicyVariable.requireKnown(variableName);
        PolicyValue value = values.get(variableName);

        if (value == null) {
            throw new PolicyEvaluationException("Required policy variable is missing");
        }

        return value;
    }
}
