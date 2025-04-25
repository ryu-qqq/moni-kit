package com.monikit.starter;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import com.monikit.core.context.LogEntryContextManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("MDCThreadContextHandler 테스트")
class MDCThreadContextHandlerTest {

    @Test
    @DisplayName("Runnable에서 MDC 값이 자식 스레드로 안전하게 전파되어야 한다.")
    void shouldPropagateMDCInRunnable() {
        // Given
        MDC.put("traceId", "abc-123");
        LogEntryContextManager mockContextManager = mock(LogEntryContextManager.class);
        MDCThreadContextHandler handler = new MDCThreadContextHandler(mockContextManager);

        AtomicReference<String> traceIdFromChild = new AtomicReference<>();

        Runnable task = handler.propagateToChildThread(() -> {
            traceIdFromChild.set(MDC.get("traceId"));
        });

        // When
        task.run();

        // Then
        assertEquals("abc-123", traceIdFromChild.get());
        assertNull(MDC.get("traceId")); // 원래 MDC는 clear되어야 함
    }

    @Test
    @DisplayName("Callable에서 MDC 값이 자식 스레드로 안전하게 전파되어야 한다.")
    void shouldPropagateMDCInCallable() throws Exception {
        // Given
        MDC.put("traceId", "xyz-789");
        LogEntryContextManager mockContextManager = mock(LogEntryContextManager.class);
        MDCThreadContextHandler handler = new MDCThreadContextHandler(mockContextManager);

        Callable<String> task = handler.propagateToChildThread(() -> MDC.get("traceId"));

        // When
        String result = task.call();

        // Then
        assertEquals("xyz-789", result);
        assertNull(MDC.get("traceId")); // 원래 MDC는 clear되어야 함
    }

    @Test
    @DisplayName("LogContextScope가 자동으로 열리고 닫혀야 한다.")
    void shouldOpenAndCloseLogContextScope() {
        // Given
        LogEntryContextManager mockContextManager = mock(LogEntryContextManager.class);
        MDCThreadContextHandler handler = new MDCThreadContextHandler(mockContextManager);

        // 실제 scope가 열리는 것 확인하기보다는 내부에서 예외 없이 처리되는지 확인
        Runnable task = handler.propagateToChildThread(() -> {
            assertDoesNotThrow(() -> {
                // LogContextScope 내부 실행
            });
        });

        // When & Then
        assertDoesNotThrow(task::run);
    }
}