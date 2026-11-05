package com.wmpj.smartcity360.core.policy.interpreter;

import java.util.Objects;

public final class BooleanValueExpression implements PolicyExpression<Boolean> {

    private final PolicyExpression<PolicyValue> expression;

    public BooleanValueExpression(PolicyExpression<PolicyValue> expression) {
        this.expression = Objects.requireNonNull(expression, "expression");
    }

    @Override
    public Boolean evaluate(PolicyEvaluationContext context) {
        return expression.evaluate(context).asBoolean();
    }
}
