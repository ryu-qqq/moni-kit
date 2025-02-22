package com.monikit.starter;

import com.monikit.core.DataSourceProvider;
import com.monikit.core.DatabaseQueryLog;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogLevel;
import com.monikit.core.MetricCollector;
import com.monikit.core.QueryLoggingService;
import com.monikit.core.QueryPerformanceEvaluator;
import com.monikit.core.SqlParameterHolder;
import com.monikit.starter.config.MoniKitLoggingProperties;

/**
 * SQL 실행 로그를 기록하고, 메트릭을 수집하는 서비스 구현체.
 * <p>
 * - `LogEntryContextManager`를 사용하여 SQL 실행 정보를 저장
 * - SQL 실행 관련 메트릭을 수집
 * - 기존 static 방식 제거 후 Spring DI 방식 적용
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1
 */
public class DefaultQueryLoggingService implements QueryLoggingService {

    private final LogEntryContextManager logEntryContextManager;
    private final MetricCollector metricCollector;
    private final DataSourceProvider dataSourceProvider;
    private final MoniKitLoggingProperties moniKitLoggingProperties;

    public DefaultQueryLoggingService(
        LogEntryContextManager logEntryContextManager,
        MetricCollector metricCollector,
        DataSourceProvider dataSourceProvider,
        MoniKitLoggingProperties moniKitLoggingProperties
    ) {
        this.logEntryContextManager = logEntryContextManager;
        this.metricCollector = metricCollector;
        this.dataSourceProvider = dataSourceProvider;
        this.moniKitLoggingProperties = moniKitLoggingProperties;
    }

    /**
     * SQL 실행 정보를 받아서 로그 컨텍스트에 저장.
     *
     * @param sql 실행된 SQL 쿼리
     * @param executionTime 실행 시간 (ms)
     * @param rowsAffected 영향을 받은 행 개수
     */
    @Override
    public void logQuery(String sql, long executionTime, int rowsAffected) {
        String traceId = TraceIdProvider.getTraceId();
        String parametersStr = safeGetCurrentParameters(sql);
        String dataSourceName = dataSourceProvider.getDataSourceName();

        LogLevel logLevel = QueryPerformanceEvaluator.evaluate(
            executionTime,
            moniKitLoggingProperties.getSlowQueryThresholdMs(),
            moniKitLoggingProperties.getCriticalQueryThresholdMs()
        );

        DatabaseQueryLog logEntry = DatabaseQueryLog.create(
            traceId, sql, executionTime, dataSourceName,
            parametersStr, rowsAffected, -1, logLevel
        );

        logEntryContextManager.addLog(logEntry);
        metricCollector.recordQueryMetrics(sql, executionTime, dataSourceName);
    }

    /**
     * 안전하게 SQL 파라미터를 가져오는 메서드.
     * - `SqlParameterHolder.getCurrentParameters()`가 비어있는 경우 "No Parameters" 반환
     */
    private String safeGetCurrentParameters(String sql) {
        if (sql == null || sql.isEmpty()) {
            return "No Parameters";
        }
        return SqlParameterHolder.getCurrentParameters();
    }


}