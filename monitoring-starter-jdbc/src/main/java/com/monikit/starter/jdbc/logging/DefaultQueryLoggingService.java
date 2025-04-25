package com.monikit.starter.jdbc.logging;

import com.monikit.starter.jdbc.DataSourceProvider;
import com.monikit.core.model.DatabaseQueryLog;
import com.monikit.core.context.LogEntryContextManager;
import com.monikit.core.LogLevel;
import com.monikit.starter.jdbc.QueryLoggingService;

/**
 * {@link QueryLoggingService}의 기본 구현체.
 *
 * <p>
 * SQL 실행 결과를 수집하여 {@link DatabaseQueryLog} 형태로 구성하고,
 * {@link LogEntryContextManager}를 통해 로그 컨텍스트에 저장한다.
 * </p>
 *
 * <p>
 * 쿼리 실행 시간에 따라 로그 레벨을 결정하기 위해 {@link QueryPerformanceEvaluator}를 사용하며,
 * 설정된 임계값(slow, critical)에 따라 {@code INFO}, {@code WARN}, {@code ERROR} 레벨이 자동 적용된다.
 * </p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * QueryLoggingService loggingService = ...
 * loggingService.logQuery("trace-123", "SELECT * FROM users", "[1]", 1300, 10);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

public class DefaultQueryLoggingService implements QueryLoggingService {

    private final LogEntryContextManager logEntryContextManager;
    private final DataSourceProvider dataSourceProvider;
    private final long slowQueryThresholdMs;
    private final long criticalQueryThresholdMs;


    /**
     * 생성자
     *
     * @param logEntryContextManager 쿼리 로그를 컨텍스트에 기록하는 매니저
     * @param dataSourceProvider     현재 데이터소스 이름을 제공하는 프로바이더
     * @param slowQueryThresholdMs   WARN 로그로 판단할 슬로우 쿼리 임계값
     * @param criticalQueryThresholdMs ERROR 로그로 판단할 크리티컬 쿼리 임계값
     */

    public DefaultQueryLoggingService(LogEntryContextManager logEntryContextManager,
                                      DataSourceProvider dataSourceProvider, long slowQueryThresholdMs,
                                      long criticalQueryThresholdMs) {
        this.logEntryContextManager = logEntryContextManager;
        this.dataSourceProvider = dataSourceProvider;
        this.slowQueryThresholdMs = slowQueryThresholdMs;
        this.criticalQueryThresholdMs = criticalQueryThresholdMs;
    }

    /**
     * 쿼리 실행 정보를 로그 컨텍스트에 기록한다.
     *
     * @param traceId       트레이스 ID (MDC 등에서 추출된 값)
     * @param sql           실행된 SQL 쿼리
     * @param parameter     바인딩된 파라미터 정보
     * @param executionTime 실행 시간 (ms)
     * @param rowsAffected  영향 받은 행 개수 (없을 경우 -1)
     */

    @Override
    public void logQuery(String traceId, String sql, String parameter, long executionTime, int rowsAffected) {
        String dataSourceName = dataSourceProvider.getDataSourceName();

        LogLevel logLevel = QueryPerformanceEvaluator.evaluate(
            executionTime,
            slowQueryThresholdMs,
            criticalQueryThresholdMs
        );

        DatabaseQueryLog logEntry = DatabaseQueryLog.of(
            traceId, sql, executionTime, dataSourceName,
            parameter, rowsAffected, -1, logLevel
        );

        logEntryContextManager.addLog(logEntry);
    }

}