package com.monikit.starter.jdbc.proxy;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.monikit.starter.jdbc.QueryLoggingService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("LoggingPreparedStatement 테스트")
class LoggingPreparedStatementTest {

    private final PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
    private final SqlParameterHolder mockHolder = Mockito.mock(SqlParameterHolder.class);
    private final QueryLoggingService mockLoggingService = Mockito.mock(QueryLoggingService.class);
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
            Mockito.verify(mockHolder).addParameter(42);
            Mockito.verify(mockPreparedStatement).setInt(1, 42);
        }

        @Test
        @DisplayName("setString() 호출 시 파라미터가 올바르게 저장되어야 한다.")
        void shouldStoreStringParameter() throws SQLException {
            // When
            loggingStatement.setString(1, "test-value");

            // Then
            Mockito.verify(mockHolder).addParameter("test-value");
            Mockito.verify(mockPreparedStatement).setString(1, "test-value");
        }

        @Test
        @DisplayName("setNull() 호출 시 NULL 값이 저장되어야 한다.")
        void shouldStoreNullParameter() throws SQLException {
            // When
            loggingStatement.setNull(1, Types.VARCHAR);

            // Then
            Mockito.verify(mockHolder).addParameter("NULL");
            Mockito.verify(mockPreparedStatement).setNull(1, Types.VARCHAR);
        }

        @Test
        @DisplayName("setBigDecimal() 호출 시 파라미터가 올바르게 저장되어야 한다.")
        void shouldStoreBigDecimalParameter() throws SQLException {
            // Given
            BigDecimal value = new BigDecimal("123.45");

            // When
            loggingStatement.setBigDecimal(1, value);

            // Then
            Mockito.verify(mockHolder).addParameter(value);
            Mockito.verify(mockPreparedStatement).setBigDecimal(1, value);
        }
    }

    @Nested
    @DisplayName("SQL 실행 및 로깅 테스트")
    class SqlExecutionLogging {

        @Test
        @DisplayName("execute() 호출 시 쿼리가 실행되고 로그가 남아야 한다.")
        void shouldLogQueryOnExecute() throws SQLException {
            // Given
            Mockito.when(mockPreparedStatement.execute()).thenReturn(true);
            Mockito.when(mockHolder.getCurrentParameters()).thenReturn("[1]");

            // When
            boolean result = loggingStatement.execute();

            // Then
            assertTrue(result);
            Mockito.verify(mockLoggingService).logQuery(ArgumentMatchers.eq(traceId), ArgumentMatchers.eq(sql), ArgumentMatchers.eq("[1]"), ArgumentMatchers.anyLong(), ArgumentMatchers.eq(-1));
        }

        @Test
        @DisplayName("executeQuery() 호출 시 쿼리가 실행되고 로그가 남아야 한다.")
        void shouldLogQueryOnExecuteQuery() throws SQLException {
            // Given
            ResultSet mockResultSet = Mockito.mock(ResultSet.class);
            Mockito.when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            Mockito.when(mockHolder.getCurrentParameters()).thenReturn("[1]");

            // When
            ResultSet resultSet = loggingStatement.executeQuery();

            // Then
            assertNotNull(resultSet);
            Mockito.verify(mockLoggingService).logQuery(ArgumentMatchers.eq(traceId), ArgumentMatchers.eq(sql), ArgumentMatchers.eq("[1]"), ArgumentMatchers.anyLong(), ArgumentMatchers.eq(-1));
        }

        @Test
        @DisplayName("executeUpdate() 호출 시 쿼리가 실행되고 영향받은 행 수가 로그에 기록되어야 한다.")
        void shouldLogQueryOnExecuteUpdate() throws SQLException {
            // Given
            int rowsAffected = 3;
            Mockito.when(mockPreparedStatement.executeUpdate()).thenReturn(rowsAffected);
            Mockito.when(mockHolder.getCurrentParameters()).thenReturn("[1]");

            // When
            int result = loggingStatement.executeUpdate();

            // Then
            assertEquals(rowsAffected, result);
            Mockito.verify(mockLoggingService).logQuery(ArgumentMatchers.eq(traceId), ArgumentMatchers.eq(sql), ArgumentMatchers.eq("[1]"), ArgumentMatchers.anyLong(), ArgumentMatchers.eq(rowsAffected));
        }
    }

    @Test
    @DisplayName("close() 호출 시 PreparedStatement와 SqlParameterHolder가 모두 닫혀야 한다.")
    void shouldClosePreparedStatementAndHolder() throws SQLException {
        // When
        loggingStatement.close();

        // Then
        Mockito.verify(mockPreparedStatement).close();
        Mockito.verify(mockHolder).close();
    }
}
