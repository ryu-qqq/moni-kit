package com.monikit.starter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.monikit.core.HttpOutboundResponseLog;
import com.monikit.core.LogType;
import com.monikit.starter.config.MoniKitMetricsProperties;
import com.monikit.starter.utils.TestLogEntryProvider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class HttpOutboundResponseMetricCollectorTest {

    private MoniKitMetricsProperties mockMetricsProperties;
    private HttpResponseMetricsRecorder mockHttpResponseMetricsRecorder;
    private HttpOutboundResponseMetricCollector collector;

    @BeforeEach
    void setUp() {
        mockMetricsProperties = mock(MoniKitMetricsProperties.class);
        mockHttpResponseMetricsRecorder = mock(HttpResponseMetricsRecorder.class);

        collector = new HttpOutboundResponseMetricCollector( mockMetricsProperties, mockHttpResponseMetricsRecorder);
    }

    @Nested
    @DisplayName("supports(LogType logType) 테스트")
    class SupportsMethod {

        @Test
        @DisplayName("should return true for LogType.OUTBOUND_RESPONSE")
        void shouldReturnTrueForOutboundResponse() {
            assertTrue(collector.supports(LogType.OUTBOUND_RESPONSE));
        }

        @Test
        @DisplayName("should return false for other LogTypes")
        void shouldReturnFalseForOtherLogTypes() {
            assertFalse(collector.supports(LogType.INBOUND_RESPONSE));
            assertFalse(collector.supports(LogType.DATABASE_QUERY));
        }
    }

    @Nested
    @DisplayName("record(HttpOutboundResponseLog logEntry) 테스트")
    class RecordMethod {

        @Test
        @DisplayName("should not record metrics when metrics are disabled")
        void shouldNotRecordMetricsWhenMetricsDisabled() {
            // Given
            when(mockMetricsProperties.isMetricsEnabled()).thenReturn(false);

            HttpOutboundResponseLog logEntry = TestLogEntryProvider.httpOutboundResponseLog();

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
            when(mockMetricsProperties.isExternalMallMetricsEnabled()).thenReturn(true);

            HttpOutboundResponseLog logEntry = TestLogEntryProvider.httpOutboundResponseLog();

            // When
            collector.record(logEntry);

            // Then
            verify(mockHttpResponseMetricsRecorder)
                .record("http_outbound_response_count", logEntry.getTargetUrl(), logEntry.getStatusCode(), logEntry.getExecutionTime());
        }
    }
}
