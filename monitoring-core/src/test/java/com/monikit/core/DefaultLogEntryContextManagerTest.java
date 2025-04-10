package com.monikit.core;

import java.util.List;

import com.monikit.core.utils.TestLogEntryProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("DefaultLogEntryContextManager 테스트")
class DefaultLogEntryContextManagerTest {

    private DefaultLogEntryContextManager logEntryContextManager;
    private LogNotifier mockLogNotifier;
    private LogAddHook mockLogAddHook;
    private LogFlushHook mockLogFlushHook;

    @BeforeEach
    void setup() {
        LogEntryContext.clear();
        LogEntryContext.setErrorOccurred(false);
        mockLogNotifier = mock(LogNotifier.class);
        mockLogAddHook = mock(LogAddHook.class);
        mockLogFlushHook = mock(LogFlushHook.class);

        logEntryContextManager = new DefaultLogEntryContextManager(mockLogNotifier,
            List.of(mockLogAddHook), List.of(mockLogFlushHook));
    }

    @Nested
    @DisplayName("로그 추가 테스트")
    class AddLogTests {

        @Test
        @DisplayName("로그를 추가하면 정상적으로 조회 가능해야 한다")
        void shouldAddLogThenBeRetrievable() {
            LogEntry log = TestLogEntryProvider.executionTimeLog();

            logEntryContextManager.addLog(log);

            assertEquals(1, LogEntryContext.size());
            assertTrue(LogEntryContext.getLogs().contains(log));
            verify(mockLogAddHook, times(1)).onAdd(log);
        }

        @Test
        @DisplayName("로그가 MAX_LOG_SIZE를 초과하면 자동으로 flush 되어야 한다")
        void shouldFlushAutomaticallyWhenMaxLogSizeExceeded() {
            for (int i = 0; i < 310; i++) {
                logEntryContextManager.addLog(TestLogEntryProvider.executionTimeLog());
            }

            assertTrue(LogEntryContext.size() <= 300);
            verify(mockLogNotifier, atLeastOnce()).notify(eq(LogLevel.WARN), anyString());
        }
    }

    @Nested
    @DisplayName("로그 삭제 테스트")
    class ClearLogTests {

        @Test
        @DisplayName("flush 호출 시 모든 로그가 삭제되어야 한다")
        void shouldFlushLogsThenClearAll() {
            logEntryContextManager.addLog(TestLogEntryProvider.executionTimeLog());
            logEntryContextManager.addLog(TestLogEntryProvider.databaseQueryLog());

            logEntryContextManager.flush();

            assertEquals(0, LogEntryContext.size());
            verify(mockLogNotifier, times(2)).notify(any(LogEntry.class));
            verify(mockLogFlushHook, times(1)).onFlush(anyList());
        }

        @Test
        @DisplayName("clear 호출 시 모든 로그와 에러 상태가 초기화되어야 한다")
        void shouldClearAllLogsAndResetErrorState() {
            logEntryContextManager.addLog(TestLogEntryProvider.executionTimeLog());
            logEntryContextManager.addLog(TestLogEntryProvider.databaseQueryLog());
            LogEntryContext.setErrorOccurred(true);

            assertEquals(2, LogEntryContext.size());
            assertTrue(LogEntryContext.hasError());

            logEntryContextManager.clear();

            assertEquals(0, LogEntryContext.size());
            assertFalse(LogEntryContext.hasError());
        }
    }
}