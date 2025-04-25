package com.monikit.jdbc.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.monikit.jdbc.DataSourceProvider;
import com.monikit.core.model.DatabaseQueryLog;
import com.monikit.core.context.LogEntryContextManager;
import com.monikit.core.LogLevel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("DefaultQueryLoggingService 테스트")
class DefaultQueryLoggingServiceTest {

    private final LogEntryContextManager mockLogEntryContextManager = mock(LogEntryContextManager.class);
    private final DataSourceProvider mockDataSourceProvider = mock(DataSourceProvider.class);
    private final long slowQueryThresholdMs = 500;
    private final long criticalQueryThresholdMs = 2000;

    private final DefaultQueryLoggingService loggingService = new DefaultQueryLoggingService(
        mockLogEntryContextManager, mockDataSourceProvider, slowQueryThresholdMs, criticalQueryThresholdMs
    );

    @Nested
    @DisplayName("SQL 로그 기록 테스트")
    class LogQueryTests {

        @Test
        @DisplayName("logQuery()가 호출되면 로그 컨텍스트에 SQL 실행 정보가 저장되어야 한다.")
        void shouldStoreQueryLog() {
            // Given
            String traceId = "test-trace";
            String sql = "SELECT * FROM users WHERE id = ?";
            String parameter = "[1]";
            long executionTime = 450; // 500ms 이하 (Normal)
            int rowsAffected = 10;
            String dataSourceName = "mainDB";

            when(mockDataSourceProvider.getDataSourceName()).thenReturn(dataSourceName);

            // When
            loggingService.logQuery(traceId, sql, parameter, executionTime, rowsAffected);

            // Then
            ArgumentCaptor<DatabaseQueryLog> logCaptor = ArgumentCaptor.forClass(DatabaseQueryLog.class);
            verify(mockLogEntryContextManager).addLog(logCaptor.capture());

            DatabaseQueryLog capturedLog = logCaptor.getValue();
            assertEquals(traceId, capturedLog.getTraceId());
            assertEquals(sql, capturedLog.getQuery());
            assertEquals(parameter, capturedLog.getParameters());
            assertEquals(executionTime, capturedLog.getExecutionTime());
            assertEquals(dataSourceName, capturedLog.getDataSource());
            assertEquals(rowsAffected, capturedLog.getRowsAffected());
        }
    }

    @Nested
    @DisplayName("쿼리 성능 평가 테스트")
    class QueryPerformanceEvaluationTests {

        @Test
        @DisplayName("실행 시간이 임계값보다 낮으면 NORMAL 레벨로 기록해야 한다.")
        void shouldLogAsNormalWhenExecutionTimeIsBelowSlowThreshold() {
            // Given
            String traceId = "test-trace";
            String sql = "SELECT * FROM orders WHERE user_id = ?";
            String parameter = "[42]";
            long executionTime = 400; // 500ms 이하 (Normal)
            int rowsAffected = 2;
            String dataSourceName = "reportDB";

            when(mockDataSourceProvider.getDataSourceName()).thenReturn(dataSourceName);

            // When
            loggingService.logQuery(traceId, sql, parameter, executionTime, rowsAffected);

            // Then
            ArgumentCaptor<DatabaseQueryLog> logCaptor = ArgumentCaptor.forClass(DatabaseQueryLog.class);
            verify(mockLogEntryContextManager).addLog(logCaptor.capture());

            Assertions.assertEquals(LogLevel.INFO, logCaptor.getValue().getLogLevel());
        }

        @Test
        @DisplayName("실행 시간이 slowQueryThresholdMs 이상이면 SLOW 레벨로 기록해야 한다.")
        void shouldLogAsSlowWhenExecutionTimeExceedsSlowThreshold() {
            // Given
            String traceId = "test-trace";
            String sql = "UPDATE payments SET status = ? WHERE id = ?";
            String parameter = "[COMPLETED, 123]";
            long executionTime = 800; // 500ms 이상 (Slow)
            int rowsAffected = 1;
            String dataSourceName = "analyticsDB";

            when(mockDataSourceProvider.getDataSourceName()).thenReturn(dataSourceName);

            // When
            loggingService.logQuery(traceId, sql, parameter, executionTime, rowsAffected);

            // Then
            ArgumentCaptor<DatabaseQueryLog> logCaptor = ArgumentCaptor.forClass(DatabaseQueryLog.class);
            verify(mockLogEntryContextManager).addLog(logCaptor.capture());

            assertEquals(LogLevel.WARN, logCaptor.getValue().getLogLevel());
        }

        @Test
        @DisplayName("실행 시간이 criticalQueryThresholdMs 이상이면 CRITICAL 레벨로 기록해야 한다.")
        void shouldLogAsCriticalWhenExecutionTimeExceedsCriticalThreshold() {
            // Given
            String traceId = "test-trace";
            String sql = "DELETE FROM logs WHERE created_at < NOW() - INTERVAL 30 DAY";
            String parameter = "[]";
            long executionTime = 3000; // 2000ms 이상 (Critical)
            int rowsAffected = 100;
            String dataSourceName = "logDB";

            when(mockDataSourceProvider.getDataSourceName()).thenReturn(dataSourceName);

            // When
            loggingService.logQuery(traceId, sql, parameter, executionTime, rowsAffected);

            // Then
            ArgumentCaptor<DatabaseQueryLog> logCaptor = ArgumentCaptor.forClass(DatabaseQueryLog.class);
            verify(mockLogEntryContextManager).addLog(logCaptor.capture());

            assertEquals(LogLevel.ERROR, logCaptor.getValue().getLogLevel());
        }
    }
}
