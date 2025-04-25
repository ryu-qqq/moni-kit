package com.monikit.jdbc.proxy;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

import com.monikit.jdbc.LoggingConnection;
import com.monikit.jdbc.QueryLoggingService;

/**
 * {@link PreparedStatement}의 SQL 실행을 감시하고 로깅하는 프록시 클래스.
 *
 * <p>
 * SQL 실행 시간, 실행된 쿼리문, 바인딩된 파라미터, 영향받은 행 수 등을 {@link QueryLoggingService}를 통해 기록한다.
 * 내부적으로 {@link SqlParameterHolder}를 사용하여 파라미터를 수집하고,
 * {@code try-with-resources} 구문을 사용하여 파라미터를 자동 정리한다.
 * </p>
 *
 * <p>
 * 이 클래스는 {@link LoggingPreparedStatementFactory}에 의해 생성되며, {@link LoggingConnection}을 통해 사용된다.
 * 실제 쿼리 실행 시점에 로깅 처리가 이루어진다.
 * </p>
 *
 * <pre>{@code
 * PreparedStatement ps = connection.prepareStatement("SELECT * FROM users WHERE id = ?");
 * ps.setInt(1, 42);
 * ps.executeQuery(); // -> 로깅 자동 수행
 * }</pre>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

public class LoggingPreparedStatement extends PreparedStatementWrapper {
    private final String traceId;
    private final String sql;
    private final SqlParameterHolder holder;
    private final QueryLoggingService queryLoggingService;

    /**
     * 생성자
     *
     * @param delegate             원본 {@link PreparedStatement}
     * @param traceId              로그 컨텍스트에 포함될 Trace ID
     * @param sql                  실행될 SQL 문
     * @param holder               바인딩 파라미터 저장소
     * @param queryLoggingService 쿼리 로깅 서비스
     */

    public LoggingPreparedStatement(PreparedStatement delegate, String traceId, String sql,  SqlParameterHolder holder, QueryLoggingService queryLoggingService) {
        super(delegate);
        this.traceId = traceId;
        this.sql = sql;
        this.holder = holder;
        this.queryLoggingService = queryLoggingService;
    }

    /**
     * {@inheritDoc}
     * <p>
     * 쿼리 실행 전후로 실행 시간을 측정하고, 로깅을 수행한다.
     * </p>
     */

    @Override
    public boolean execute() throws SQLException {
        try(holder) {
            long start = System.currentTimeMillis();
            boolean result = super.execute();
            long executionTime = System.currentTimeMillis() - start;
            queryLoggingService.logQuery(traceId, sql, holder.getCurrentParameters(), executionTime, -1);
            return result;
        }
    }


    /**
     * {@inheritDoc}
     * <p>
     * 쿼리 실행 전후로 실행 시간을 측정하고, 로깅을 수행한다.
     * </p>
     */


    @Override
    public ResultSet executeQuery() throws SQLException {
        try(holder){
            long start = System.currentTimeMillis();
            ResultSet result = super.executeQuery();
            long executionTime = System.currentTimeMillis() - start;
            queryLoggingService.logQuery(traceId, sql, holder.getCurrentParameters(), executionTime, -1);
            return result;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * 쿼리 실행 전후로 실행 시간을 측정하고, 영향을 받은 행 수를 포함하여 로깅을 수행한다.
     * </p>
     */

    @Override
    public int executeUpdate() throws SQLException {
        try(holder){
            long start = System.currentTimeMillis();
            int rowsAffected = super.executeUpdate();
            long executionTime = System.currentTimeMillis() - start;
            queryLoggingService.logQuery(traceId, sql, holder.getCurrentParameters(), executionTime, rowsAffected);
            return rowsAffected;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * 자원 해제 시, {@link SqlParameterHolder}도 함께 정리한다.
     * </p>
     */

    @Override
    public void close() throws SQLException {
        try {
            super.close();
        } finally {
            holder.close();
        }
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

}