package com.monikit.starter;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import com.monikit.config.DynamicLogRule;
import com.monikit.core.TraceIdProvider;
import com.monikit.core.context.LogEntryContextManager;
import com.monikit.core.model.ExecutionDetailLog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

        LogEntryContextManager logManager = mock(LogEntryContextManager.class);
        TraceIdProvider traceIdProvider = mock(TraceIdProvider.class);
        when(traceIdProvider.getTraceId()).thenReturn("trace-123");
        DynamicMatcher matcher = mock(DynamicMatcher.class);

        when(matcher.findMatchingRule(any(), anyLong())).thenReturn(Optional.of(rule));

        ExecutionLoggingAspect aspect = new ExecutionLoggingAspect(logManager, traceIdProvider, matcher);

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

        LogEntryContextManager logManager = mock(LogEntryContextManager.class);
        TraceIdProvider traceIdProvider = mock(TraceIdProvider.class);
        when(traceIdProvider.getTraceId()).thenReturn("trace-ex");
        DynamicMatcher matcher = mock(DynamicMatcher.class);

        ExecutionLoggingAspect aspect = new ExecutionLoggingAspect(logManager, traceIdProvider, matcher);

        ErrorService target = new ErrorService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        ErrorService proxy = factory.getProxy();

        // When & Then
        RuntimeException thrown = assertThrows(RuntimeException.class, proxy::failMethod);
        assertEquals("boom", thrown.getMessage());



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