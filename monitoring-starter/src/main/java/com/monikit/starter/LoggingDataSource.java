package com.monikit.starter;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DelegatingDataSource;

import com.monikit.core.QueryLoggingService;

/**
 * SQL 실행을 감시하는 `DataSource` 프록시.
 * <p>
 * 모든 JDBC Connection을 감싸서 SQL 실행을 추적할 수 있도록 함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class LoggingDataSource extends DelegatingDataSource {

    private final QueryLoggingService queryLoggingService;

    public LoggingDataSource(DataSource targetDataSource, QueryLoggingService queryLoggingService) {
        super(targetDataSource);
        this.queryLoggingService = queryLoggingService;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new LoggingConnection(super.getConnection(), queryLoggingService);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return new LoggingConnection(super.getConnection(username, password), queryLoggingService);
    }
}