package com.monikit.starter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;



class QueryMetricsRecorderTest {

    private SqlQueryCountMetricsBinder countMetricsBinder;
    private SqlQueryDurationMetricsBinder durationMetricsBinder;
    private QueryMetricsRecorder queryMetricsRecorder;

    @BeforeEach
    void setUp() {
        // Mocking the dependencies
        countMetricsBinder = mock(SqlQueryCountMetricsBinder.class);
        durationMetricsBinder = mock(SqlQueryDurationMetricsBinder.class);

        // Creating the recorder with mocked dependencies
        queryMetricsRecorder = new QueryMetricsRecorder(countMetricsBinder, durationMetricsBinder);
    }

    @Test
    @DisplayName("shouldRecordQueryMetrics")
    void shouldRecordQueryMetrics() {
        // Given
        String sql = "SELECT * FROM users WHERE id = ?";
        String dataSource = "primary_db";
        long executionTime = 150L;

        // When
        queryMetricsRecorder.record(sql, dataSource, executionTime);

        // Then
        // Verify that increment() was called on the countMetricsBinder
        verify(countMetricsBinder, times(1)).increment(sql, dataSource);

        // Verify that record() was called on the durationMetricsBinder
        verify(durationMetricsBinder, times(1)).record(sql, dataSource, executionTime);
    }

    @Test
    @DisplayName("shouldNotCallDurationMetricsBinderIfNoExecutionTime")
    void shouldNotCallDurationMetricsBinderIfNoExecutionTime() {
        // Given
        String sql = "SELECT * FROM users WHERE id = ?";
        String dataSource = "primary_db";
        long executionTime = 0L;

        // When
        queryMetricsRecorder.record(sql, dataSource, executionTime);

        // Then
        // Verify that increment() was called on the countMetricsBinder
        verify(countMetricsBinder, times(1)).increment(sql, dataSource);

        // Ensure that durationMetricsBinder record() is called, even for 0 execution time
        verify(durationMetricsBinder, times(1)).record(sql, dataSource, executionTime);
    }

    @Test
    @DisplayName("shouldHandleMultipleQueries")
    void shouldHandleMultipleQueries() {
        // Given
        String sql1 = "SELECT * FROM users WHERE id = ?";
        String dataSource1 = "primary_db";
        String sql2 = "UPDATE orders SET status = ? WHERE id = ?";
        String dataSource2 = "secondary_db";
        long executionTime1 = 100L;
        long executionTime2 = 200L;

        // When
        queryMetricsRecorder.record(sql1, dataSource1, executionTime1);
        queryMetricsRecorder.record(sql2, dataSource2, executionTime2);

        // Then
        // Verify that increment() was called on the countMetricsBinder for both queries
        verify(countMetricsBinder, times(1)).increment(sql1, dataSource1);
        verify(countMetricsBinder, times(1)).increment(sql2, dataSource2);

        // Verify that record() was called on the durationMetricsBinder for both queries
        verify(durationMetricsBinder, times(1)).record(sql1, dataSource1, executionTime1);
        verify(durationMetricsBinder, times(1)).record(sql2, dataSource2, executionTime2);
    }
}
