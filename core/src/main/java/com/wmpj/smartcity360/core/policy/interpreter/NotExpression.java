package com.wmpj.smartcity360.core.policy.interpreter;

import java.util.Objects;

public final class NotExpression implements PolicyExpression<Boolean> {

    private final PolicyExpression<Boolean> expression;

    public NotExpression(PolicyExpression<Boolean> expression) {
        this.expression = Objects.requireNonNull(expression, "expression");
    }

    @Override
    public Boolean evaluate(PolicyEvaluationContext context) {
        return !expression.evaluate(context);
    }
}
