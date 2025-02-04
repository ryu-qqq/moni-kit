package com.monikit.core;

/**
 * SQL 실행 로그를 기록하고, 메트릭을 수집하는 서비스.
 * <p>
 * - PreparedStatement 실행 정보를 받아 `LogEntryContextManager`에 저장
 * - SQL 실행 관련 메트릭을 수집
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class QueryLoggingService {

    private final MetricCollector metricCollector; // 메트릭 수집기

    public QueryLoggingService(MetricCollector metricCollector) {
        this.metricCollector = metricCollector;
    }

    /**
     * SQL 실행 정보를 받아서 로그 컨텍스트에 저장.
     *
     * @param sql 실행된 SQL 쿼리
     * @param executionTime 실행 시간 (ms)
     * @param rowsAffected 영향을 받은 행 개수
     */
    public void logQuery(String sql, long executionTime, int rowsAffected) {
        String traceId = TraceIdProvider.currentTraceId();
        String parametersStr = SqlParameterHolder.getCurrentParameters();
        String dataSourceName = DataSourceProvider.currentDataSourceName();

        LogLevel logLevel = QueryPerformanceEvaluator.evaluate(
            executionTime,
            SqlLoggingPropertiesHolder.getSlowQueryThresholdMs(),
            SqlLoggingPropertiesHolder.getCriticalQueryThresholdMs()
        );

        DatabaseQueryLog logEntry = DatabaseQueryLog.create(
            traceId, sql, executionTime, dataSourceName,
            parametersStr, rowsAffected, -1, logLevel
        );

        LogEntryContextManager.addLog(logEntry);
        metricCollector.recordQueryMetrics(sql, executionTime, dataSourceName);

    }
}