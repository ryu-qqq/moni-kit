package com.monikit.starter;

import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.monikit.config.DynamicLogRule;
import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.core.model.ExceptionLog;
import com.monikit.core.model.ExecutionDetailLog;
import com.monikit.core.context.LogEntryContextManager;
import com.monikit.core.TraceIdProvider;
/**
 * 동적 로깅 규칙(DynamicLogRule)에 기반한 실행 시간 측정 AOP.
 * <p>
 * 설정 파일(application.yml)의 `monikit.logging.dynamic-matching` 목록을 기반으로,
 * 클래스 이름 / 메서드 이름 / 실행시간 / SpEL 조건 등을 평가하여 실행 시간을 자동 로깅합니다.
 * </p>
 *
 * <ul>
 *     <li>예외 발생 시 {@link ExceptionLog} 자동 기록</li>
 *     <li>조건 만족 시 {@link ExecutionDetailLog} 기록</li>
 *     <li>추적 ID는 {@link TraceIdProvider}를 통해 자동 생성</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 1.1.2
 */
@Aspect
public class ExecutionLoggingAspect {

    private final LogEntryContextManager logEntryContextManager;
    private final MoniKitLoggingProperties loggingProperties;
    private final TraceIdProvider traceIdProvider;
    private final DynamicMatcher matcher;

    public ExecutionLoggingAspect(LogEntryContextManager logEntryContextManager,
                                  MoniKitLoggingProperties loggingProperties,
                                  TraceIdProvider traceIdProvider) {
        this.logEntryContextManager = logEntryContextManager;
        this.loggingProperties = loggingProperties;
        this.traceIdProvider = traceIdProvider;
        this.matcher = new DynamicMatcher(loggingProperties.getDynamicMatching(), loggingProperties.getAllowedPackages());
    }

    @Pointcut("execution(* *(..))")
    public void allMethods() {}

    @Around("allMethods()")
    public Object logExecutionTimeIfMatched(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!loggingProperties.isLogEnabled()) {
            return joinPoint.proceed();
        }

        long start = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            long duration = System.currentTimeMillis() - start;

            Optional<DynamicLogRule> matchedRule = matcher.findMatchingRule(joinPoint, duration);
            if (matchedRule.isPresent()) {
                String traceId = traceIdProvider.getTraceId();
                String className = joinPoint.getTarget().getClass().getSimpleName();
                String methodName = joinPoint.getSignature().getName();

                if (error != null) {
                    logEntryContextManager.addLog(ExceptionLog.of(traceId, error));
                }

                logEntryContextManager.addLog(ExecutionDetailLog.of(
                    traceId,
                    className,
                    methodName,
                    duration,
                    ArgumentUtils.safeArgsToString(joinPoint.getArgs()),
                    ArgumentUtils.safeOutputToString(result),
                    matchedRule.get().getThresholdMillis(),
                    matchedRule.get().getTag()
                ));
            }
        }
    }

}