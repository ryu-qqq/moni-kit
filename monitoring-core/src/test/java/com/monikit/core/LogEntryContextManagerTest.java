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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LogEntryContextManager 테스트")
class LogEntryContextManagerTest {

    @BeforeEach
    void setup() {
        LogEntryContext.clear();
    }

    @Test
    @DisplayName("should add log then it should be retrievable")
    void shouldAddLogThenBeRetrievable() {
        LogEntry log = TestLogEntryProvider.executionTimeLog();
        LogEntryContextManager.addLog(log);

        Queue<LogEntry> logs = LogEntryContext.getLogs();
        assertEquals(1, logs.size());
        assertTrue(logs.contains(log));
    }

    @Test
    @DisplayName("should flush logs then all logs should be cleared")
    void shouldFlushLogsThenAllLogsShouldBeCleared() {
        LogEntry log1 = TestLogEntryProvider.executionTimeLog();
        LogEntry log2 = TestLogEntryProvider.databaseQueryLog();

        LogEntryContextManager.addLog(log1);
        LogEntryContextManager.addLog(log2);
        LogEntryContextManager.flush();

        assertEquals(0, LogEntryContext.size());
    }

    @Test
    @DisplayName("should clear logs when MAX_LOG_SIZE is exceeded")
    void shouldClearLogsWhenMaxLogSizeIsExceeded() {
        for (int i = 0; i < 1001; i++) {
            LogEntryContextManager.addLog(TestLogEntryProvider.executionTimeLog());
        }

        assertTrue(LogEntryContext.size() <= 1000);
    }

    @Test
    @DisplayName("should log exception when it occurs")
    void shouldLogExceptionWhenItOccurs() {
        String traceId = "test-trace-id";
        Exception exception = new RuntimeException("Test exception");

        LogEntryContextManager.logException(traceId, exception);

        Queue<LogEntry> logs = LogEntryContext.getLogs();
        assertEquals(1, logs.size());
        assertInstanceOf(ExceptionLog.class, logs.iterator().next());
        assertTrue(LogEntryContext.hasError());
    }

    @Test
    @DisplayName("should not log duplicate exceptions")
    void shouldNotLogDuplicateExceptions() {
        String traceId = "test-trace-id";
        Exception firstException = new RuntimeException("First exception");
        Exception secondException = new RuntimeException("Second exception");

        LogEntryContextManager.logException(traceId, firstException);
        LogEntryContextManager.logException(traceId, secondException);

        Queue<LogEntry> logs = LogEntryContext.getLogs();
        assertEquals(1, logs.size());
        assertInstanceOf(ExceptionLog.class, logs.iterator().next());
        assertTrue(LogEntryContext.hasError());
    }

    @Test
    @DisplayName("should reset error state after flush")
    void shouldResetErrorStateAfterFlush() {
        String traceId = "test-trace-id";
        Exception exception = new RuntimeException("Test exception");

        LogEntryContextManager.logException(traceId, exception);
        assertTrue(LogEntryContext.hasError());

        LogEntryContextManager.flush();

        assertFalse(LogEntryContext.hasError());
        assertEquals(0, LogEntryContext.size());
    }


    @Nested
    @DisplayName("멀티스레드 컨텍스트 전파 테스트")
    class ThreadContextPropagationTests {

        @Test
        @DisplayName("should propagate log context to child thread then log should be available in child thread")
        void shouldPropagateLogContextToChildThreadThenLogShouldBeAvailableInChildThread() throws InterruptedException {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);

            Runnable childTask = LogEntryContextManager.propagateToChildThread(() ->{
                Queue<LogEntry> logs = LogEntryContext.getLogs();
                assertEquals(1, logs.size());
                assertTrue(logs.contains(log));

            });

            Thread thread = new Thread(childTask);
            thread.start();
            thread.join();
        }

        @Test
        @DisplayName("should propagate log context using Callable and return expected result")
        void shouldPropagateLogContextUsingCallableAndReturnExpectedResult() throws Exception {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);

            Callable<Boolean> childTask = LogEntryContextManager.propagateToChildThread(() -> {
                Queue<LogEntry> logs = LogEntryContext.getLogs();
                assertEquals(1, logs.size());
                assertTrue(logs.contains(log));
                return true;
            });

            ExecutorService executor = newSingleThreadExecutor();
            Future<Boolean> future = executor.submit(childTask);
            boolean result = future.get(); // Callable 실행 결과 가져오기
            executor.shutdown();

            assertTrue(result); // Callable이 정상 실행되었는지 검증
        }

        @Test
        @DisplayName("should propagate error state to child thread using Callable")
        void shouldPropagateErrorStateToChildThreadUsingCallable() throws Exception {
            LogEntryContext.setErrorOccurred(true);

            Callable<Boolean> childTask = LogEntryContextManager.propagateToChildThread(LogEntryContext::hasError);

            ExecutorService executor = newSingleThreadExecutor();
            Future<Boolean> future = executor.submit(childTask);
            boolean hasErrorInChildThread = future.get();
            executor.shutdown();

            assertTrue(hasErrorInChildThread);
        }

    }

}