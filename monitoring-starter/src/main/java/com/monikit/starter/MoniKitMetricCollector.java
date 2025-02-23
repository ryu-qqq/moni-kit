package com.monikit.starter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.monikit.core.MetricCollector;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

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
                "method", method,
                "uri", normalizedUri,
                "status", String.valueOf(statusCode))
            .increment();


        String timerKey = method + "|" + normalizedUri;
        Timer timer = timerCache.computeIfAbsent(timerKey, key ->
            Timer.builder("http_request_duration")
                .description("HTTP request processing time")
                .tag("method", method)
                .tag("uri", normalizedUri)
                .register(meterRegistry)
        );


        timer.record(duration, TimeUnit.MILLISECONDS);
    }

    @Override
    public void recordQueryMetrics(String sql, long executionTime, String dataSourceName) {
        String queryCategory = QueryMetricUtils.categorizeQuery(sql);

        // 1. 기본적인 SQL 실행 횟수 및 평균 실행 시간 기록
        meterRegistry.counter("sql_query_total",
                "query_category", queryCategory,
                "datasource", dataSourceName)
            .increment();

        // ✅ Timer 캐싱 적용
        String timerKey = queryCategory + "|" + dataSourceName;
        Timer timer = timerCache.computeIfAbsent(timerKey, key ->
            Timer.builder("sql_query_duration")
                .description("SQL query execution time")
                .tag("query_category", queryCategory)
                .tag("datasource", dataSourceName)
                .register(meterRegistry)
        );

        // 3. Timer 기록
        timer.record(executionTime, TimeUnit.MILLISECONDS);

        // 느린 쿼리 로그 기록
        if (executionTime > 500) {
            logger.warn("Slow Query Detected! [Execution Time: {} ms] SQL: {}", executionTime, sql);
        }

        // 랜덤 샘플링하여 일부 쿼리 로그 출력
        if (Math.random() < 0.1) {
            logger.info("Sampled Query Log: {}", sql);
        }
    }
}