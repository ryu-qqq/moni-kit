package com.monikit.core;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * SQL 실행을 감시하는 `PreparedStatement` 프록시.
 * <p>
 * SQL 실행 시간, 실행된 SQL, 바인딩된 값, 영향을 받은 행 개수를 추적할 수 있음.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public class LoggingPreparedStatement extends PreparedStatementWrapper {
    private final String traceId;
    private final String sql;
    private final SqlParameterHolder holder;
    private final QueryLoggingService queryLoggingService;

    public LoggingPreparedStatement(PreparedStatement delegate, String traceId, String sql,  SqlParameterHolder holder, QueryLoggingService queryLoggingService) {
        super(delegate);
        this.traceId = traceId;
        this.sql = sql;
        this.holder = holder;
        this.queryLoggingService = queryLoggingService;
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        holder.addParameter(x);
        super.setObject(parameterIndex, x);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        holder.addParameter("NULL");
        super.setNull(parameterIndex, sqlType);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        holder.addParameter(x);
        super.setBoolean(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        holder.addParameter(x);
        super.setInt(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        holder.addParameter(x);
        super.setLong(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        holder.addParameter(x);
        super.setDouble(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        holder.addParameter(x);
        super.setString(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        holder.addParameter(x);
        super.setDate(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        holder.addParameter(x);
        super.setTime(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        holder.addParameter(x);
        super.setTimestamp(parameterIndex, x);
    }


    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        holder.addParameter(x);
        super.setBigDecimal(parameterIndex, x);
    }



    @Override
    public boolean execute() throws SQLException {
        long start = System.currentTimeMillis();
        boolean result = super.execute();
        long executionTime = System.currentTimeMillis() - start;
        queryLoggingService.logQuery(traceId, sql, holder.getCurrentParameters(), executionTime, -1);
        return result;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        long start = System.currentTimeMillis();
        ResultSet result = super.executeQuery();
        long executionTime = System.currentTimeMillis() - start;
        queryLoggingService.logQuery(traceId, sql, holder.getCurrentParameters(), executionTime, -1);
        return result;
    }

    @Override
    public int executeUpdate() throws SQLException {
        long start = System.currentTimeMillis();
        int rowsAffected = super.executeUpdate();
        long executionTime = System.currentTimeMillis() - start;
        queryLoggingService.logQuery(traceId, sql, holder.getCurrentParameters(), executionTime, rowsAffected);
        return rowsAffected;
    }

    @Override
    public void close() throws SQLException {
        try {
            super.close();
        } finally {
            holder.close();
        }
    }

}