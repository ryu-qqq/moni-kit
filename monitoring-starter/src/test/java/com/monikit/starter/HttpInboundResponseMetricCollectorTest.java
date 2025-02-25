package com.monikit.starter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.monikit.core.HttpInboundResponseLog;
import com.monikit.core.LogType;
import com.monikit.starter.config.MoniKitMetricsProperties;
import com.monikit.starter.utils.TestLogEntryProvider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class HttpInboundResponseMetricCollectorTest {

    private MoniKitMetricsProperties mockMetricsProperties;
    private HttpResponseMetricsRecorder mockHttpResponseMetricsRecorder;
    private HttpInboundResponseMetricCollector collector;

    @BeforeEach
    void setUp() {
        mockMetricsProperties = mock(MoniKitMetricsProperties.class);
        mockHttpResponseMetricsRecorder = mock(HttpResponseMetricsRecorder.class);
        collector = new HttpInboundResponseMetricCollector(mockMetricsProperties, mockHttpResponseMetricsRecorder);

    }

    @Nested
    @DisplayName("supports(LogType logType) 테스트")
    class SupportsMethod {

        @Test
        @DisplayName("should return true for LogType.INBOUND_RESPONSE")
        void shouldReturnTrueForInboundResponse() {
            assertTrue(collector.supports(LogType.INBOUND_RESPONSE));
        }

        @Test
        @DisplayName("should return false for other LogTypes")
        void shouldReturnFalseForOtherLogTypes() {
            assertFalse(collector.supports(LogType.OUTBOUND_REQUEST));
            assertFalse(collector.supports(LogType.DATABASE_QUERY));
        }
    }

    @Nested
    @DisplayName("record(HttpInboundResponseLog logEntry) 테스트")
    class RecordMethod {

        @Test
        @DisplayName("should not record metrics when metrics are disabled")
        void shouldNotRecordMetricsWhenMetricsDisabled() {
            // Given
            when(mockMetricsProperties.isMetricsEnabled()).thenReturn(false);

            HttpInboundResponseLog logEntry = TestLogEntryProvider.httpInboundResponseLog();

            // When
            collector.record(logEntry);

            // Then
            verifyNoInteractions(mockHttpResponseMetricsRecorder);
        }

        @Test
        @DisplayName("should record metrics when metrics are enabled")
        void shouldRecordMetricsWhenMetricsEnabled() {
            // Given
            when(mockMetricsProperties.isMetricsEnabled()).thenReturn(true);
            when(mockMetricsProperties.isHttpMetricsEnabled()).thenReturn(true);

            HttpInboundResponseLog logEntry = TestLogEntryProvider.httpInboundResponseLog();

            // When
            collector.record(logEntry);

            // Then
            verify(mockHttpResponseMetricsRecorder)
                .record(logEntry.getRequestUri(), logEntry.getStatusCode(), logEntry.getExecutionTime());

        }
    }
}
