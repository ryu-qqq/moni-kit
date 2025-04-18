package com.monikit.starter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.core.ExceptionLog;
import com.monikit.core.ExecutionDetailLog;
import com.monikit.core.ExecutionLog;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.TraceIdProvider;

/**
 * @Controller, @Service, @Repository 어노테이션이 붙은 메서드의 실행 시간을 자동으로 로깅하는 AOP.
 * <p>
 * - 실행 시간이 설정된 임계값을 초과할 경우 상세 로그를 기록하고, 그렇지 않으면 요약 로그를 남깁니다.
 * - 모든 계층 (Web → Service → Repository)의 trace 흐름을 완성하기 위해 Controller도 포함합니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */
@Aspect
public class ExecutionLoggingAspect {

    private final LogEntryContextManager logEntryContextManager;
    private final MoniKitLoggingProperties loggingProperties;
    private final TraceIdProvider traceIdProvider;

    public ExecutionLoggingAspect(LogEntryContextManager logEntryContextManager,
                                  MoniKitLoggingProperties loggingProperties, TraceIdProvider traceIdProvider) {
        this.logEntryContextManager = logEntryContextManager;
        this.loggingProperties = loggingProperties;
        this.traceIdProvider = traceIdProvider;
    }

    @Pointcut("within(@org.springframework.stereotype.Controller *) || "
        + "within(@org.springframework.stereotype.Service *) || " +
        "within(@org.springframework.stereotype.Repository *)")
    public void controllerServiceRepositoryMethods() {}

    @Around("controllerServiceRepositoryMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        if (!loggingProperties.isLogEnabled()) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        String traceId = traceIdProvider.getTraceId();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String inputParams = safeArgsToString(joinPoint.getArgs());

        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            logEntryContextManager.addLog(ExceptionLog.create(traceId, e));
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            long threshold = loggingProperties.getThresholdMillis();
            String outputValue = safeOutputToString(result);

            if (executionTime > threshold) {
                logEntryContextManager.addLog(ExecutionDetailLog.create(
                    traceId, className, methodName, executionTime, inputParams, outputValue, threshold
                ));
            } else if (loggingProperties.isSummaryLogging()) {
                logEntryContextManager.addLog(ExecutionLog.create(
                    traceId, className, methodName, executionTime
                ));
            }
        }
    }

    private String safeArgsToString(Object[] args) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            sb.append("arg").append(i).append("=");
            try {
                sb.append(args[i]);
            } catch (Exception e) {
                sb.append("[unserializable:").append(args[i].getClass().getSimpleName()).append("]");
            }
            if (i < args.length - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }


    private String safeOutputToString(Object output) {
        if (output == null) return "null";
        try {
            return output.toString();
        } catch (Exception e) {
            return "[unserializable:" + output.getClass().getSimpleName() + "]";
        }
    }



}
