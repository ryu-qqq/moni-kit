package com.monikit.core;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("LoggingPreparedStatement 테스트")
class LoggingPreparedStatementTest {

    private final PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
    private final SqlParameterHolder mockHolder = mock(SqlParameterHolder.class);
    private final QueryLoggingService mockLoggingService = mock(QueryLoggingService.class);
    private final String traceId = "test-trace-id";
    private final String sql = "SELECT * FROM users WHERE id = ?";
    private final LoggingPreparedStatement loggingStatement = new LoggingPreparedStatement(mockPreparedStatement, traceId, sql, mockHolder, mockLoggingService);

    @Nested
    @DisplayName("SQL 파라미터 설정 테스트")
    class SqlParameterSetting {

        @Test
        @DisplayName("setInt() 호출 시 파라미터가 올바르게 저장되어야 한다.")
        void shouldStoreIntegerParameter() throws SQLException {
            // When
            loggingStatement.setInt(1, 42);

            // Then
            verify(mockHolder).addParameter(42);
            verify(mockPreparedStatement).setInt(1, 42);
        }

        @Test
        @DisplayName("setString() 호출 시 파라미터가 올바르게 저장되어야 한다.")
        void shouldStoreStringParameter() throws SQLException {
            // When
            loggingStatement.setString(1, "test-value");

            // Then
            verify(mockHolder).addParameter("test-value");
            verify(mockPreparedStatement).setString(1, "test-value");
        }

        @Test
        @DisplayName("setNull() 호출 시 NULL 값이 저장되어야 한다.")
        void shouldStoreNullParameter() throws SQLException {
            // When
            loggingStatement.setNull(1, Types.VARCHAR);

            // Then
            verify(mockHolder).addParameter("NULL");
            verify(mockPreparedStatement).setNull(1, Types.VARCHAR);
        }

        @Test
        @DisplayName("setBigDecimal() 호출 시 파라미터가 올바르게 저장되어야 한다.")
        void shouldStoreBigDecimalParameter() throws SQLException {
            // Given
            BigDecimal value = new BigDecimal("123.45");

            // When
            loggingStatement.setBigDecimal(1, value);

            // Then
            verify(mockHolder).addParameter(value);
            verify(mockPreparedStatement).setBigDecimal(1, value);
        }
    }

    @Nested
    @DisplayName("SQL 실행 및 로깅 테스트")
    class SqlExecutionLogging {

        @Test
        @DisplayName("execute() 호출 시 쿼리가 실행되고 로그가 남아야 한다.")
        void shouldLogQueryOnExecute() throws SQLException {
            // Given
            when(mockPreparedStatement.execute()).thenReturn(true);
            when(mockHolder.getCurrentParameters()).thenReturn("[1]");

            // When
            boolean result = loggingStatement.execute();

            // Then
            assertTrue(result);
            verify(mockLoggingService).logQuery(eq(traceId), eq(sql), eq("[1]"), anyLong(), eq(-1));
        }

        @Test
        @DisplayName("executeQuery() 호출 시 쿼리가 실행되고 로그가 남아야 한다.")
        void shouldLogQueryOnExecuteQuery() throws SQLException {
            // Given
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockHolder.getCurrentParameters()).thenReturn("[1]");

            // When
            ResultSet resultSet = loggingStatement.executeQuery();

            // Then
            assertNotNull(resultSet);
            verify(mockLoggingService).logQuery(eq(traceId), eq(sql), eq("[1]"), anyLong(), eq(-1));
        }

        @Test
        @DisplayName("executeUpdate() 호출 시 쿼리가 실행되고 영향받은 행 수가 로그에 기록되어야 한다.")
        void shouldLogQueryOnExecuteUpdate() throws SQLException {
            // Given
            int rowsAffected = 3;
            when(mockPreparedStatement.executeUpdate()).thenReturn(rowsAffected);
            when(mockHolder.getCurrentParameters()).thenReturn("[1]");

            // When
            int result = loggingStatement.executeUpdate();

            // Then
            assertEquals(rowsAffected, result);
            verify(mockLoggingService).logQuery(eq(traceId), eq(sql), eq("[1]"), anyLong(), eq(rowsAffected));
        }
    }

    @Test
    @DisplayName("close() 호출 시 PreparedStatement와 SqlParameterHolder가 모두 닫혀야 한다.")
    void shouldClosePreparedStatementAndHolder() throws SQLException {
        // When
        loggingStatement.close();

        // Then
        verify(mockPreparedStatement).close();
        verify(mockHolder).close();
    }
}
