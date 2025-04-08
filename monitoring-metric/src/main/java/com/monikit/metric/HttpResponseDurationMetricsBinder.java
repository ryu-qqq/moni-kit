package com.monikit.metric;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * HTTP 응답 시간을 기록하는 `MeterBinder`
 */

public class HttpResponseDurationMetricsBinder implements MeterBinder {

    private final ConcurrentMap<String, Timer> timerCache = new ConcurrentHashMap<>();
    private MeterRegistry meterRegistry;

    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;
    }

    /**
     * 동적으로 Timer를 기록하는 메서드
     */
    public void record(String path, int statusCode, long responseTime) {
        String timerKey = "http_response_duration|" + path + "|" + statusCode;

        Timer timer = timerCache.computeIfAbsent(timerKey, key ->
            Timer.builder("http_response_duration")
                .description("Time taken for HTTP responses")
                .tag("path", path)
                .tag("status", String.valueOf(statusCode))
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
        );

        timer.record(responseTime, TimeUnit.MILLISECONDS);
    }
}
