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

import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DefaultThreadContextHandler 테스트")
class DefaultThreadContextHandlerTest {

    private DefaultThreadContextHandler threadContextHandler;

    @BeforeEach
    void setup() {
        LogEntryContext.clear();
        LogEntryContext.setErrorOccurred(false);
        threadContextHandler = new DefaultThreadContextHandler();
    }

    @Nested
    @DisplayName("멀티스레드 컨텍스트 전파 테스트")
    class ThreadContextPropagationTests {

        @Test
        @DisplayName("Runnable을 통해 부모 스레드의 로그 컨텍스트가 자식 스레드로 전달되어야 한다")
        void shouldPropagateLogContextToChildThread() throws InterruptedException {
            // Given
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);

            // When
            Runnable childTask = threadContextHandler.propagateToChildThread(() -> {
                Queue<LogEntry> logs = LogEntryContext.getLogs();
                assertEquals(1, logs.size());
                assertTrue(logs.contains(log));
            });

            Thread thread = new Thread(childTask);
            thread.start();
            thread.join();
        }

        @Test
        @DisplayName("Callable을 통해 부모 스레드의 로그 컨텍스트가 자식 스레드로 전달되어야 한다")
        void shouldPropagateLogContextUsingCallable() throws Exception {
            // Given
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);

            // When
            Callable<Boolean> childTask = threadContextHandler.propagateToChildThread(() -> {
                Queue<LogEntry> logs = LogEntryContext.getLogs();
                return logs.size() == 1 && logs.contains(log);
            });

            ExecutorService executor = newSingleThreadExecutor();
            Future<Boolean> future = executor.submit(childTask);
            boolean result = future.get();
            executor.shutdown();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("각 스레드가 독립적인 컨텍스트를 유지해야 한다")
        void shouldHaveThreadLocalIsolationBetweenThreads() throws Exception {
            ExecutorService executor = newFixedThreadPool(2);

            Future<Boolean> future1 = executor.submit(() -> {
                LogEntryContext.setErrorOccurred(true);
                return LogEntryContext.hasError();
            });

            Future<Boolean> future2 = executor.submit(() -> LogEntryContext.hasError());

            boolean hasErrorInThread1 = future1.get();
            boolean hasErrorInThread2 = future2.get();

            executor.shutdown();

            assertTrue(hasErrorInThread1);
            assertFalse(hasErrorInThread2);
        }

        @Test
        @DisplayName("여러 개의 스레드에서 부모 컨텍스트가 올바르게 전파되어야 한다")
        void shouldPropagateLogContextToMultipleChildThreads() throws Exception {
            // Given: 부모 스레드에서 LogEntry를 추가
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);

            ExecutorService executor = newFixedThreadPool(5);

            // When: 여러 개의 스레드가 동시에 실행되도록 설정
            Callable<Boolean> task = threadContextHandler.propagateToChildThread(() -> {
                Queue<LogEntry> logs = LogEntryContext.getLogs();
                return logs.size() == 1 && logs.contains(log);
            });

            Future<Boolean> future1 = executor.submit(task);
            Future<Boolean> future2 = executor.submit(task);
            Future<Boolean> future3 = executor.submit(task);
            Future<Boolean> future4 = executor.submit(task);
            Future<Boolean> future5 = executor.submit(task);

            // Then: 모든 스레드가 부모의 컨텍스트를 올바르게 복사받았는지 확인
            assertTrue(future1.get());
            assertTrue(future2.get());
            assertTrue(future3.get());
            assertTrue(future4.get());
            assertTrue(future5.get());

            executor.shutdown();

        }


        @Test
        @DisplayName("각 스레드는 독립적인 로그 컨텍스트를 유지해야 한다")
        void shouldMaintainThreadLocalIsolation() throws Exception {
            ExecutorService executor = newFixedThreadPool(2);

            Future<Boolean> future1 = executor.submit(() -> {
                LogEntryContext.setErrorOccurred(true);
                return LogEntryContext.hasError();
            });

            Future<Boolean> future2 = executor.submit(LogEntryContext::hasError);

            boolean hasErrorInThread1 = future1.get();
            boolean hasErrorInThread2 = future2.get();

            executor.shutdown();

            assertTrue(hasErrorInThread1);
            assertFalse(hasErrorInThread2);
        }

    }

    @Nested
    @DisplayName("예외 처리 테스트")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("예외 발생 시 로그 컨텍스트에 예외 정보가 기록되어야 한다")
        void shouldLogExceptionWhenItOccurs() {
            // Given
            String traceId = "test-trace-id";
            Exception exception = new RuntimeException("Test exception");

            // When
            threadContextHandler.logException(traceId, exception, ErrorCategory.APPLICATION_ERROR);

            // Then
            Queue<LogEntry> logs = LogEntryContext.getLogs();
            assertEquals(1, logs.size());
            assertInstanceOf(ExceptionLog.class, logs.iterator().next());
            assertTrue(LogEntryContext.hasError());
        }

        @Test
        @DisplayName("중복된 예외는 기록되지 않아야 한다")
        void shouldNotLogDuplicateExceptions() {
            // Given
            String traceId = "test-trace-id";
            Exception firstException = new RuntimeException("First exception");
            Exception secondException = new RuntimeException("Second exception");

            // When
            threadContextHandler.logException(traceId, firstException, ErrorCategory.APPLICATION_ERROR);
            threadContextHandler.logException(traceId, secondException, ErrorCategory.APPLICATION_ERROR);

            // Then
            Queue<LogEntry> logs = LogEntryContext.getLogs();
            assertEquals(1, logs.size());
            assertInstanceOf(ExceptionLog.class, logs.iterator().next());
            assertTrue(LogEntryContext.hasError());
        }
    }


}
