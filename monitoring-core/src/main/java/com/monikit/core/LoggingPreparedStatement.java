package com.monikit.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    private final String sql;

    public LoggingPreparedStatement(PreparedStatement delegate, String sql) {
        super(delegate);
        this.sql = sql;
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        SqlParameterHolder.addParameter(x);
        super.setObject(parameterIndex, x);
    }

    @Override
    public boolean execute() throws SQLException {
        long startTime = System.currentTimeMillis();
        try {
            return super.execute();
        } finally {
            QueryLoggingService.logQuery(sql, System.currentTimeMillis() - startTime, 0);
            SqlParameterHolder.clear();
        }
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        long startTime = System.currentTimeMillis();
        ResultSet resultSet = super.executeQuery();

        return new LoggingResultSet(resultSet, sql, startTime);
    }

    @Override
    public int executeUpdate() throws SQLException {
        long startTime = System.currentTimeMillis();
        int rowsAffected = 0;
        try {
            rowsAffected = super.executeUpdate();
            return rowsAffected;
        } catch (SQLException e) {
            rowsAffected = -1;
            throw e;
        } finally {
            QueryLoggingService.logQuery(sql, System.currentTimeMillis() - startTime, rowsAffected);
            SqlParameterHolder.clear();
        }
    }


}