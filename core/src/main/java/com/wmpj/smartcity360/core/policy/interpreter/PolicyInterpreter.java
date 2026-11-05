package com.wmpj.smartcity360.core.policy.interpreter;

import com.wmpj.smartcity360.core.entity.PolicyEvaluationResult;

public final class PolicyInterpreter {

    private final PolicyParser parser = new PolicyParser();

    public void validate(String expression) {
        parser.parse(expression);
    }

    public PolicyInterpretationResult evaluate(
            String expression,
            PolicyEvaluationContext context
    ) {
        try {
            PolicyExpression<Boolean> parsedExpression = parser.parse(expression);
            boolean matched = parsedExpression.evaluate(context);

            if (matched) {
                return new PolicyInterpretationResult(
                        PolicyEvaluationResult.MATCHED,
                        null
                );
            }

            return new PolicyInterpretationResult(
                    PolicyEvaluationResult.NOT_MATCHED,
                    null
            );
        } catch (PolicySyntaxException | PolicyEvaluationException exception) {
            return new PolicyInterpretationResult(
                    PolicyEvaluationResult.ERROR,
                    exception.getMessage()
            );
        }
    }
}
