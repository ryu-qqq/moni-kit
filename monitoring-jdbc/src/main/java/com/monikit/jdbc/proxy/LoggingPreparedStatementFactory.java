package com.monikit.jdbc.proxy;

import java.sql.PreparedStatement;

import com.monikit.core.TraceIdProvider;
import com.monikit.jdbc.QueryLoggingService;

/**
 * {@link LoggingPreparedStatement} 인스턴스를 생성하는 팩토리 클래스.
 *
 * <p>
 * 이 클래스는 {@link PreparedStatement}를 감싸는 {@code LoggingPreparedStatement}를 생성하여,
 * SQL 실행 시간, 실행된 SQL, 바인딩된 파라미터, 영향받은 행 수 등을 로깅할 수 있도록 구성한다.
 * 내부적으로 MDC에서 traceId를 추출하여 로깅 정보에 포함시킨다.
 * </p>
 *
 * <p>
 * Spring DI 환경에서 {@link QueryLoggingService}, {@link TraceIdProvider} 를 주입받아 사용되며,
 * {@code LoggingConnection}을 통해 자동으로 사용된다.
 * </p>
 *
 * <pre>{@code
 * PreparedStatement ps = connection.prepareStatement("SELECT * FROM users WHERE id = ?");
 * LoggingPreparedStatement loggingPs = factory.create(ps, "SELECT * FROM users WHERE id = ?");
 * }</pre>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */


public class LoggingPreparedStatementFactory {

    private final QueryLoggingService queryLoggingService;
    private final TraceIdProvider traceIdProvider;

    /**
     * {@link LoggingPreparedStatementFactory} 생성자
     *
     * @param queryLoggingService SQL 실행 정보를 기록할 서비스
     * @param traceIdProvider TRACE ID 를 추출할 프로바이더
     */

    public LoggingPreparedStatementFactory(QueryLoggingService queryLoggingService, TraceIdProvider traceIdProvider) {
        this.queryLoggingService = queryLoggingService;
        this.traceIdProvider = traceIdProvider;
    }

    /**
     * 주어진 {@link PreparedStatement}를 감싸는 {@link LoggingPreparedStatement}를 생성한다.
     *
     * @param delegate 원본 PreparedStatement
     * @param sql      실행할 SQL 문
     * @return 로깅 기능이 포함된 PreparedStatement 프록시
     */

    public LoggingPreparedStatement create(PreparedStatement delegate, String sql) {
        SqlParameterHolder holder = new SqlParameterHolder();
        String traceId = traceIdProvider.getTraceId();
        return new LoggingPreparedStatement(delegate, traceId, sql, holder, queryLoggingService);
    }

}
