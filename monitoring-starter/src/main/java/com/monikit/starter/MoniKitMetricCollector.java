package com.monikit.starter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.monikit.core.MetricCollector;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Tags;

/**
 * Prometheus 기반의 메트릭 수집기.
 * <p>
 * - HTTP 요청 횟수 및 실행 시간을 수집하여 Prometheus로 전송.
 * - SQL 쿼리 실행 횟수 및 실행 시간을 수집하여 Prometheus로 전송.
 * </p>
 */
public class MoniKitMetricCollector implements MetricCollector {
    private static final Logger logger = LoggerFactory.getLogger(MoniKitMetricCollector.class);

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Timer> timerCache = new ConcurrentHashMap<>();

    public MoniKitMetricCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void recordHttpRequest(String method, String uri, int statusCode, long duration) {
        String normalizedUri = HttpRequestMetricUtils.normalizeUri(uri);

        meterRegistry.counter("http_requests_total",
                Tags.of("method", method, "uri", normalizedUri, "status", String.valueOf(statusCode)))
            .increment();

        String timerKey = String.join("::", method, normalizedUri);
        Timer timer = timerCache.computeIfAbsent(timerKey, key ->
            Timer.builder("http_request_duration")
                .description("HTTP request processing time")
                .tags("method", method, "uri", normalizedUri) // ✅ .tags() 사용
                .register(meterRegistry)
        );

        timer.record(duration, TimeUnit.MILLISECONDS);
    }

    @Override
    public void recordQueryMetrics(String sql, long executionTime, String dataSourceName) {
        String queryCategory = QueryMetricUtils.categorizeQuery(sql);

        meterRegistry.counter("sql_query_total",
                Tags.of("query_category", queryCategory, "datasource", dataSourceName))
            .increment();

        String timerKey = String.join("::", queryCategory, dataSourceName);

        Timer timer = timerCache.computeIfAbsent(timerKey, key ->
            Timer.builder("sql_query_duration")
                .description("SQL query execution time")
                .tags("query_category", queryCategory, "datasource", dataSourceName) // ✅ .tags() 사용
                .register(meterRegistry)
        );

        timer.record(executionTime, TimeUnit.MILLISECONDS);

        if (executionTime > 2000) {
            logger.warn("Slow Query Detected! [Execution Time: {} ms] SQL: {}", executionTime, sql);
        }

        if (ThreadLocalRandom.current().nextInt(100) < 10) { // 10% 확률
            logger.info("Sampled Query Log: {}", sql);
        }
    }

}
