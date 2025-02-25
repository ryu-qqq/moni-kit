package com.monikit.core;

/**
 * SQL 실행 로그를 기록하고, 메트릭을 수집하는 서비스 구현체.
 * <p>
 * - `LogEntryContextManager`를 사용하여 SQL 실행 정보를 저장
 * - SQL 실행 관련 메트릭을 수집
 * - 기존 static 방식 제거 후 Spring DI 방식 적용
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.1
 */

public class DefaultQueryLoggingService implements QueryLoggingService {

    private final LogEntryContextManager logEntryContextManager;
    private final DataSourceProvider dataSourceProvider;
    private final long slowQueryThresholdMs;
    private final long criticalQueryThresholdMs;

    public DefaultQueryLoggingService(LogEntryContextManager logEntryContextManager,
                                      DataSourceProvider dataSourceProvider, long slowQueryThresholdMs,
                                      long criticalQueryThresholdMs) {
        this.logEntryContextManager = logEntryContextManager;
        this.dataSourceProvider = dataSourceProvider;
        this.slowQueryThresholdMs = slowQueryThresholdMs;
        this.criticalQueryThresholdMs = criticalQueryThresholdMs;
    }

    /**
     * SQL 실행 정보를 받아서 로그 컨텍스트에 저장.
     *
     * @param sql 실행된 SQL 쿼리
     * @param executionTime 실행 시간 (ms)
     * @param rowsAffected 영향을 받은 행 개수
     */
    @Override
    public void logQuery(String traceId, String sql, String parameter, long executionTime, int rowsAffected) {
        String dataSourceName = dataSourceProvider.getDataSourceName();

        LogLevel logLevel = QueryPerformanceEvaluator.evaluate(
            executionTime,
            slowQueryThresholdMs,
            criticalQueryThresholdMs
        );

        DatabaseQueryLog logEntry = DatabaseQueryLog.create(
            traceId, sql, executionTime, dataSourceName,
            parameter, rowsAffected, -1, logLevel
        );

        logEntryContextManager.addLog(logEntry);
    }

}