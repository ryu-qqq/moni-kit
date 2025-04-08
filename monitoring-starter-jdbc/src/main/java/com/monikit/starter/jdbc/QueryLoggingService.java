package com.monikit.starter.jdbc;


/**
 * SQL 쿼리 실행에 대한 로그를 기록하는 서비스 인터페이스.
 * <p>
 * 이 인터페이스는 SQL 실행에 대한 메타데이터를 받아, 이를 로깅하거나 외부 시스템으로 전달하는 역할을 수행한다.
 * 주로 {@link com.monikit.starter.jdbc.proxy.LoggingPreparedStatement} 내부에서 호출되어
 * 실제 SQL 실행 정보를 수집 및 기록하는 데 사용된다.
 * </p>
 *
 * <p>
 * 이 인터페이스는 {@code monikit-starter-jdbc}에서 기본 제공되는
 * {@link com.monikit.starter.jdbc.logging.DefaultQueryLoggingService} 구현체 외에도
 * 사용자가 커스텀 구현체를 주입하여 확장할 수 있다.
 * </p>
 *
 * <pre>{@code
 * queryLoggingService.logQuery(
 *     "trace-12345",
 *     "SELECT * FROM users WHERE id = ?",
 *     "[1]",
 *     35L,
 *     1
 * );
 * }</pre>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

public interface QueryLoggingService {

    /**
     * SQL 쿼리 실행 정보를 기록한다.
     *
     * @param traceId       요청의 추적 ID (예: MDC 기반 trace ID)
     * @param sql           실행된 SQL 쿼리
     * @param parameter     바인딩된 파라미터 목록 (예: [1, 'name', null])
     * @param executionTime SQL 실행 시간 (밀리초 단위)
     * @param rowsAffected  영향받은 행 수 (알 수 없는 경우 -1)
     */

    void logQuery(String traceId, String sql, String parameter, long executionTime, int rowsAffected);

}
