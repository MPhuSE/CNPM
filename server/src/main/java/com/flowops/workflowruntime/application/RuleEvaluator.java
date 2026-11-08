package com.flowops.workflowruntime.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class RuleEvaluator {
    public RuleEvaluator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    private final ObjectMapper objectMapper;

    public boolean evaluate(String conditionJson, String formDataJson) {
        if (conditionJson == null || conditionJson.isBlank() || "{}".equals(conditionJson.trim())) {
            return true; // Default unconditional transition
        }

        try {
            Map<String, Object> condition = objectMapper.readValue(conditionJson, new TypeReference<>() {});
            Map<String, Object> formData = formDataJson != null && !formDataJson.isBlank()
                    ? objectMapper.readValue(formDataJson, new TypeReference<>() {})
                    : Map.of();

            return evaluateCondition(condition, formData);
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean evaluateCondition(Map<String, Object> condition, Map<String, Object> formData) {
        if (condition.isEmpty()) return true;

        // Check compound 'all' (AND)
        if (condition.containsKey("all")) {
            List<Map<String, Object>> subConditions = (List<Map<String, Object>>) condition.get("all");
            return subConditions.stream().allMatch(c -> evaluateCondition(c, formData));
        }

        // Check compound 'any' (OR)
        if (condition.containsKey("any")) {
            List<Map<String, Object>> subConditions = (List<Map<String, Object>>) condition.get("any");
            return subConditions.stream().anyMatch(c -> evaluateCondition(c, formData));
        }

        // Single predicate: field, op, value
        String field = (String) condition.get("field");
        String op = (String) condition.get("op");
        Object targetValue = condition.get("value");

        if (field == null || op == null) return true;

        Object actualValue = formData.get(field);
        if (actualValue == null) return false;

        return compareValues(actualValue, op, targetValue);
    }

    private boolean compareValues(Object actual, String op, Object target) {
        // Try numeric / monetary comparison
        try {
            BigDecimal actualNum = new BigDecimal(actual.toString().trim());
            BigDecimal targetNum = new BigDecimal(target.toString().trim());
            int cmp = actualNum.compareTo(targetNum);

            return switch (op) {
                case "EQ" -> cmp == 0;
                case "NE" -> cmp != 0;
                case "GT" -> cmp > 0;
                case "GTE" -> cmp >= 0;
                case "LT" -> cmp < 0;
                case "LTE" -> cmp <= 0;
                default -> false;
            };
        } catch (NumberFormatException ignored) {
            // Fallback to string comparison
            String actualStr = actual.toString();
            String targetStr = target != null ? target.toString() : null;

            return switch (op) {
                case "EQ" -> Objects.equals(actualStr, targetStr);
                case "NE" -> !Objects.equals(actualStr, targetStr);
                default -> false;
            };
        }
    }

}
