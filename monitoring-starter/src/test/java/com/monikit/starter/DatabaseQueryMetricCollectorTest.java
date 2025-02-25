package com.monikit.starter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.monikit.core.DatabaseQueryLog;
import com.monikit.core.LogType;
import com.monikit.starter.config.MoniKitMetricsProperties;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class DatabaseQueryMetricCollectorTest {

    @Mock
    private MoniKitMetricsProperties metricsProperties;

    @Mock
    private SqlQueryCountMetricsBinder countMetricsBinder;

    @Mock
    private SqlQueryDurationMetricsBinder durationMetricsBinder;

    private DatabaseQueryMetricCollector collector;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        collector = new DatabaseQueryMetricCollector(metricsProperties, countMetricsBinder, durationMetricsBinder);
    }

    @Test
    @DisplayName("shouldSupportDatabaseQueryLogType")
    void shouldSupportDatabaseQueryLogType() {
        // When & Then
        assertTrue(collector.supports(LogType.DATABASE_QUERY));
    }

    @Nested
    @DisplayName("record() 메서드 테스트")
    class RecordMethodTests {

        @Test
        @DisplayName("shouldNotRecordMetricsWhenMetricsAreDisabled")
        void shouldNotRecordMetricsWhenMetricsAreDisabled() {
            // Given
            when(metricsProperties.isMetricsEnabled()).thenReturn(false);
            when(metricsProperties.isQueryMetricsEnabled()).thenReturn(false);
            DatabaseQueryLog logEntry = mock(DatabaseQueryLog.class);

            // When
            collector.record(logEntry);

            // Then
            verify(countMetricsBinder, never()).increment();
            verify(durationMetricsBinder, never()).record(anyLong());
        }

        @Test
        @DisplayName("shouldRecordMetricsWhenMetricsAreEnabled")
        void shouldRecordMetricsWhenMetricsAreEnabled() {
            // Given
            when(metricsProperties.isMetricsEnabled()).thenReturn(true);
            when(metricsProperties.isQueryMetricsEnabled()).thenReturn(true);

            DatabaseQueryLog logEntry = mock(DatabaseQueryLog.class);
            when(logEntry.getExecutionTime()).thenReturn(100L);

            // When
            collector.record(logEntry);

            // Then
            verify(countMetricsBinder, times(1)).increment();
            verify(durationMetricsBinder, times(1)).record(100L);
        }
    }
}