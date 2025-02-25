package com.monikit.starter;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.monikit.core.ExceptionLog;
import com.monikit.core.ExecutionDetailLog;
import com.monikit.core.ExecutionTimeLog;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogLevel;
import com.monikit.starter.config.MoniKitLoggingProperties;

/**
 * @Service 및 @Repository 어노테이션이 붙은 메서드의 실행 시간을 자동으로 로깅하는 AOP.
 * <p>
 * - 메서드 실행 시간, 입력값, 출력값을 로깅함.
 * - 예외 발생 시에도 실행 시간을 로깅함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0.1
 */
@Aspect
@Component
public class ExecutionLoggingAspect {

    private final LogEntryContextManager logEntryContextManager;
    private final MoniKitLoggingProperties loggingProperties;

    public ExecutionLoggingAspect(LogEntryContextManager logEntryContextManager,
                                  MoniKitLoggingProperties loggingProperties) {
        this.logEntryContextManager = logEntryContextManager;
        this.loggingProperties = loggingProperties;
    }

    /**
     * @Service 또는 @Repository가 붙은 클래스의 모든 메서드 타겟.
     */
    @Pointcut("within(@org.springframework.stereotype.Service *) || within(@org.springframework.stereotype.Repository *)")
    public void serviceAndRepositoryMethods() {}

    /**
     * 서비스 및 리포지토리 메서드의 실행 시간을 측정하고 로깅.
     */
    @Around("serviceAndRepositoryMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!loggingProperties.isLogEnabled()) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        String traceId = TraceIdProvider.getTraceId();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String inputParams = Arrays.toString(joinPoint.getArgs());

        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            logEntryContextManager.addLog(ExceptionLog.create(traceId, e, ErrorCategoryClassifier.categorize(e)));
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            String outputValue = result != null ? result.toString() : "null";

            if (loggingProperties.isDetailedLogging()) {
                logEntryContextManager.addLog(ExecutionDetailLog.create(
                    traceId, className, methodName, executionTime, inputParams, outputValue, LogLevel.INFO
                ));
            } else {
                logEntryContextManager.addLog(ExecutionTimeLog.create(
                    traceId, LogLevel.INFO, className, methodName, executionTime
                ));
            }
        }
    }
}
