package com.monikit.starter;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import com.monikit.config.DynamicLogRule;
import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.core.ExceptionLog;
import com.monikit.core.ExecutionDetailLog;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.TraceIdProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ExecutionLoggingAspect 전체 테스트")
class ExecutionLoggingAspectTest {

    @Test
    @DisplayName("규칙에 매칭되면 ExecutionDetailLog가 기록되어야 한다.")
    void shouldLogExecutionDetailWhenMatched() throws Throwable {
        // Given
        DynamicLogRule rule = new DynamicLogRule();
        rule.setClassNamePattern("TestService");
        rule.setMethodNamePattern("doSomething");
        rule.setWhen(null);
        rule.setThresholdMillis(100L);
        rule.setTag("unit");

        MoniKitLoggingProperties properties = mock(MoniKitLoggingProperties.class);
        when(properties.isLogEnabled()).thenReturn(true);
        when(properties.getDynamicMatching()).thenReturn(List.of(rule));
        when(properties.getAllowedPackages()).thenReturn(List.of("com.monikit"));

        LogEntryContextManager logManager = mock(LogEntryContextManager.class);
        TraceIdProvider traceIdProvider = mock(TraceIdProvider.class);
        when(traceIdProvider.getTraceId()).thenReturn("trace-123");

        ExecutionLoggingAspect aspect = new ExecutionLoggingAspect(logManager, properties, traceIdProvider);

        TestService target = new TestService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        TestService proxy = factory.getProxy();

        // When
        String result = proxy.doSomething("input");

        // Then
        ArgumentCaptor<ExecutionDetailLog> logCaptor = ArgumentCaptor.forClass(ExecutionDetailLog.class);
        verify(logManager).addLog(logCaptor.capture());

        ExecutionDetailLog log = logCaptor.getValue();
        assertEquals("trace-123", log.getTraceId());
        assertEquals("TestService", log.getClassName());
        assertEquals("doSomething", log.getMethodName());
        assertEquals("[arg0=input]", log.getInput());
        assertEquals("input_result", log.getOutput());
        assertEquals("unit", log.getTag());
    }

    @Test
    @DisplayName("예외 발생 시 ExceptionLog가 기록되어야 한다.")
    void shouldLogExceptionWhenErrorThrown() {
        // Given
        DynamicLogRule rule = new DynamicLogRule();
        rule.setClassNamePattern("ErrorService");
        rule.setMethodNamePattern("failMethod");
        rule.setWhen(null);
        rule.setThresholdMillis(100L);
        rule.setTag("error");

        MoniKitLoggingProperties properties = mock(MoniKitLoggingProperties.class);
        when(properties.isLogEnabled()).thenReturn(true);
        when(properties.getDynamicMatching()).thenReturn(List.of(rule));
        when(properties.getAllowedPackages()).thenReturn(List.of("com.monikit"));

        LogEntryContextManager logManager = mock(LogEntryContextManager.class);
        TraceIdProvider traceIdProvider = mock(TraceIdProvider.class);
        when(traceIdProvider.getTraceId()).thenReturn("trace-ex");

        ExecutionLoggingAspect aspect = new ExecutionLoggingAspect(logManager, properties, traceIdProvider);

        ErrorService target = new ErrorService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        ErrorService proxy = factory.getProxy();

        // When & Then
        RuntimeException thrown = assertThrows(RuntimeException.class, proxy::failMethod);
        assertEquals("boom", thrown.getMessage());

        ArgumentCaptor<ExceptionLog> exceptionCaptor = ArgumentCaptor.forClass(ExceptionLog.class);
        verify(logManager).addLog(exceptionCaptor.capture());

        ExceptionLog log = exceptionCaptor.getValue();
        assertEquals("trace-ex", log.getTraceId());
        assertTrue(log.getMessage().contains("boom"));
    }


    static class TestService {
        public String doSomething(String input) {
            return input + "_result";
        }
    }

    static class ErrorService {
        public String failMethod() {
            throw new RuntimeException("boom");
        }
    }
}