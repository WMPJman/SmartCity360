package com.wmpj.smartcity360.core.policy.interpreter;

import java.math.BigDecimal;
import java.util.List;

public final class PolicyParser {

    public static final int MAXIMUM_AST_DEPTH = 32;

    private final PolicyTokenizer tokenizer = new PolicyTokenizer();
    private List<PolicyToken> tokens;
    private int currentIndex;
    private int constructedNodes;

    public synchronized PolicyExpression<Boolean> parse(String expression) {
        tokens = tokenizer.tokenize(expression);
        currentIndex = 0;
        constructedNodes = 0;
        PolicyExpression<Boolean> parsed = parseOr(0);
        require(PolicyTokenType.END, "Unexpected content after policy expression");
        return parsed;
    }

    private PolicyExpression<Boolean> parseOr(int depth) {
        PolicyExpression<Boolean> expression = parseAnd(depth);

        while (match(PolicyTokenType.OR)) {
            requireDepth(depth);
            expression = new OrExpression(expression, parseAnd(depth + 1));
            recordNode();
        }

        return expression;
    }

    private PolicyExpression<Boolean> parseAnd(int depth) {
        PolicyExpression<Boolean> expression = parseNot(depth);

        while (match(PolicyTokenType.AND)) {
            requireDepth(depth);
            expression = new AndExpression(expression, parseNot(depth + 1));
            recordNode();
        }

        return expression;
    }

    private PolicyExpression<Boolean> parseNot(int depth) {
        requireDepth(depth);

        if (match(PolicyTokenType.NOT)) {
            recordNode();
            return new NotExpression(parseNot(depth + 1));
        }

        return parseBooleanPrimary(depth);
    }

    private PolicyExpression<Boolean> parseBooleanPrimary(int depth) {
        requireDepth(depth);

        if (match(PolicyTokenType.LEFT_PARENTHESIS)) {
            PolicyExpression<Boolean> expression = parseOr(depth + 1);
            require(
                    PolicyTokenType.RIGHT_PARENTHESIS,
                    "Closing parenthesis is required"
            );
            return expression;
        }

        PolicyExpression<PolicyValue> left = parseValue();
        ComparisonOperator operator = comparisonOperator();

        if (operator == null) {
            recordNode();
            return new BooleanValueExpression(left);
        }

        PolicyExpression<PolicyValue> right = parseValue();
        recordNode();
        return new ComparisonExpression(left, operator, right);
    }

    private PolicyExpression<PolicyValue> parseValue() {
        PolicyToken token = current();

        if (match(PolicyTokenType.IDENTIFIER)) {
            try {
                recordNode();
                return new VariableExpression(token.text());
            } catch (PolicyEvaluationException exception) {
                throw new PolicySyntaxException("Unknown policy variable");
            }
        }

        if (match(PolicyTokenType.NUMBER)) {
            try {
                recordNode();
                return new LiteralExpression(
                        PolicyValue.numeric(new BigDecimal(token.text()))
                );
            } catch (NumberFormatException exception) {
                throw new PolicySyntaxException("Numeric literal is invalid");
            }
        }

        if (match(PolicyTokenType.BOOLEAN)) {
            recordNode();
            return new LiteralExpression(
                    PolicyValue.bool(Boolean.parseBoolean(token.text()))
            );
        }

        throw new PolicySyntaxException("Policy value is required");
    }

    private ComparisonOperator comparisonOperator() {
        if (match(PolicyTokenType.EQUAL)) {
            return ComparisonOperator.EQUAL;
        }

        if (match(PolicyTokenType.NOT_EQUAL)) {
            return ComparisonOperator.NOT_EQUAL;
        }

        if (match(PolicyTokenType.GREATER_THAN)) {
            return ComparisonOperator.GREATER_THAN;
        }

        if (match(PolicyTokenType.GREATER_THAN_OR_EQUAL)) {
            return ComparisonOperator.GREATER_THAN_OR_EQUAL;
        }

        if (match(PolicyTokenType.LESS_THAN)) {
            return ComparisonOperator.LESS_THAN;
        }

        if (match(PolicyTokenType.LESS_THAN_OR_EQUAL)) {
            return ComparisonOperator.LESS_THAN_OR_EQUAL;
        }

        return null;
    }

    private boolean match(PolicyTokenType tokenType) {
        if (current().type() != tokenType) {
            return false;
        }

        currentIndex++;
        return true;
    }

    private void require(PolicyTokenType tokenType, String message) {
        if (!match(tokenType)) {
            throw new PolicySyntaxException(message);
        }
    }

    private PolicyToken current() {
        return tokens.get(currentIndex);
    }

    private static void requireDepth(int depth) {
        if (depth > MAXIMUM_AST_DEPTH) {
            throw new PolicySyntaxException("Policy expression is nested too deeply");
        }
    }

    private void recordNode() {
        constructedNodes++;

        if (constructedNodes > MAXIMUM_AST_DEPTH) {
            throw new PolicySyntaxException("Policy expression is too complex");
        }
    }
}
