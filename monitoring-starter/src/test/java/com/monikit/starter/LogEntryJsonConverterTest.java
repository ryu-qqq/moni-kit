package com.monikit.starter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monikit.core.DatabaseQueryLog;
import com.monikit.core.LogEntry;
import com.monikit.core.utils.TestLogEntryProvider;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@DisplayName("LogEntryJsonConverter 테스트")
class LogEntryJsonConverterTest {

    @Nested
    @DisplayName("toJson() 메서드 동작 테스트")
    class ToJsonTests {

        @Test
        @DisplayName("LogEntry 객체를 JSON 문자열로 변환해야 한다.")
        void shouldConvertLogEntryToJson() {
            // Given

            DatabaseQueryLog databaseQueryLog = TestLogEntryProvider.databaseQueryLog();

            // When
            String jsonResult = LogEntryJsonConverter.toJson(databaseQueryLog);

            // Then
            assertNotNull(jsonResult);
            assertTrue(jsonResult.contains("test-trace-123"));
        }

        @Test
        @DisplayName("직렬화 실패 시 RuntimeException을 던져야 한다.")
        void shouldThrowRuntimeExceptionWhenSerializationFails() throws JsonProcessingException {
            // Given
            LogEntry logEntry = mock(LogEntry.class);
            ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
            doThrow(new JsonProcessingException("Serialization error") {}).when(mockObjectMapper).writeValueAsString(any());

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> LogEntryJsonConverter.toJson(logEntry));
            assertTrue(exception.getMessage().contains("Failed to serialize LogEntry to JSON"));
        }
    }
}