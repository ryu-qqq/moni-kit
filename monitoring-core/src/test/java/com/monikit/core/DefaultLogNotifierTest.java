package com.monikit.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultLogNotifier")
class DefaultLogNotifierTest {

    private LogEntry sampleLog;
    private AtomicBoolean called;
    private LogSink testSink;

    @BeforeEach
    void setup() {
        called = new AtomicBoolean(false);
        sampleLog = new LogEntry() {
            @Override
            public Instant getTimestamp() {
                return Instant.now();
            }

            @Override
            public String getTraceId() {
                return "test-trace-id";
            }

            @Override
            public LogType getLogType() {
                return LogType.EXECUTION_TIME;
            }

            @Override
            public LogLevel getLogLevel() {
                return LogLevel.INFO;
            }

            @Override
            public String toString() {
                return "MockLogEntry{}";
            }
        };

        testSink = new LogSink() {
            @Override
            public boolean supports(LogType logType) {
                return logType == LogType.EXECUTION_TIME;
            }

            @Override
            public void send(LogEntry logEntry) {
                called.set(true);
                assertEquals(sampleLog.getTraceId(), logEntry.getTraceId());
            }
        };
    }

    @Test
    @DisplayName("LogSink가 없을 때 System.out 출력 확인")
    void shouldPrintLogToSystemOutWhenNoSinkProvided() {
        DefaultLogNotifier notifier = new DefaultLogNotifier(List.of());
        assertDoesNotThrow(() -> notifier.notify(sampleLog));
    }

    @Test
    @DisplayName("지원되는 Sink가 존재할 경우 send가 호출된다")
    void shouldCallSinkSendWhenSinkSupportsLogType() {
        DefaultLogNotifier notifier = new DefaultLogNotifier(List.of(testSink));
        notifier.notify(sampleLog);
        assertTrue(called.get());
    }

    @Test
    @DisplayName("LogLevel 메시지 출력도 문제 없이 동작")
    void shouldPrintMessageToSystemOut() {
        DefaultLogNotifier notifier = new DefaultLogNotifier(List.of());
        assertDoesNotThrow(() -> notifier.notify(LogLevel.INFO, "Test message"));
    }
}