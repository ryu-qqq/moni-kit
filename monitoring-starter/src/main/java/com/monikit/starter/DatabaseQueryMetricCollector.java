package com.monikit.starter;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.monikit.core.DatabaseQueryLog;
import com.monikit.core.LogType;
import com.monikit.core.MetricCollector;
import com.monikit.starter.config.MoniKitMetricsProperties;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;

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
    private final SqlQueryCountMetricsBinder countMetricsBinder;
    private final SqlQueryDurationMetricsBinder durationMetricsBinder;

    public DatabaseQueryMetricCollector(
        MoniKitMetricsProperties metricsProperties,
        SqlQueryCountMetricsBinder countMetricsBinder,
        SqlQueryDurationMetricsBinder durationMetricsBinder) {
        this.metricsProperties = metricsProperties;
        this.countMetricsBinder = countMetricsBinder;
        this.durationMetricsBinder = durationMetricsBinder;
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

        countMetricsBinder.increment();

        durationMetricsBinder.record(logEntry.getExecutionTime());
    }
}