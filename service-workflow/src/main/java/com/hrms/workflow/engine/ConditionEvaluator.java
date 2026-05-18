package com.hrms.workflow.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Evaluates Spring-EL boolean expressions like `#amount > 10000 && #type == 'TRAVEL'`
 * against the workflow context map. Expressions are cached to avoid re-parsing.
 */
@Slf4j
@Component
public class ConditionEvaluator {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final Map<String, Expression> cache = new ConcurrentHashMap<>();

    public boolean evaluate(String expression, Map<String, Object> context) {
        if (expression == null || expression.isBlank()) return true;
        try {
            Expression expr = cache.computeIfAbsent(expression, parser::parseExpression);
            Map<String, Object> root = Objects.requireNonNullElseGet(context, Map::of);
            EvaluationContext ctx = SimpleEvaluationContext.forReadOnlyDataBinding()
                    .withRootObject(root).build();
            Boolean v = expr.getValue(ctx, Boolean.class);
            return v != null && v;
        } catch (Exception e) {
            log.warn("SpEL '{}' failed against ctx {} — {}", expression, context, e.getMessage());
            return false;
        }
    }
}
