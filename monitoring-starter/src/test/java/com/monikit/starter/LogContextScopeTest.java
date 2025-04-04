package com.monikit.starter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.monikit.core.LogEntryContextManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("LogContextScope 테스트")
@ExtendWith(MockitoExtension.class)
class LogContextScopeTest  {

    @Mock
    private LogEntryContextManager mockLogEntryContextManager;

    @Test
    @DisplayName("생성 시 LogEntryContextManager의 clear()가 호출되어야 한다.")
    void shouldCallClearOnCreation() {
        // When
        new LogContextScope(mockLogEntryContextManager);

        // Then
        verify(mockLogEntryContextManager, times(1)).clear();
    }

    @Test
    @DisplayName("close()가 호출되면 LogEntryContextManager의 flush()가 호출되어야 한다.")
    void shouldCallFlushOnClose() {
        // Given
        LogContextScope logContextScope = new LogContextScope(mockLogEntryContextManager);

        // When
        logContextScope.close();

        // Then
        verify(mockLogEntryContextManager, times(1)).flush();
    }

    @Test
    @DisplayName("try-with-resources 사용 시 자동으로 flush()가 호출되어야 한다.")
    void shouldAutoFlushOnTryWithResources() {
        // When
        try (LogContextScope ignored = new LogContextScope(mockLogEntryContextManager)) {
            // 내부 로직 실행
        }

        // Then
        verify(mockLogEntryContextManager, times(1)).flush();
    }
}