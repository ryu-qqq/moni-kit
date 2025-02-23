package com.monikit.starter;

import com.monikit.core.LogEntry;
import com.monikit.core.LogLevel;
import com.monikit.starter.utils.TestLogEntryProvider;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.*;

@DisplayName("LogbackLogNotifier 테스트")
class DefaultSlf4jLogNotifierTest {

    private static MockedStatic<LoggerFactory> mockedLoggerFactory;
    private static Logger mockLogger;
    private static DefaultSlf4jLogNotifier logNotifier;

    @BeforeAll
    static void setup() {
        mockLogger = mock(Logger.class);

        mockedLoggerFactory = mockStatic(LoggerFactory.class);
        mockedLoggerFactory.when(() -> LoggerFactory.getLogger(DefaultSlf4jLogNotifier.class))
            .thenReturn(mockLogger);

        logNotifier = new DefaultSlf4jLogNotifier();
    }

    @AfterAll
    static void tearDown() {
        mockedLoggerFactory.close();
    }

    @Nested
    @DisplayName("notify(LogLevel, String) 테스트")
    class NotifyLogLevelStringTests {

        @Test
        @DisplayName("INFO 레벨의 로그가 정상적으로 출력되어야 한다.")
        void shouldLogInfoLevelMessage() {
            // Given
            String message = "Test Info Message";

            // When
            logNotifier.notify(LogLevel.INFO, message);

            // Then
            verify(mockLogger).info(message);
        }

        @Test
        @DisplayName("ERROR 레벨의 로그가 정상적으로 출력되어야 한다.")
        void shouldLogErrorLevelMessage() {
            // Given
            String message = "Test Error Message";

            // When
            logNotifier.notify(LogLevel.ERROR, message);

            // Then
            verify(mockLogger).error(message);
        }
    }

    @Nested
    @DisplayName("notify(LogEntry) 테스트")
    class NotifyLogEntryTests {

        @Test
        @DisplayName("LogEntry 객체가 정상적으로 JSON 변환 후 로그 출력되어야 한다.")
        void shouldLogJsonFormattedLogEntry() {
            // Given
            LogEntry logEntry = TestLogEntryProvider.executionTimeLog();
            String jsonLog = LogEntryJsonConverter.toJson(logEntry);

            // When
            logNotifier.notify(logEntry);

            // Then
            verify(mockLogger).info(jsonLog);
        }

        @Test
        @DisplayName("JSON 직렬화 실패 시 오류 메시지가 ERROR 레벨로 기록되어야 한다.")
        void shouldLogErrorWhenSerializationFails() {
            // Given
            LogEntry logEntry = TestLogEntryProvider.executionTimeLog();

            try (var mockedConverter = mockStatic(LogEntryJsonConverter.class)) {
                mockedConverter.when(() -> LogEntryJsonConverter.toJson(logEntry))
                    .thenThrow(new RuntimeException("Serialization Error"));

                // When
                logNotifier.notify(logEntry);

                // Then
                verify(mockLogger).error(
                    eq("Failed to serialize LogEntry: {}"),
                    eq("Serialization Error"),
                    any(RuntimeException.class)
                );

                verify(mockLogger).error(contains("Log serialization failed: Serialization Error"));
            }
        }
    }
}
