package com.wmpj.smartcity360.core.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public record PolicyEvaluationInput(
        Map<String, BigDecimal> numericValues,
        Map<String, Boolean> booleanValues
) {

    public PolicyEvaluationInput {
        numericValues = Map.copyOf(
                Objects.requireNonNull(numericValues, "numericValues")
        );
        booleanValues = Map.copyOf(
                Objects.requireNonNull(booleanValues, "booleanValues")
        );
    }
}
