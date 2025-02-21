package com.monikit.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.monikit.core.utils.TestLogEntryProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DefaultLogEntryContextManager 테스트")
class DefaultLogEntryContextManagerTest {

    private DefaultLogEntryContextManager logEntryContextManager;

    @BeforeEach
    void setup() {
        LogEntryContext.clear();
        LogEntryContext.setErrorOccurred(false);
        logEntryContextManager = new DefaultLogEntryContextManager(DefaultLogNotifier.getInstance(), DefaultErrorLogNotifier.getInstance());
    }

    @Nested
    @DisplayName("로그 추가 테스트")
    class AddLogTests {

        @Test
        @DisplayName("로그를 추가하면 정상적으로 조회 가능해야 한다")
        void shouldAddLogThenBeRetrievable() {
            // Given
            LogEntry log = TestLogEntryProvider.executionTimeLog();

            // When
            logEntryContextManager.addLog(log);

            // Then
            assertEquals(1, LogEntryContext.size());
            assertTrue(LogEntryContext.getLogs().contains(log));
        }

        @Test
        @DisplayName("로그가 MAX_LOG_SIZE를 초과하면 자동으로 flush 되어야 한다")
        void shouldFlushAutomaticallyWhenMaxLogSizeExceeded() {
            // Given
            for (int i = 0; i < 310; i++) {
                logEntryContextManager.addLog(TestLogEntryProvider.executionTimeLog());
            }

            // When & Then
            assertTrue(LogEntryContext.size() <= 300);
        }
    }

    @Nested
    @DisplayName("로그 삭제 테스트")
    class ClearLogTests {

        @Test
        @DisplayName("flush 호출 시 모든 로그가 삭제되어야 한다")
        void shouldFlushLogsThenClearAll() {
            // Given
            logEntryContextManager.addLog(TestLogEntryProvider.executionTimeLog());
            logEntryContextManager.addLog(TestLogEntryProvider.databaseQueryLog());

            // When
            logEntryContextManager.flush();

            // Then
            assertEquals(0, LogEntryContext.size());
        }

        @Test
        @DisplayName("clear 호출 시 모든 로그와 에러 상태가 초기화되어야 한다")
        void shouldClearAllLogsAndResetErrorState() {
            // Given
            logEntryContextManager.addLog(TestLogEntryProvider.executionTimeLog());
            logEntryContextManager.addLog(TestLogEntryProvider.databaseQueryLog());
            LogEntryContext.setErrorOccurred(true);

            assertEquals(2, LogEntryContext.size());
            assertTrue(LogEntryContext.hasError());

            // When
            logEntryContextManager.clear();

            // Then
            assertEquals(0, LogEntryContext.size());
            assertFalse(LogEntryContext.hasError());
        }
    }
}