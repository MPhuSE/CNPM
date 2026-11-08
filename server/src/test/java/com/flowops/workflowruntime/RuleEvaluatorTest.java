package com.flowops.workflowruntime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowops.workflowruntime.application.RuleEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleEvaluatorTest {

    private RuleEvaluator ruleEvaluator;

    @BeforeEach
    void setUp() {
        ruleEvaluator = new RuleEvaluator(new ObjectMapper());
    }

    @Test
    @DisplayName("Empty condition {} evaluates to TRUE (unconditional)")
    void testEmptyConditionEvaluatesToTrue() {
        assertTrue(ruleEvaluator.evaluate("{}", "{\"amount\": 5000000}"));
        assertTrue(ruleEvaluator.evaluate("", "{}"));
        assertTrue(ruleEvaluator.evaluate(null, "{}"));
    }

    @Test
    @DisplayName("GTE comparison on VND string amounts")
    void testGteComparisonOnAmounts() {
        String condition = "{\"field\": \"amount\", \"op\": \"GTE\", \"value\": \"10000000\"}";

        // Less than 10M -> false
        assertFalse(ruleEvaluator.evaluate(condition, "{\"amount\": \"9999999\"}"));

        // Exactly 10M -> true
        assertTrue(ruleEvaluator.evaluate(condition, "{\"amount\": \"10000000\"}"));

        // Greater than 10M -> true
        assertTrue(ruleEvaluator.evaluate(condition, "{\"amount\": \"10000001\"}"));
    }

    @Test
    @DisplayName("EQ comparison on text field")
    void testEqComparisonOnText() {
        String condition = "{\"field\": \"accessLevel\", \"op\": \"EQ\", \"value\": \"ADMIN\"}";

        assertTrue(ruleEvaluator.evaluate(condition, "{\"accessLevel\": \"ADMIN\"}"));
        assertFalse(ruleEvaluator.evaluate(condition, "{\"accessLevel\": \"USER\"}"));
    }

    @Test
    @DisplayName("Compound ALL (AND) conditions")
    void testCompoundAllConditions() {
        String condition = """
                {
                  "all": [
                    {"field": "amount", "op": "GTE", "value": "10000000"},
                    {"field": "item", "op": "EQ", "value": "MacBook"}
                  ]
                }
                """;

        assertTrue(ruleEvaluator.evaluate(condition, "{\"amount\": \"15000000\", \"item\": \"MacBook\"}"));
        assertFalse(ruleEvaluator.evaluate(condition, "{\"amount\": \"5000000\", \"item\": \"MacBook\"}"));
        assertFalse(ruleEvaluator.evaluate(condition, "{\"amount\": \"15000000\", \"item\": \"ThinkPad\"}"));
    }
}
