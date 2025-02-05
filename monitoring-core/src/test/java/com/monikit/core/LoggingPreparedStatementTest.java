package com.monikit.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoggingPreparedStatementTest {

    private PreparedStatement mockDelegate;
    private LoggingPreparedStatement loggingPreparedStatement;

    @BeforeEach
    void setUp() {
        mockDelegate = mock(PreparedStatement.class);
        loggingPreparedStatement = new LoggingPreparedStatement(mockDelegate, "SELECT * FROM users WHERE id = ?");
        MetricCollectorProvider.setMetricCollector(new TestMetricCollector());
    }



    @Test
    @DisplayName("should add parameter on setObject method")
    void shouldAddParameterOnSetObject() throws SQLException {
        String param = "test";

        loggingPreparedStatement.setObject(1, param);

        verify(mockDelegate).setObject(1, param);
        assertTrue(SqlParameterHolder.getCurrentParameters().contains(param));
    }

    @Test
    @DisplayName("should log query execution on execute method")
    void shouldLogQueryOnExecute() throws SQLException {
        when(mockDelegate.execute()).thenReturn(true);

        boolean result = loggingPreparedStatement.execute();

        verify(mockDelegate).execute();
        assertTrue(result);
    }

    @Test
    @DisplayName("should clear parameters after execution")
    void shouldClearParametersAfterExecution() throws SQLException {
        String param = "test";
        loggingPreparedStatement.setObject(1, param);

        loggingPreparedStatement.execute();

        assertEquals("[]", SqlParameterHolder.getCurrentParameters());
    }



    static class TestMetricCollector implements MetricCollector {

        @Override
        public void recordHttpRequest(String method, String uri, int statusCode, long duration) {
        }

        @Override
        public void recordQueryMetrics(String sql, long executionTime, String dataSourceName) {
        }
    }

}