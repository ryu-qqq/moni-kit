package com.monikit.core;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.monikit.core.utils.TestLogEntryProvider;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ThreadContextPropagator 테스트")
class ThreadContextPropagatorTest {

    @BeforeEach
    void setup() {
        LogEntryContext.clear();
        LogEntryContext.setErrorOccurred(false);
    }

    @Nested
    @DisplayName("Runnable 기반 컨텍스트 전파 테스트")
    class RunnablePropagationTests {

        @Test
        @DisplayName("should propagate log context to child thread using Runnable")
        void shouldPropagateLogContextToChildThreadUsingRunnable() throws InterruptedException {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);
            LogEntryContext.setErrorOccurred(true);

            Runnable childTask = ThreadContextPropagator.propagateToChildThread(() -> {
                Queue<LogEntry> logs = LogEntryContext.getLogs();
                assertEquals(1, logs.size());
                assertTrue(logs.contains(log));
                assertTrue(LogEntryContext.hasError());
            });

            Thread thread = new Thread(childTask);
            thread.start();
            thread.join();
        }

        @Test
        @DisplayName("should clear child thread context without affecting parent")
        void shouldClearChildThreadContextWithoutAffectingParent() throws InterruptedException {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);
            LogEntryContext.setErrorOccurred(true);

            Runnable childTask = ThreadContextPropagator.propagateToChildThread(() -> {
                LogEntryContext.clear();
                assertEquals(0, LogEntryContext.size());
                assertFalse(LogEntryContext.hasError());
            });

            Thread thread = new Thread(childTask);
            thread.start();
            thread.join();

            // 부모 스레드의 컨텍스트는 유지되어야 함
            assertEquals(1, LogEntryContext.size());
            assertTrue(LogEntryContext.hasError());
        }
    }

    @Nested
    @DisplayName("Callable 기반 컨텍스트 전파 테스트")
    class CallablePropagationTests {

        @Test
        @DisplayName("should propagate log context to child thread using Callable and return expected result")
        void shouldPropagateLogContextToChildThreadUsingCallableAndReturnExpectedResult() throws Exception {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);
            LogEntryContext.setErrorOccurred(true);

            Callable<Boolean> childTask = ThreadContextPropagator.propagateToChildThread(() -> {
                Queue<LogEntry> logs = LogEntryContext.getLogs();
                assertEquals(1, logs.size());
                assertTrue(logs.contains(log));
                assertTrue(LogEntryContext.hasError());
                return true;
            });

            ExecutorService executor = newSingleThreadExecutor();
            Future<Boolean> future = executor.submit(childTask);
            boolean result = future.get();
            executor.shutdown();

            assertTrue(result);
        }

        @Test
        @DisplayName("should propagate error state to child thread using Callable")
        void shouldPropagateErrorStateToChildThreadUsingCallable() throws Exception {
            LogEntryContext.setErrorOccurred(true);

            Callable<Boolean> childTask = ThreadContextPropagator.propagateToChildThread(LogEntryContext::hasError);

            ExecutorService executor = newSingleThreadExecutor();
            Future<Boolean> future = executor.submit(childTask);
            boolean hasErrorInChildThread = future.get();
            executor.shutdown();

            assertTrue(hasErrorInChildThread);
        }

        @Test
        @DisplayName("should clear child thread context without affecting parent using Callable")
        void shouldClearChildThreadContextWithoutAffectingParentUsingCallable() throws Exception {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);
            LogEntryContext.setErrorOccurred(true);

            Callable<Boolean> childTask = ThreadContextPropagator.propagateToChildThread(() -> {
                LogEntryContext.clear();
                assertEquals(0, LogEntryContext.size());
                assertFalse(LogEntryContext.hasError());
                return true;
            });

            ExecutorService executor = newSingleThreadExecutor();
            Future<Boolean> future = executor.submit(childTask);
            boolean result = future.get();
            executor.shutdown();

            assertTrue(result);

            // 부모 스레드의 컨텍스트는 유지되어야 함
            assertEquals(1, LogEntryContext.size());
            assertTrue(LogEntryContext.hasError());
        }
    }
}