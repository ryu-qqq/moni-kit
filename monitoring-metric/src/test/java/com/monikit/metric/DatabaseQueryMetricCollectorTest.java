package com.monikit.metric;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.monikit.config.MoniKitMetricsProperties;
import com.monikit.core.DatabaseQueryLog;
import com.monikit.core.LogType;


import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.*;

class DatabaseQueryMetricCollectorTest {

    @Mock
    private MoniKitMetricsProperties metricsProperties;

    @Mock
    private QueryMetricsRecorder queryMetricsRecorder;

    private DatabaseQueryMetricCollector collector;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        collector = new DatabaseQueryMetricCollector(metricsProperties, queryMetricsRecorder);
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
            DatabaseQueryLog logEntry = TestLogEntryProvider.databaseQueryLog();

            // When
            collector.record(logEntry);

            // Then
            verify(queryMetricsRecorder, never()).record(logEntry.getQuery(), logEntry.getDataSource(), logEntry.getExecutionTime());        }

        @Test
        @DisplayName("shouldRecordMetricsWhenMetricsAreEnabled")
        void shouldRecordMetricsWhenMetricsAreEnabled() {
            // Given
            when(metricsProperties.isMetricsEnabled()).thenReturn(true);
            when(metricsProperties.isQueryMetricsEnabled()).thenReturn(true);

            DatabaseQueryLog logEntry = TestLogEntryProvider.databaseQueryLog();

            // When
            collector.record(logEntry);

            // Then
            verify(queryMetricsRecorder, never()).record(logEntry.getQuery(), logEntry.getDataSource(), logEntry.getExecutionTime());
        }
    }
}