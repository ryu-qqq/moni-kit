package com.monikit.starter.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DelegatingDataSource;

import com.monikit.starter.jdbc.proxy.LoggingPreparedStatementFactory;

/**
 * SQL 실행을 감시하는 프록시 {@link DataSource} 구현체.
 * <p>
 * 이 클래스는 내부적으로 실제 {@link DataSource}를 감싸고,
 * 반환되는 {@link Connection}을 {@link LoggingConnection}으로 래핑하여
 * 모든 {@code prepareStatement()} 호출 시 {@code LoggingPreparedStatement}를 생성하도록 한다.
 * </p>
 *
 * <p>
 * {@link LoggingPreparedStatementFactory}를 통해 생성된 {@code LoggingPreparedStatement}는
 * SQL 실행 시간, 파라미터, 영향을 받은 행 수 등을 기록하는 기능을 제공하며,
 * {@link QueryLoggingService}를 통해 로그 수집 및 메트릭 전송을 수행할 수 있다.
 * </p>
 *
 * <pre>{@code
 * DataSource original = ...;
 * DataSource loggingDataSource = new LoggingDataSource(original, factory);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 1.1.0
 * @see LoggingConnection
 * @see com.monikit.starter.jdbc.proxy.LoggingPreparedStatement
 * @see com.monikit.starter.jdbc.QueryLoggingService
 */

public class LoggingDataSource extends DelegatingDataSource {

    private final LoggingPreparedStatementFactory preparedStatementFactory;

    /**
     * 새로운 LoggingDataSource 인스턴스를 생성한다.
     *
     * @param targetDataSource          원본 {@link DataSource}
     * @param preparedStatementFactory  {@link LoggingPreparedStatementFactory} 인스턴스
     */
    public LoggingDataSource(DataSource targetDataSource, LoggingPreparedStatementFactory preparedStatementFactory) {
        super(targetDataSource);
        this.preparedStatementFactory = preparedStatementFactory;
    }

    /**
     * {@link LoggingConnection}으로 래핑된 {@link Connection}을 반환한다.
     *
     * @return SQL 로깅 기능이 포함된 {@link Connection}
     * @throws SQLException 커넥션 생성 실패 시
     */
    @Override
    public Connection getConnection() throws SQLException {
        return new LoggingConnection(super.getConnection(), preparedStatementFactory);
    }

    /**
     * {@link LoggingConnection}으로 래핑된 {@link Connection}을 반환한다.
     *
     * @param username 사용자 이름
     * @param password 비밀번호
     * @return SQL 로깅 기능이 포함된 {@link Connection}
     * @throws SQLException 커넥션 생성 실패 시
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return new LoggingConnection(super.getConnection(username, password), preparedStatementFactory);
    }


}