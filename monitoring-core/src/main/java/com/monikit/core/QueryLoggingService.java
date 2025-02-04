package com.monikit.core;

/**
 * SQL 실행 로그를 기록하는 서비스.
 * <p>
 * PreparedStatement의 실행 결과를 받아서 로그 컨텍스트에 저장.
 * 추후 로깅 방식이 변경될 경우 이 클래스만 수정하면 됨.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class QueryLoggingService {

    private QueryLoggingService() {
    }

    /**
     * SQL 실행 정보를 받아서 로그 컨텍스트에 저장.
     *
     * @param sql 실행된 SQL 쿼리
     * @param executionTime 실행 시간 (ms)
     * @param rowsAffected 영향을 받은 행 개수
     */
    static void logQuery(String sql, long executionTime, int rowsAffected) {
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
    }
}