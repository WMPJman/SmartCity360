package com.wmpj.smartcity360.core.policy.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PolicyTokenizer {

    public static final int MAXIMUM_EXPRESSION_LENGTH = 1_000;
    public static final int MAXIMUM_TOKENS = 200;

    public List<PolicyToken> tokenize(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new PolicySyntaxException("Policy expression is required");
        }

        if (expression.length() > MAXIMUM_EXPRESSION_LENGTH) {
            throw new PolicySyntaxException("Policy expression is too long");
        }

        List<PolicyToken> tokens = new ArrayList<>();
        int index = 0;

        while (index < expression.length()) {
            char current = expression.charAt(index);

            if (Character.isWhitespace(current)) {
                index++;
                continue;
            }

            if (Character.isLetter(current)) {
                int start = index;

                while (index < expression.length()
                        && Character.isLetterOrDigit(expression.charAt(index))) {
                    index++;
                }

                addToken(tokens, wordToken(expression.substring(start, index)));
                continue;
            }

            if (Character.isDigit(current)
                    || (current == '-' && nextIsDigit(expression, index))) {
                int start = index;
                index++;

                while (index < expression.length()
                        && (Character.isDigit(expression.charAt(index))
                        || expression.charAt(index) == '.')) {
                    index++;
                }

                addToken(
                        tokens,
                        new PolicyToken(
                                PolicyTokenType.NUMBER,
                                expression.substring(start, index)
                        )
                );
                continue;
            }

            PolicyToken operator = operatorToken(expression, index);

            if (operator == null) {
                throw new PolicySyntaxException("Policy expression contains an invalid token");
            }

            addToken(tokens, operator);
            index += operator.text().length();
        }

        tokens.add(new PolicyToken(PolicyTokenType.END, ""));
        return List.copyOf(tokens);
    }

    private static PolicyToken wordToken(String text) {
        String keyword = text.toUpperCase(Locale.ROOT);

        return switch (keyword) {
            case "AND" -> new PolicyToken(PolicyTokenType.AND, text);
            case "OR" -> new PolicyToken(PolicyTokenType.OR, text);
            case "NOT" -> new PolicyToken(PolicyTokenType.NOT, text);
            case "TRUE", "FALSE" -> new PolicyToken(PolicyTokenType.BOOLEAN, keyword);
            default -> new PolicyToken(PolicyTokenType.IDENTIFIER, text);
        };
    }

    private static PolicyToken operatorToken(String expression, int index) {
        String remainder = expression.substring(index);

        if (remainder.startsWith(">=")) {
            return new PolicyToken(PolicyTokenType.GREATER_THAN_OR_EQUAL, ">=");
        }

        if (remainder.startsWith("<=")) {
            return new PolicyToken(PolicyTokenType.LESS_THAN_OR_EQUAL, "<=");
        }

        if (remainder.startsWith("==")) {
            return new PolicyToken(PolicyTokenType.EQUAL, "==");
        }

        if (remainder.startsWith("!=")) {
            return new PolicyToken(PolicyTokenType.NOT_EQUAL, "!=");
        }

        char current = expression.charAt(index);

        return switch (current) {
            case '>' -> new PolicyToken(PolicyTokenType.GREATER_THAN, ">");
            case '<' -> new PolicyToken(PolicyTokenType.LESS_THAN, "<");
            case '(' -> new PolicyToken(PolicyTokenType.LEFT_PARENTHESIS, "(");
            case ')' -> new PolicyToken(PolicyTokenType.RIGHT_PARENTHESIS, ")");
            default -> null;
        };
    }

    private static boolean nextIsDigit(String expression, int index) {
        return index + 1 < expression.length()
                && Character.isDigit(expression.charAt(index + 1));
    }

    private static void addToken(List<PolicyToken> tokens, PolicyToken token) {
        if (tokens.size() >= MAXIMUM_TOKENS) {
            throw new PolicySyntaxException("Policy expression has too many tokens");
        }

        tokens.add(token);
    }
}
