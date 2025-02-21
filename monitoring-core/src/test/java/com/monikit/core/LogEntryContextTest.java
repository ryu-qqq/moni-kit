package com.monikit.core;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.monikit.core.utils.TestLogEntryProvider;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LogEntryContext & ThreadContextPropagator 테스트")
class LogEntryContextTest {

    @BeforeEach
    void setup() {
        LogEntryContext.clear();
        LogEntryContext.setErrorOccurred(false);
    }

    @Nested
    @DisplayName("로그 추가 및 조회 테스트")
    class AddAndRetrieveLogTests {

        @Test
        @DisplayName("should add log then it should be retrievable")
        void shouldAddLogThenBeRetrievable() {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);

            assertEquals(1, LogEntryContext.size());
            assertTrue(LogEntryContext.getLogs().contains(log));
        }
    }

    @Nested
    @DisplayName("로그 삭제 테스트")
    class ClearLogTests {

        @Test
        @DisplayName("should clear logs then getLogs should return empty")
        void shouldClearLogsThenGetLogsShouldReturnEmpty() {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);

            LogEntryContext.clear();
            assertEquals(0, LogEntryContext.size());
        }
    }

    @Nested
    @DisplayName("멀티스레드 컨텍스트 전파 테스트")
    class ThreadContextPropagationTests {

        @Test
        @DisplayName("should propagate log context to child thread then log should be available in child thread")
        void shouldPropagateLogContextToChildThreadThenLogShouldBeAvailableInChildThread() throws InterruptedException {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);

            Runnable childTask = ThreadContextPropagator.propagateToChildThread(() -> {
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

            Callable<Boolean> childTask = ThreadContextPropagator.propagateToChildThread(() -> {
                Queue<LogEntry> logs = LogEntryContext.getLogs();
                assertEquals(1, logs.size());
                assertTrue(logs.contains(log));
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
    }

    @ParameterizedTest
    @MethodSource("provideTestLogEntries")
    @DisplayName("should add multiple log entries then they should be retrievable")
    void shouldAddMultipleLogEntriesThenTheyShouldBeRetrievable(LogEntry logEntry) {
        LogEntryContext.addLog(logEntry);
        assertTrue(LogEntryContext.getLogs().contains(logEntry));
    }

    static Stream<LogEntry> provideTestLogEntries() {
        return Stream.of(
            TestLogEntryProvider.executionTimeLog(),
            TestLogEntryProvider.databaseQueryLog(),
            TestLogEntryProvider.exceptionLog(),
            TestLogEntryProvider.httpInboundRequestLog(),
            TestLogEntryProvider.httpInboundResponseLog(),
            TestLogEntryProvider.batchJobLog(),
            TestLogEntryProvider.httpOutboundRequestLog(),
            TestLogEntryProvider.httpOutboundResponseLog(),
            TestLogEntryProvider.executionDetailLog()
        );
    }

    @Nested
    @DisplayName("예외 상태 관리 테스트")
    class ErrorStateTests {

        @Test
        @DisplayName("should set error state when an exception occurs")
        void shouldSetErrorStateWhenExceptionOccurs() {
            assertFalse(LogEntryContext.hasError());

            LogEntryContext.setErrorOccurred(true);

            assertTrue(LogEntryContext.hasError());
        }

        @Test
        @DisplayName("should not reset error state on multiple calls")
        void shouldNotResetErrorStateOnMultipleCalls() {
            LogEntryContext.setErrorOccurred(true);
            LogEntryContext.setErrorOccurred(true);

            assertTrue(LogEntryContext.hasError());
        }

        @Test
        @DisplayName("should clear error state when log context is cleared")
        void shouldClearErrorStateWhenLogContextIsCleared() {
            LogEntryContext.setErrorOccurred(true);
            assertTrue(LogEntryContext.hasError());

            LogEntryContext.clear();
            assertFalse(LogEntryContext.hasError());
        }

        @Test
        @DisplayName("should propagate error state to child thread")
        void shouldPropagateErrorStateToChildThread() throws InterruptedException {
            LogEntryContext.setErrorOccurred(true);

            Runnable childTask = ThreadContextPropagator.propagateToChildThread(() -> {
                assertTrue(LogEntryContext.hasError());
            });

            Thread thread = new Thread(childTask);
            thread.start();
            thread.join();
        }
    }
}