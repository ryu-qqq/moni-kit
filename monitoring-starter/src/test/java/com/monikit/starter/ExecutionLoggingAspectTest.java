package com.monikit.starter;

import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.core.LogEntry;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.TraceIdProvider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ExecutionLoggingAspectTest {

    private LogEntryContextManager contextManager;
    private MoniKitLoggingProperties properties;
    private TraceIdProvider traceIdProvider;
    private ExecutionLoggingAspect aspect;

    @BeforeEach
    void setup() {
        contextManager = mock(LogEntryContextManager.class);
        properties = new MoniKitLoggingProperties();
        properties.setLogEnabled(true);
        properties.setThresholdMillis(300);
        traceIdProvider = mock(TraceIdProvider.class);
        when(traceIdProvider.getTraceId()).thenReturn("test-trace-id");

        aspect = new ExecutionLoggingAspect(contextManager, properties, traceIdProvider);
    }

    @Test
    @DisplayName("ExecutionLoggingAspect - threshold 이하일 때 요약 로그")
    void shouldLogExecutionSummaryWhenUnderThreshold() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.Service");
        when(signature.getName()).thenReturn("doSomething");
        when(pjp.getArgs()).thenReturn(new Object[]{"test"});
        when(pjp.proceed()).then(invocation -> {
            Thread.sleep(10); // simulate quick method
            return "ok";
        });

        aspect.logExecutionTime(pjp);

        verify(contextManager, atLeastOnce()).addLog(any(LogEntry.class));
    }

    @Test
    @DisplayName("ExecutionLoggingAspect - threshold 초과 시 상세 로그")
    void shouldLogExecutionDetailWhenOverThreshold() throws Throwable {
        properties.setThresholdMillis(5); // 낮게 설정해서 무조건 초과

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.example.Service");
        when(signature.getName()).thenReturn("doSomething");
        when(pjp.getArgs()).thenReturn(new Object[]{"test"});
        when(pjp.proceed()).then(invocation -> {
            Thread.sleep(50); // simulate slow method
            return "done";
        });

        aspect.logExecutionTime(pjp);

        verify(contextManager, atLeastOnce()).addLog(any(LogEntry.class));
    }
}
