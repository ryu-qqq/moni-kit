package com.monikit.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import com.monikit.jdbc.proxy.LoggingPreparedStatement;
import com.monikit.jdbc.proxy.LoggingPreparedStatementFactory;

/**
 * {@link Connection} 인터페이스를 감싸 SQL 실행을 로깅하는 프록시 클래스.
 * <p>
 * {@link #prepareStatement(String)} 및 다양한 오버로드 메서드를 감지하여
 * 내부적으로 {@code LoggingPreparedStatement}를 생성하도록 위임한다.
 * </p>
 *
 * <p>
 * {@link LoggingPreparedStatementFactory}를 통해 {@code PreparedStatement}를 생성할 때,
 * SQL 실행 시간, 파라미터, 실행 결과 등을 {@link QueryLoggingService}에 전달하여
 * 로깅 및 메트릭 수집을 수행할 수 있도록 구성된다.
 * </p>
 *
 * <p>
 * {@code Statement}, {@code CallableStatement} 등은 감시 대상이 아니며,
 * 직접적인 래핑 없이 원본 객체를 반환한다.
 * </p>
 *
 * <pre>{@code
 * Connection original = dataSource.getConnection();
 * Connection logging = new LoggingConnection(original, factory);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 1.1.0
 * @see LoggingDataSource
 * @see LoggingPreparedStatement
 * @see QueryLoggingService
 */

public class LoggingConnection implements Connection {

    private final Connection delegate;
    private final LoggingPreparedStatementFactory loggingPreparedStatementFactory;

    /**
     * 원본 {@link Connection}과 SQL 로깅을 위한 팩토리를 받아 래핑된 {@code LoggingConnection}을 생성한다.
     *
     * @param delegate 원본 {@link Connection} 인스턴스
     * @param loggingPreparedStatementFactory {@link LoggingPreparedStatement} 생성을 위한 팩토리
     */

    public LoggingConnection(Connection delegate, LoggingPreparedStatementFactory loggingPreparedStatementFactory) {
        this.delegate = delegate;
        this.loggingPreparedStatementFactory = loggingPreparedStatementFactory;
    }

    /**
     * {@link LoggingPreparedStatement}로 감싼 {@link PreparedStatement}를 반환한다.
     *
     * @param sql 실행할 SQL 쿼리
     * @return 로깅 기능이 포함된 {@link PreparedStatement}
     * @throws SQLException SQL 예외
     */

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return loggingPreparedStatementFactory.create(delegate.prepareStatement(sql), sql);
    }

    /**
     * 기타 메서드들은 모두 위임(delegate) 방식으로 원본 {@link Connection}에 전달된다.
     * SQL 실행 감시가 필요한 경우 {@link PreparedStatement} 관련 메서드만 래핑된다.
     */

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return delegate.prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return delegate.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        delegate.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return delegate.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        delegate.commit();
    }

    @Override
    public void rollback() throws SQLException {
        delegate.rollback();
    }

    @Override
    public Statement createStatement() throws SQLException {
        return delegate.createStatement();
    }

    @Override
    public void close() throws SQLException {
        delegate.close();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        delegate.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return delegate.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        delegate.setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
        return delegate.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        delegate.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return delegate.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return delegate.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws
        SQLException {
        return loggingPreparedStatementFactory.create(delegate.prepareStatement(sql), sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
                                              int resultSetHoldability) throws SQLException {
        PreparedStatement statement = delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        return loggingPreparedStatementFactory.create(statement, sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        PreparedStatement statement = delegate.prepareStatement(sql, autoGeneratedKeys);
        return loggingPreparedStatementFactory.create(statement, sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        PreparedStatement statement = delegate.prepareStatement(sql, columnIndexes);
        return loggingPreparedStatementFactory.create(statement, sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        PreparedStatement statement = delegate.prepareStatement(sql, columnNames);
        return loggingPreparedStatementFactory.create(statement, sql);
    }


    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return delegate.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        delegate.setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        delegate.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return delegate.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return delegate.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return delegate.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        delegate.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        delegate.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws
        SQLException {
        return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }



    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }



    @Override
    public Clob createClob() throws SQLException {
        return delegate.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return delegate.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return delegate.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return delegate.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return delegate.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        delegate.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        delegate.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return delegate.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return delegate.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return delegate.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return delegate.createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        delegate.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return delegate.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        delegate.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        delegate.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return delegate.getNetworkTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

}