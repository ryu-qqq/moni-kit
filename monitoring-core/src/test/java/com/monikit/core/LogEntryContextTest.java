package com.monikit.core;

import java.util.Queue;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.monikit.core.utils.TestLogEntryProvider;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LogEntryContext 테스트")
class LogEntryContextTest {

    @BeforeEach
    void setup() {
        LogEntryContext.clear();
    }

    @Nested
    @DisplayName("로그 추가 및 조회 테스트")
    class AddAndRetrieveLogTests {

        @Test
        @DisplayName("로그를 추가하면 조회 시 해당 로그가 포함되어 있어야 한다")
        void testAddLogAndGetLogs() {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);

            Queue<LogEntry> logs = LogEntryContext.getLogs();
            assertEquals(1, logs.size());
            assertTrue(logs.contains(log));
        }
    }

    @Nested
    @DisplayName("로그 삭제 테스트")
    class ClearLogTests {

        @Test
        @DisplayName("clear() 호출 후 getLogs()는 빈 리스트를 반환해야 한다")
        void testClearLogs() {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);

            LogEntryContext.clear();
            Queue<LogEntry> logs = LogEntryContext.getLogs();

            assertTrue(logs.isEmpty());
        }
    }

    @Nested
    @DisplayName("멀티스레드 컨텍스트 전파 테스트")
    class ThreadContextPropagationTests {

        @Test
        @DisplayName("부모 스레드에서 추가한 로그가 자식 스레드에서도 유지되어야 한다")
        void testPropagateToChildThread() throws InterruptedException {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);

            Runnable childTask = LogEntryContext.propagateToChildThread(() -> {
                Queue<LogEntry> logs = LogEntryContext.getLogs();
                assertEquals(1, logs.size());
                assertTrue(logs.contains(log));
            });

            Thread thread = new Thread(childTask);
            thread.start();
            thread.join();
        }
    }

    @ParameterizedTest
    @MethodSource("provideTestLogEntries")
    @DisplayName("다양한 로그 엔트리를 추가하고 조회할 수 있어야 한다")
    void testMultipleLogEntries(LogEntry logEntry) {
        LogEntryContextManager.addLog(logEntry);
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
}