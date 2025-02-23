package com.monikit.starter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TraceIdProvider 테스트")
class TraceIdProviderTest {

    private static final String TEST_TRACE_ID = "test-trace-123";

    @AfterEach
    void tearDown() {
        // 테스트 후 MDC 정리
        TraceIdProvider.clear();
    }

    @Test
    @DisplayName("초기 상태에서 getTraceId()는 'N/A'를 반환해야 한다.")
    void shouldReturnNAWhenTraceIdNotSet() {
        // When
        String traceId = TraceIdProvider.getTraceId();

        // Then
        assertEquals("N/A", traceId, "Trace ID가 설정되지 않은 경우 'N/A'가 반환되어야 한다.");
    }

    @Test
    @DisplayName("setTraceId() 호출 후 getTraceId()는 설정한 값을 반환해야 한다.")
    void shouldReturnSetTraceId() {
        // Given
        TraceIdProvider.setTraceId(TEST_TRACE_ID);

        // When
        String traceId = TraceIdProvider.getTraceId();

        // Then
        assertEquals(TEST_TRACE_ID, traceId, "설정된 Trace ID를 올바르게 반환해야 한다.");
    }

    @Test
    @DisplayName("clear() 호출 후 getTraceId()는 'N/A'를 반환해야 한다.")
    void shouldReturnNAAfterClear() {
        // Given
        TraceIdProvider.setTraceId(TEST_TRACE_ID);
        TraceIdProvider.clear();

        // When
        String traceId = TraceIdProvider.getTraceId();

        // Then
        assertEquals("N/A", traceId, "clear() 호출 후 Trace ID는 'N/A'로 초기화되어야 한다.");
    }
}
