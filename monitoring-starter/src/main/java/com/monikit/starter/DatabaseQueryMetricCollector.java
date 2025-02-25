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
 * @since 1.2
 */
public class DatabaseQueryMetricCollector implements MetricCollector<DatabaseQueryLog> {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseQueryMetricCollector.class);

    private final Counter sqlQueryCounter;
    private final Timer sqlQueryTimer;
    private final MoniKitMetricsProperties metricsProperties;

    public DatabaseQueryMetricCollector(Counter sqlQueryCounter, Timer sqlQueryTimer,
                                        MoniKitMetricsProperties metricsProperties) {
        this.sqlQueryCounter = sqlQueryCounter;
        this.sqlQueryTimer = sqlQueryTimer;
        this.metricsProperties = metricsProperties;
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

        String sql = logEntry.getQuery();
        long executionTime = logEntry.getExecutionTime();

        sqlQueryCounter.increment();
        sqlQueryTimer.record(executionTime, TimeUnit.MILLISECONDS);

        if (executionTime > metricsProperties.getSlowQueryThresholdMs()) {
            logger.warn("⚠️ Slow Query Detected! [Execution Time: {} ms] SQL: {}", executionTime, sql);
        }

        if (ThreadLocalRandom.current().nextInt(100) < metricsProperties.getQuerySamplingRate()) {
            logger.info("📌 Sampled Query Log: {}", sql);
        }

    }

}