package com.monikit.core;

import java.util.Queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.monikit.core.utils.TestLogEntryProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LogEntryContextManager 테스트")
class LogEntryContextManagerTest {

    @BeforeEach
    void setup() {
        LogEntryContext.clear();
    }

    @Test
    @DisplayName("addLog() 호출 시 로그가 정상적으로 추가되어야 한다")
    void testAddLog() {
        LogEntry log = TestLogEntryProvider.executionTimeLog();
        LogEntryContextManager.addLog(log);

        Queue<LogEntry> logs = LogEntryContext.getLogs();
        assertEquals(1, logs.size());
        assertTrue(logs.contains(log));
    }

    @Test
    @DisplayName("flush() 호출 시 모든 로그가 삭제되어야 한다")
    void testFlush() {
        LogEntry log1 = TestLogEntryProvider.executionTimeLog();
        LogEntry log2 = TestLogEntryProvider.databaseQueryLog();

        LogEntryContextManager.addLog(log1);
        LogEntryContextManager.addLog(log2);
        LogEntryContextManager.flush();

        assertTrue(LogEntryContext.getLogs().isEmpty());
    }

    @Test
    @DisplayName("MAX_LOG_SIZE를 초과하면 로그가 초기화되어야 한다")
    void testMaxLogSizeExceeded() {
        for (int i = 0; i < 1001; i++) {
            LogEntryContextManager.addLog(TestLogEntryProvider.executionTimeLog());
        }

        assertTrue(LogEntryContext.getLogs().size() <= 1000);
    }
}