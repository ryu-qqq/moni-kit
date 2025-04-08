package com.monikit.starter.jdbc.proxy;

import com.monikit.core.TraceIdProvider;
import com.monikit.starter.jdbc.QueryLoggingService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@DisplayName("LoggingPreparedStatementFactory 테스트")
class LoggingPreparedStatementFactoryTest {

    private final QueryLoggingService mockQueryLoggingService = Mockito.mock(QueryLoggingService.class);
    private final TraceIdProvider mockTraceIdProvider = Mockito.mock(TraceIdProvider.class);

    private final LoggingPreparedStatementFactory factory = new LoggingPreparedStatementFactory(mockQueryLoggingService, mockTraceIdProvider);

    @Test
    @DisplayName("create() 호출 시 LoggingPreparedStatement를 반환해야 한다.")
    void shouldReturnLoggingPreparedStatement() {
        // Given
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        String sql = "SELECT * FROM users";

        // When
        LoggingPreparedStatement result = factory.create(mockPreparedStatement, sql);

        // Then
        assertNotNull(result, "생성된 LoggingPreparedStatement는 null이 아니어야 한다.");
        assertInstanceOf(LoggingPreparedStatement.class, result, "반환된 객체는 LoggingPreparedStatement여야 한다.");
    }

}
