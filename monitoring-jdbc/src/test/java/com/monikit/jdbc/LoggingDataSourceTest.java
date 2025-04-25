package com.monikit.jdbc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import com.monikit.jdbc.proxy.LoggingPreparedStatementFactory;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoggingDataSource 테스트")
class LoggingDataSourceTest {

    private final DataSource mockDataSource = mock(DataSource.class);
    private final Connection mockConnection = mock(Connection.class);
    private final LoggingPreparedStatementFactory mockFactory = mock(LoggingPreparedStatementFactory.class);

    private final LoggingDataSource loggingDataSource = new LoggingDataSource(mockDataSource, mockFactory);

    @Nested
    @DisplayName("getConnection() 테스트")
    class GetConnectionTests {

        @Test
        @DisplayName("getConnection() 호출 시 LoggingConnection을 반환해야 한다.")
        void shouldReturnLoggingConnection() throws SQLException {
            // Given
            when(mockDataSource.getConnection()).thenReturn(mockConnection);

            // When
            Connection result = loggingDataSource.getConnection();

            // Then
            assertNotNull(result);
            assertTrue(result instanceof LoggingConnection);
            verify(mockDataSource).getConnection();
        }

        @Test
        @DisplayName("getConnection(username, password) 호출 시 LoggingConnection을 반환해야 한다.")
        void shouldReturnLoggingConnectionWithCredentials() throws SQLException {
            // Given
            String username = "testUser";
            String password = "testPass";
            when(mockDataSource.getConnection(username, password)).thenReturn(mockConnection);

            // When
            Connection result = loggingDataSource.getConnection(username, password);

            // Then
            assertNotNull(result);
            assertTrue(result instanceof LoggingConnection);
            verify(mockDataSource).getConnection(username, password);
        }
    }
}
