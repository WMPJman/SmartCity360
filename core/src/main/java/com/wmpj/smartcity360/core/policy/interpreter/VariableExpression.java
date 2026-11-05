package com.wmpj.smartcity360.core.policy.interpreter;

public final class VariableExpression implements PolicyExpression<PolicyValue> {

    private final String variableName;

    public VariableExpression(String variableName) {
        PolicyVariable.requireKnown(variableName);
        this.variableName = variableName;
    }

    @Override
    public PolicyValue evaluate(PolicyEvaluationContext context) {
        return context.requireValue(variableName);
    }
}
