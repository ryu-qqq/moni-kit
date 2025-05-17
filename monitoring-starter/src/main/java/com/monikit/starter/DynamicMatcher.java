package com.monikit.starter;

import java.util.List;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.monikit.config.DynamicLogRule;

/**
 * 실행 중 joinPoint에 대해 동적으로 로깅 대상 여부를 판단하는 매처 클래스.
 * <p>
 * - MoniKitLoggingProperties에 정의된 DynamicLogRule을 기반으로 판단
 * - 허용된 base-package에 해당하지 않으면 자동으로 제외
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.2
 */
public class DynamicMatcher {
    private final List<DynamicLogRule> rules;
    private final List<String> allowedPackages;
    private final ExpressionParser parser = new SpelExpressionParser();

    public DynamicMatcher(List<DynamicLogRule> rules, List<String> allowedPackages) {
        this.rules = rules;
        this.allowedPackages = allowedPackages;
    }

    public Optional<DynamicLogRule> findMatchingRule(ProceedingJoinPoint joinPoint, long executionTime) {
        String fqcn = joinPoint.getTarget().getClass().getName();
        if (!isClassAllowed(fqcn)) return Optional.empty();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getMethod().getName();
        Object[] args = joinPoint.getArgs();

        for (DynamicLogRule rule : rules) {
            if (!className.matches(rule.getClassNamePattern())) continue;
            if (!methodName.matches(rule.getMethodNamePattern())) continue;
            if (executionTime < rule.getThresholdMillis()) continue;

            if (rule.getWhen() == null || rule.getWhen().isBlank()) return Optional.of(rule);

            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("executionTime", executionTime);
            context.setVariable("className", className);
            context.setVariable("methodName", methodName);
            context.setVariable("args", args);

            Boolean result = parser.parseExpression(rule.getWhen()).getValue(context, Boolean.class);
            if (Boolean.TRUE.equals(result)) return Optional.of(rule);
        }

        return Optional.empty();
    }

    private boolean isClassAllowed(String fullyQualifiedClassName) {
        if (allowedPackages == null || allowedPackages.isEmpty()) return true;
        return allowedPackages.stream().anyMatch(fullyQualifiedClassName::startsWith);
    }
}