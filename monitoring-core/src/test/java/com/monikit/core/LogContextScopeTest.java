package com.monikit.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.monikit.core.utils.TestLogEntryProvider;

import static org.junit.jupiter.api.Assertions.*;

class LogContextScopeTest {

    @BeforeEach
    void setup() {
        LogEntryContextManager.setLogNotifier(new DefaultLogNotifier());
    }


    @Test
    @DisplayName("should flush logs automatically when closed")
    void shouldFlushLogsAutomaticallyWhenClosed() {
        try (LogContextScope scope = new LogContextScope()) {
            LogEntryContextManager.addLog(new ExecutionTimeLog("trace-123", LogLevel.INFO, "TestClass", "testMethod", 200));

            assertEquals(1, LogEntryContext.size());
        }

        assertEquals(0, LogEntryContext.size());
    }

    @Test
    @DisplayName("should clear all logs and reset error state")
    void shouldClearLogsAutomaticallyWhenStarted() {
        try (LogContextScope scope = new LogContextScope()) {
            assertEquals(0, LogEntryContext.size());
        }
    }

}