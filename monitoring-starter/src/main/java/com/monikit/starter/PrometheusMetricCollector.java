package com.monikit.starter;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.monikit.core.MetricCollector;

/**
 * Prometheus 기반의 메트릭 수집기.
 * <p>
 * - HTTP 요청 횟수 및 실행 시간을 수집하여 Prometheus로 전송.
 * - SQL 쿼리 실행 횟수 및 실행 시간을 수집하여 Prometheus로 전송.
 * </p>
 */
@Component
public class PrometheusMetricCollector implements MetricCollector {

    private final MeterRegistry meterRegistry;

    public PrometheusMetricCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void recordHttpRequest(String method, String uri, int statusCode, long duration) {
        meterRegistry.counter("http_requests_total",
                "method", method,
                "uri", uri,
                "status", String.valueOf(statusCode))
            .increment();

        Timer.builder("http_request_duration")
            .description("HTTP request processing time")
            .tag("method", method)
            .tag("uri", uri)
            .register(meterRegistry)
            .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    @Override
    public void recordQueryMetrics(String sql, long executionTime, String dataSourceName) {
        meterRegistry.counter("sql_query_total",
                "query", sql,
                "datasource", dataSourceName)
            .increment();

        Timer.builder("sql_query_duration")
            .description("SQL query execution time")
            .tag("query", sql)
            .tag("datasource", dataSourceName)
            .register(meterRegistry)
            .record(executionTime, TimeUnit.MILLISECONDS);
    }

}
