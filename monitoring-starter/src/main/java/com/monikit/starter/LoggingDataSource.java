package com.monikit.starter;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DelegatingDataSource;

/**
 * SQL 실행을 감시하는 `DataSource` 프록시.
 * <p>
 * 모든 JDBC Connection을 감싸서 SQL 실행을 추적할 수 있도록 함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public class LoggingDataSource extends DelegatingDataSource {

    private final LoggingPreparedStatementFactory preparedStatementFactory;

    public LoggingDataSource(DataSource targetDataSource, LoggingPreparedStatementFactory preparedStatementFactory) {
        super(targetDataSource);
        this.preparedStatementFactory = preparedStatementFactory;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new LoggingConnection(super.getConnection(), preparedStatementFactory);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return new LoggingConnection(super.getConnection(username, password), preparedStatementFactory);
    }

}