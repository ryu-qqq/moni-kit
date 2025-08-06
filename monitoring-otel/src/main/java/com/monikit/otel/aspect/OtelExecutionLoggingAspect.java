package com.monikit.otel.aspect;

import com.monikit.config.DynamicLogRule;
import com.monikit.starter.DynamicMatcher;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * OpenTelemetry 기반 실행 로깅 Aspect.
 * <p>
 * 기존 {@link com.monikit.starter.ExecutionLoggingAspect}를 대체하여
 * 순수 OpenTelemetry 방식으로 실행 시간, 메트릭, 분산 추적을 처리합니다.
 * </p>
 * 
 * <h3>OpenTelemetry 우선 접근</h3>
 * <ul>
 *   <li>실행 시간 측정: Span duration으로 자동 처리</li>
 *   <li>메트릭 수집: OpenTelemetry Metrics로 표준화</li>
 *   <li>분산 추적: AWS X-Ray 네이티브 연동</li>
 *   <li>로그 상관관계: TraceId/SpanId 자동 주입</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2.0.0
 */
@Aspect
public class OtelExecutionLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(OtelExecutionLoggingAspect.class);

    // OpenTelemetry Semantic Conventions
    private static final AttributeKey<String> CODE_FUNCTION = AttributeKey.stringKey("code.function");
    private static final AttributeKey<String> CODE_NAMESPACE = AttributeKey.stringKey("code.namespace");
    private static final AttributeKey<Long> EXECUTION_TIME_MS = AttributeKey.longKey("monikit.execution.time_ms");
    private static final AttributeKey<Long> THRESHOLD_MS = AttributeKey.longKey("monikit.execution.threshold_ms");
    private static final AttributeKey<String> TAG = AttributeKey.stringKey("monikit.tag");
    private static final AttributeKey<String> ARGUMENTS = AttributeKey.stringKey("monikit.arguments");
    private static final AttributeKey<String> RESULT = AttributeKey.stringKey("monikit.result");
    private static final AttributeKey<Boolean> SLOW_EXECUTION = AttributeKey.booleanKey("monikit.slow_execution");

    private final Tracer tracer;
    private final DynamicMatcher matcher;

    public OtelExecutionLoggingAspect(Tracer tracer, DynamicMatcher matcher) {
        this.tracer = tracer;
        this.matcher = matcher;
    }

    @Pointcut("(@within(org.springframework.stereotype.Service) || " +
        "@within(org.springframework.stereotype.Repository) || " +
        "@within(org.springframework.stereotype.Controller) || " +
        "@within(com.monikit.core.LogExecutionTime) || " +
        "@annotation(com.monikit.core.LogExecutionTime)) " +
        "&& !within(com.monikit..*)")
    public void applicationBeansOrAnnotated() {}

    @Around("applicationBeansOrAnnotated()")
    public Object traceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String spanName = className + "." + methodName;

        // OpenTelemetry Span 생성
        Span span = tracer.spanBuilder(spanName)
            .setAttribute(CODE_NAMESPACE, signature.getDeclaringType().getName())
            .setAttribute(CODE_FUNCTION, methodName)
            .startSpan();

        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;

        try (Scope scope = span.makeCurrent()) {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            span.setStatus(StatusCode.ERROR, t.getMessage());
            span.recordException(t);
            throw t;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // 동적 매칭 규칙 확인
            Optional<DynamicLogRule> matchedRule = matcher.findMatchingRule(joinPoint, duration);
            
            if (matchedRule.isPresent()) {
                DynamicLogRule rule = matchedRule.get();
                
                // 🔥 OpenTelemetry Span에 정보 추가
                updateOpenTelemetrySpan(span, duration, rule, joinPoint, result);
            }
            
            span.end();
        }
    }

    /**
     * OpenTelemetry Span에 실행 정보를 추가합니다.
     */
    private void updateOpenTelemetrySpan(
        Span span, 
        long duration, 
        DynamicLogRule rule, 
        ProceedingJoinPoint joinPoint, 
        Object result
    ) {
        var attributesBuilder = Attributes.builder()
            .put(EXECUTION_TIME_MS, duration)
            .put(THRESHOLD_MS, rule.getThresholdMillis());
        
        if (!rule.getTag().isEmpty()) {
            attributesBuilder.put(TAG, rule.getTag());
        }
        
        // 인자와 결과를 안전하게 문자열로 변환
        try {
            String args = safeArgsToString(joinPoint.getArgs());
            if (!args.isEmpty()) {
                attributesBuilder.put(ARGUMENTS, args);
            }
        } catch (Exception e) {
            logger.debug("Failed to serialize arguments", e);
        }
        
        try {
            String resultStr = safeOutputToString(result);
            if (!resultStr.isEmpty()) {
                attributesBuilder.put(RESULT, resultStr);
            }
        } catch (Exception e) {
            logger.debug("Failed to serialize result", e);
        }
        
        // 실행 시간이 임계값을 초과한 경우 이벤트 추가
        if (duration > rule.getThresholdMillis()) {
            attributesBuilder.put(SLOW_EXECUTION, true);
        }
        
        span.setAllAttributes(attributesBuilder.build());
    }

    private String safeArgsToString(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            try {
                sb.append(args[i] != null ? args[i].toString() : "null");
            } catch (Exception e) {
                sb.append("[toString failed]");
            }
        }
        return sb.toString();
    }

    private String safeOutputToString(Object result) {
        if (result == null) {
            return "null";
        }
        
        try {
            return result.toString();
        } catch (Exception e) {
            return "[toString failed]";
        }
    }
}
