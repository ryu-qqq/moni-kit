package com.monikit.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("LoggingPreparedStatement 테스트")
class LoggingPreparedStatementTest {

    private PreparedStatement mockPreparedStatement;
    private QueryLoggingService mockQueryLoggingService;
    private LoggingPreparedStatement loggingPreparedStatement;
    private final String SQL_QUERY = "SELECT * FROM users";

    @BeforeEach
    void setUp() throws SQLException {
        mockPreparedStatement = mock(PreparedStatement.class);
        mockQueryLoggingService = mock(QueryLoggingService.class);
        loggingPreparedStatement = new LoggingPreparedStatement(mockPreparedStatement, SQL_QUERY, mockQueryLoggingService);
    }

    @Nested
    @DisplayName("setObject() 테스트")
    class SetObjectTests {

        @Test
        @DisplayName("setObject 호출 시 SqlParameterHolder에 값이 추가되어야 한다")
        void shouldAddParameterToSqlParameterHolderWhenSetObjectIsCalled() throws SQLException {
            // Given
            int parameterIndex = 1;
            Object parameterValue = "testValue";

            // When
            loggingPreparedStatement.setObject(parameterIndex, parameterValue);

            // Then
            verify(mockPreparedStatement, times(1)).setObject(parameterIndex, parameterValue);
        }
    }

    @Nested
    @DisplayName("execute() 테스트")
    class ExecuteTests {

        @Test
        @DisplayName("execute 호출 시 QueryLoggingService.logQuery()가 실행되어야 한다")
        void shouldLogQueryWhenExecuteIsCalled() throws SQLException {
            // Given
            when(mockPreparedStatement.execute()).thenReturn(true);

            // When
            boolean result = loggingPreparedStatement.execute();

            // Then
            assertTrue(result);
            verify(mockQueryLoggingService, times(1)).logQuery(eq(SQL_QUERY), anyLong(), eq(0));
            verify(mockPreparedStatement, times(1)).execute();
        }
    }

    @Nested
    @DisplayName("executeQuery() 테스트")
    class ExecuteQueryTests {

        @Test
        @DisplayName("executeQuery 호출 시 LoggingResultSet이 반환되어야 한다")
        void shouldReturnLoggingResultSetWhenExecuteQueryIsCalled() throws SQLException {
            // Given
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            // When
            ResultSet resultSet = loggingPreparedStatement.executeQuery();

            // Then
            assertInstanceOf(LoggingResultSet.class, resultSet);
            verify(mockPreparedStatement, times(1)).executeQuery();
        }
    }

    @Nested
    @DisplayName("executeUpdate() 테스트")
    class ExecuteUpdateTests {

        @Test
        @DisplayName("executeUpdate 호출 시 QueryLoggingService.logQuery()가 실행되어야 한다")
        void shouldLogQueryWhenExecuteUpdateIsCalled() throws SQLException {
            // Given
            when(mockPreparedStatement.executeUpdate()).thenReturn(5);

            // When
            int rowsAffected = loggingPreparedStatement.executeUpdate();

            // Then
            assertEquals(5, rowsAffected);
            verify(mockQueryLoggingService, times(1)).logQuery(eq(SQL_QUERY), anyLong(), eq(5));
            verify(mockPreparedStatement, times(1)).executeUpdate();
        }
    }
}
