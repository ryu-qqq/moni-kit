package com.monikit.starter;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.monikit.core.ExecutionDetailLog;
import com.monikit.core.ExecutionTimeLog;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogLevel;
import com.monikit.core.ThreadContextPropagator;
import com.monikit.core.TraceIdProvider;

/**
 * @Service 및 @Component 어노테이션이 붙은 메서드의 실행 시간을 자동으로 로깅하는 AOP.
 * <p>
 * - 메서드 실행 시간, 입력값, 출력값을 로깅함.
 * - 예외 발생 시에도 실행 시간을 로깅함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Aspect
@Component
public class ExecutionLoggingAspect {

    @Value("${logging.detailedLogging:false}")
    private boolean detailedLogging;

    /**
     * @Service 또는 @Component가 붙은 클래스의 모든 메서드 타겟
     */
    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceAndComponentMethods() {}

    /**
     * 서비스 및 컴포넌트 메서드의 실행 시간을 측정하고 로깅
     */
    @Around("serviceAndComponentMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String traceId = TraceIdProvider.currentTraceId();;
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String inputParams = Arrays.toString(joinPoint.getArgs());

        Object result = null;
        try {
            result = ThreadContextPropagator.runWithContext(joinPoint::proceed);
            return result;
        } catch (Exception e) {
            LogEntryContextManager.logException(traceId, e);
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            String outputValue = result != null ? result.toString() : "null";

            if (detailedLogging) {
                LogEntryContextManager.addLog(ExecutionDetailLog.create(
                    traceId, className, methodName, executionTime, inputParams, outputValue, LogLevel.DEBUG
                ));
            } else {
                LogEntryContextManager.addLog(ExecutionTimeLog.create(
                    traceId, LogLevel.INFO, className, methodName, executionTime
                ));
            }
        }
    }

}