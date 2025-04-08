package com.monikit.metric;

import com.monikit.config.MoniKitMetricsProperties;
import com.monikit.core.DatabaseQueryLog;
import com.monikit.core.LogType;
import com.monikit.core.MetricCollector;

/**
 * 기본 SQL 쿼리 실행 메트릭을 수집하는 구현체.
 * <p>
 * - SQL 쿼리 실행 횟수(`sql_query_total`) 및 실행 시간(`sql_query_duration`)을 기록.
 * - 설정(`monikit.metrics.enabled` & `monikit.metrics.query.enabled`)이 `true`일 때만 자동 등록됨.
 * - 슬로우 쿼리 감지 및 샘플링 로깅 기능 제공.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1
 */

public class DatabaseQueryMetricCollector implements MetricCollector<DatabaseQueryLog> {

    private final MoniKitMetricsProperties metricsProperties;
    private final QueryMetricsRecorder queryMetricsRecorder;

    public DatabaseQueryMetricCollector(MoniKitMetricsProperties metricsProperties,
                                        QueryMetricsRecorder queryMetricsRecorder) {
        this.metricsProperties = metricsProperties;
        this.queryMetricsRecorder = queryMetricsRecorder;
    }

    @Override
    public boolean supports(LogType logType) {
        return logType == LogType.DATABASE_QUERY;
    }

    @Override
    public void record(DatabaseQueryLog logEntry) {
        if (!metricsProperties.isMetricsEnabled() || !metricsProperties.isQueryMetricsEnabled()) {
            return;
        }

        String sql = QueryMetricUtils.categorizeQuery(logEntry.getQuery());
        String dataSource = logEntry.getDataSource();
        long executionTime = logEntry.getExecutionTime();

        queryMetricsRecorder.record(sql, dataSource, executionTime);
    }
}