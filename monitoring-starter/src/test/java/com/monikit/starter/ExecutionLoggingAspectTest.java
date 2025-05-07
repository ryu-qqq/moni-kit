package com.monikit.starter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import com.monikit.config.DynamicLogRule;
import com.monikit.core.TraceIdProvider;
import com.monikit.core.context.LogEntryContextManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ExecutionLoggingAspect 전체 테스트")
class ExecutionLoggingAspectTest {

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