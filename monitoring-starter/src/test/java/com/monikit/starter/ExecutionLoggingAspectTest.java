package com.monikit.starter;

import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.core.*;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@DisplayName("ExecutionLoggingAspect 테스트")
@ExtendWith(MockitoExtension.class)
class ExecutionLoggingAspectTest {

    @Mock
    private TraceIdProvider traceIdProvider;

    @Mock
    private LogEntryContextManager mockLogEntryContextManager;

    @Mock
    private MoniKitLoggingProperties mockLoggingProperties;

    @InjectMocks
    private ExecutionLoggingAspect aspect;

    @Mock
    private ProceedingJoinPoint mockJoinPoint;

    @BeforeEach
    void setUp() {
        reset(mockLogEntryContextManager, mockLoggingProperties, traceIdProvider);
    }

    @Nested
    @DisplayName("AOP 작동 검증")
    class AspectInvocationTests {

        @Test
        @DisplayName("AOP가 메서드를 감싸고 실행 시간을 측정해야 한다.")
        void shouldInterceptMethodExecution() throws Throwable {
            // Given
            MethodSignature mockSignature = mock(MethodSignature.class);
            when(mockJoinPoint.getSignature()).thenReturn(mockSignature);
            when(mockSignature.getDeclaringTypeName()).thenReturn("com.monikit.service.TestService");
            when(mockSignature.getName()).thenReturn("testMethod");
            when(mockJoinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
            when(mockJoinPoint.proceed()).thenReturn("Mocked Result");

            doReturn(true).when(mockLoggingProperties).isLogEnabled();
            doReturn(false).when(mockLoggingProperties).isDetailedLogging();

            // When
            Object result = aspect.logExecutionTime(mockJoinPoint);

            // Then
            assertEquals("Mocked Result", result);
            verify(mockLogEntryContextManager, atLeastOnce()).addLog(any(ExecutionLog.class));
            verify(mockLogEntryContextManager, never()).addLog(any(ExecutionDetailLog.class));
        }

        @Test
        @DisplayName("detailedLogging=true일 때 ExecutionDetailLog가 저장되어야 한다.")
        void shouldLogExecutionDetailsWhenDetailedLoggingEnabled() throws Throwable {
            // Given
            MethodSignature mockSignature = mock(MethodSignature.class);
            when(mockJoinPoint.getSignature()).thenReturn(mockSignature);
            when(mockSignature.getDeclaringTypeName()).thenReturn("com.monikit.service.TestService");
            when(mockSignature.getName()).thenReturn("testMethod");
            when(mockJoinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
            when(mockJoinPoint.proceed()).thenReturn("Mocked Result");

            doReturn(true).when(mockLoggingProperties).isLogEnabled();
            doReturn(true).when(mockLoggingProperties).isDetailedLogging();

            // When
            Object result = aspect.logExecutionTime(mockJoinPoint);

            // Then
            assertEquals("Mocked Result", result);
            verify(mockLogEntryContextManager, atLeastOnce()).addLog(any(ExecutionDetailLog.class));
            verify(mockLogEntryContextManager, never()).addLog(any(ExecutionLog.class));
        }

        @Test
        @DisplayName("logEnabled=false일 때 로그를 남기지 않아야 한다.")
        void shouldNotLogWhenLoggingDisabled() throws Throwable {
            // Given
            doReturn(false).when(mockLoggingProperties).isLogEnabled();

            // When
            Object result = aspect.logExecutionTime(mockJoinPoint);

            // Then
            assertNull(result);
            verify(mockLogEntryContextManager, never()).addLog(any(LogEntry.class));
        }
    }
}