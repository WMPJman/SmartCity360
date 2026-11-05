package com.wmpj.smartcity360.core.policy.interpreter;

import java.util.Objects;

public final class LiteralExpression implements PolicyExpression<PolicyValue> {

    private final PolicyValue value;

    public LiteralExpression(PolicyValue value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    @Override
    public PolicyValue evaluate(PolicyEvaluationContext context) {
        return value;
    }
}
