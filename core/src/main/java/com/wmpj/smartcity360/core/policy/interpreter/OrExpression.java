package com.wmpj.smartcity360.core.policy.interpreter;

import java.util.Objects;

public final class OrExpression implements PolicyExpression<Boolean> {

    private final PolicyExpression<Boolean> left;
    private final PolicyExpression<Boolean> right;

    public OrExpression(
            PolicyExpression<Boolean> left,
            PolicyExpression<Boolean> right
    ) {
        this.left = Objects.requireNonNull(left, "left");
        this.right = Objects.requireNonNull(right, "right");
    }

    @Override
    public Boolean evaluate(PolicyEvaluationContext context) {
        if (left.evaluate(context)) {
            return true;
        }

        return right.evaluate(context);
    }
}
