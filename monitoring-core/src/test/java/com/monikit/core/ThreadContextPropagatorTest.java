package com.monikit.core;

import java.util.Queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.monikit.core.utils.TestLogEntryProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreadContextPropagatorTest {


    @BeforeEach
    void setup() {
        LogEntryContext.clear();
        LogEntryContext.setErrorOccurred(false);
        LogEntryContextManager.setLogNotifier(new DefaultLogNotifier());
    }


    @Nested
    @DisplayName("ThreadContextPropagator with Runnable")
    class RunnableTests {

        @Test
        @DisplayName("should propagate log context to child thread when Runnable is executed")
        void shouldPropagateLogContextToChildThread() throws Exception {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContextManager.addLog(log);

            Runnable task = () -> {
                Queue<LogEntry> logs = LogEntryContext.getLogs();
                assertEquals(1, logs.size());
                assertTrue(logs.contains(log));
            };

            ThreadContextPropagator.runWithContextRunnable(task);
        }

        @Test
        @DisplayName("should handle exceptions in Runnable and flush context")
        void shouldHandleExceptionsInRunnableAndFlushContext() throws Exception {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContextManager.addLog(log);

            Runnable task = () -> {
                throw new RuntimeException("Test exception");
            };

            try {
                ThreadContextPropagator.runWithContextRunnable(task);
            } catch (RuntimeException e) {
                assertEquals("Test exception", e.getMessage());
            }

            assertEquals(0, LogEntryContext.size());
        }
    }


    @Nested
    @DisplayName("ThreadContextPropagator with Callable")
    class CallableTests {

        @Test
        @DisplayName("should propagate log context to child thread when Callable is executed")
        void shouldPropagateLogContextToChildThread() throws Exception {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContextManager.addLog(log);

            Boolean result = ThreadContextPropagator.runWithContextCallable(() -> {
                Queue<LogEntry> logs = LogEntryContext.getLogs();
                assertEquals(1, logs.size());
                assertTrue(logs.contains(log));
                return true;
            });
            assertTrue(result);
        }

        @Test
        @DisplayName("should handle exceptions in Callable and flush context")
        void shouldHandleExceptionsInCallableAndFlushContext() throws Exception {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContextManager.addLog(log);

            Exception exception = assertThrows(RuntimeException.class, () ->
                ThreadContextPropagator.runWithContextCallable(() -> {
                    throw new RuntimeException("Test exception");
                })
            );

            assertTrue(exception.getMessage().contains("Test exception"));
            assertEquals(0, LogEntryContext.size());
        }

    }



}