package com.monikit.starter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * HTTP 응답 횟수를 메트릭으로 기록하는 `MeterBinder`
 */
public class HttpResponseCountMetricsBinder implements MeterBinder {

    private final ConcurrentMap<String, Counter> counterCache = new ConcurrentHashMap<>();
    private MeterRegistry meterRegistry;

    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;

        // 기본적인 카운터를 미리 등록 (예시)
        registerCounter("/default", "200");
    }

    private void registerCounter(String path, String status) {
        String counterKey = "http_response_count|" + path + "|" + status;

        counterCache.computeIfAbsent(counterKey, key ->
            Counter.builder("http_response_count")
                .description("Total number of HTTP responses")
                .tag("path", path)
                .tag("status", status)
                .register(meterRegistry)
        );
    }

    /**
     * 동적으로 Counter를 기록하는 메서드
     */
    public void increment(String path, int statusCode) {
        String counterKey = "http_response_count|" + path + "|" + statusCode;

        Counter counter = counterCache.computeIfAbsent(counterKey, key ->
            Counter.builder("http_response_count")
                .description("Total number of HTTP responses")
                .tag("path", path)
                .tag("status", String.valueOf(statusCode))
                .register(meterRegistry)
        );

        counter.increment();
    }
}
