package com.wmpj.smartcity360.core.policy.interpreter;

import java.util.Objects;

public final class ComparisonExpression implements PolicyExpression<Boolean> {

    private final PolicyExpression<PolicyValue> left;
    private final ComparisonOperator operator;
    private final PolicyExpression<PolicyValue> right;

    public ComparisonExpression(
            PolicyExpression<PolicyValue> left,
            ComparisonOperator operator,
            PolicyExpression<PolicyValue> right
    ) {
        this.left = Objects.requireNonNull(left, "left");
        this.operator = Objects.requireNonNull(operator, "operator");
        this.right = Objects.requireNonNull(right, "right");
    }

    @Override
    public Boolean evaluate(PolicyEvaluationContext context) {
        PolicyValue leftValue = left.evaluate(context);
        PolicyValue rightValue = right.evaluate(context);

        if (leftValue.type() != rightValue.type()) {
            throw new PolicyEvaluationException("Comparison types do not match");
        }

        if (leftValue.type() == PolicyValueType.BOOLEAN) {
            return compareBooleans(leftValue.asBoolean(), rightValue.asBoolean());
        }

        int comparison = leftValue.asNumeric().compareTo(rightValue.asNumeric());
        return compareNumericResult(comparison);
    }

    private boolean compareBooleans(boolean leftValue, boolean rightValue) {
        if (operator == ComparisonOperator.EQUAL) {
            return leftValue == rightValue;
        }

        if (operator == ComparisonOperator.NOT_EQUAL) {
            return leftValue != rightValue;
        }

        throw new PolicyEvaluationException("Boolean ordering is not supported");
    }

    private boolean compareNumericResult(int comparison) {
        return switch (operator) {
            case EQUAL -> comparison == 0;
            case NOT_EQUAL -> comparison != 0;
            case GREATER_THAN -> comparison > 0;
            case GREATER_THAN_OR_EQUAL -> comparison >= 0;
            case LESS_THAN -> comparison < 0;
            case LESS_THAN_OR_EQUAL -> comparison <= 0;
        };
    }
}
