package com.wmpj.smartcity360.core.policy.interpreter;

import com.wmpj.smartcity360.core.entity.PolicyEvaluationResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PolicyInterpreterTest {

    @Test
    void evaluatesTypedExpressionsWithPrecedenceAndParentheses() {
        PolicyInterpreter interpreter = new PolicyInterpreter();
        PolicyEvaluationContext context = context(
                Map.of(
                        "noiseLevel",
                        new BigDecimal("72.5"),
                        "parkingOccupancy",
                        new BigDecimal("45")
                ),
                Map.of("maintenanceMode", false, "zonePowerOn", true)
        );

        assertEquals(
                PolicyEvaluationResult.MATCHED,
                interpreter.evaluate(
                        "noiseLevel >= 70 AND NOT maintenanceMode",
                        context
                ).result()
        );
        assertEquals(
                PolicyEvaluationResult.NOT_MATCHED,
                interpreter.evaluate(
                        "noiseLevel > 80 OR parkingOccupancy > 90 AND zonePowerOn == true",
                        context
                ).result()
        );
        assertEquals(
                PolicyEvaluationResult.MATCHED,
                interpreter.evaluate(
                        "(noiseLevel > 80 OR parkingOccupancy < 50) AND zonePowerOn",
                        context
                ).result()
        );
    }

    @Test
    void buildsARealAstAndShortCircuitsLogicalOperations() {
        PolicyParser parser = new PolicyParser();
        PolicyExpression<Boolean> expression = parser.parse(
                "noiseLevel > 50 AND zonePowerOn"
        );
        AtomicInteger rightEvaluations = new AtomicInteger();
        PolicyExpression<Boolean> falseExpression = evaluationContext -> false;
        PolicyExpression<Boolean> countedExpression = evaluationContext -> {
            rightEvaluations.incrementAndGet();
            return true;
        };

        assertInstanceOf(AndExpression.class, expression);
        assertEquals(
                false,
                new AndExpression(falseExpression, countedExpression).evaluate(emptyContext())
        );
        assertEquals(0, rightEvaluations.get());
        assertEquals(
                true,
                new OrExpression(countedExpression, falseExpression).evaluate(emptyContext())
        );
        assertEquals(1, rightEvaluations.get());
    }

    @Test
    void distinguishesInvalidEvaluationFromFalse() {
        PolicyInterpreter interpreter = new PolicyInterpreter();

        assertEquals(
                PolicyEvaluationResult.ERROR,
                interpreter.evaluate(
                        "unknownVariable == true",
                        emptyContext()
                ).result()
        );
        assertEquals(
                PolicyEvaluationResult.ERROR,
                interpreter.evaluate(
                        "noiseLevel == true",
                        context(Map.of("noiseLevel", BigDecimal.ONE), Map.of())
                ).result()
        );
        assertEquals(
                PolicyEvaluationResult.ERROR,
                interpreter.evaluate("noiseLevel >", emptyContext()).result()
        );
    }

    @Test
    void enforcesExpressionContextAndNestingBounds() {
        PolicyParser parser = new PolicyParser();
        String oversized = "zonePowerOn".repeat(100);
        String deeplyNested = "(".repeat(40)
                + "zonePowerOn"
                + ")".repeat(40);
        String excessiveAst = "zonePowerOn AND ".repeat(40) + "zonePowerOn";

        assertThrows(PolicySyntaxException.class, () -> parser.parse(oversized));
        assertThrows(PolicySyntaxException.class, () -> parser.parse(deeplyNested));
        assertThrows(PolicySyntaxException.class, () -> parser.parse(excessiveAst));
        assertThrows(
                PolicyEvaluationException.class,
                () -> new PolicyEvaluationContext(
                        Map.of(
                                "noiseLevel",
                                PolicyValue.numeric(BigDecimal.ONE),
                                "unknownVariable",
                                PolicyValue.bool(true)
                        )
                )
        );
    }

    private static PolicyEvaluationContext context(
            Map<String, BigDecimal> numericValues,
            Map<String, Boolean> booleanValues
    ) {
        return PolicyEvaluationContext.fromTypedValues(numericValues, booleanValues);
    }

    private static PolicyEvaluationContext emptyContext() {
        return new PolicyEvaluationContext(Map.of());
    }
}
