package com.monikit.starter;

import com.monikit.core.DatabaseQueryLog;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogLevel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * SQL 실행을 감시하는 `PreparedStatement` 프록시.
 * <p>
 * SQL 실행 시간, 실행된 SQL, 바인딩된 값, 영향을 받은 행 개수를 추적할 수 있음.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class LoggingPreparedStatement extends PreparedStatementWrapper {

    private static final Logger LOGGER = Logger.getLogger("SQL_LOGGER");
    private final String sql;
    private final List<Object> parameters = new ArrayList<>();

    public LoggingPreparedStatement(PreparedStatement delegate, String sql) {
        super(delegate);
        this.sql = sql;
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        parameters.add(parameterIndex - 1, x);
        super.setObject(parameterIndex, x);
    }

    @Override
    public boolean execute() throws SQLException {
        long startTime = System.currentTimeMillis();
        boolean result = super.execute();
        logExecution(System.currentTimeMillis() - startTime, 0);
        return result;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        long startTime = System.currentTimeMillis();
        ResultSet resultSet = super.executeQuery();
        logExecution(System.currentTimeMillis() - startTime, -1);
        return resultSet;
    }

    @Override
    public int executeUpdate() throws SQLException {
        long startTime = System.currentTimeMillis();
        int rowsAffected = super.executeUpdate();
        logExecution(System.currentTimeMillis() - startTime, rowsAffected);
        return rowsAffected;
    }

    private void logExecution(long executionTime, int rowsAffected) {
        String traceId = TraceIdHolder.getTraceId();
        String executedQuery = sql;
        String parametersStr = parameters.toString();

        LOGGER.info(String.format("[SQL] %s -- Params: %s -- Execution Time: %dms", executedQuery, parametersStr, executionTime));

        DatabaseQueryLog logEntry = DatabaseQueryLog.create(
            traceId, executedQuery, executionTime, "defaultDataSource", "unknown",
            parametersStr, rowsAffected, -1, LogLevel.INFO
        );
        LogEntryContextManager.addLog(logEntry);
    }
}
