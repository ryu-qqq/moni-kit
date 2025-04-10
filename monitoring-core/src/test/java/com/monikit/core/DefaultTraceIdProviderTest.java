package com.monikit.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultTraceIdProvider")
class DefaultTraceIdProviderTest {

    @Test
    @DisplayName("getTraceId()는 UUID 형식의 ID를 반환하고 재사용한다")
    void shouldGenerateAndReuseTraceId() {
        DefaultTraceIdProvider provider = new DefaultTraceIdProvider();

        String traceId1 = provider.getTraceId();
        String traceId2 = provider.getTraceId();

        assertNotNull(traceId1);
        assertEquals(traceId1, traceId2);
    }

    @Test
    @DisplayName("setTraceId()로 지정된 ID를 반환한다")
    void shouldReturnSetTraceId() {
        DefaultTraceIdProvider provider = new DefaultTraceIdProvider();
        provider.setTraceId("test-trace-id");

        assertEquals("test-trace-id", provider.getTraceId());
    }

    @Test
    @DisplayName("clear() 호출 시 traceId는 새로 생성된다")
    void shouldClearAndGenerateNewId() {
        DefaultTraceIdProvider provider = new DefaultTraceIdProvider();

        String original = provider.getTraceId();
        provider.clear();
        String regenerated = provider.getTraceId();

        assertNotNull(regenerated);
        assertNotEquals(original, regenerated);
    }
}