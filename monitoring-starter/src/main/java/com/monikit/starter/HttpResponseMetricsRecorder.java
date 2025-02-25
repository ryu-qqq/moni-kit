package com.monikit.starter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

/**
 * `path` 및 `statusCode` 별로 HTTP 응답 횟수를 카운트하는 유틸리티 클래스.
 * <p>
 * - INBOUND_RESPONSE (서버로 들어오는 응답)
 * - OUTBOUND_RESPONSE (외부 API 응답)
 * 두 가지 모두에서 재사용 가능.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.3
 */
@Component
public class HttpResponseMetricsRecorder {

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Timer> timerCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> counterCache = new ConcurrentHashMap<>();

    public HttpResponseMetricsRecorder(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * HTTP 응답 횟수를 기록하고, 응답 시간을 측정하는 메서드.
     *
     * @param metricName   메트릭 이름 (예: http_outbound_response_count)
     * @param path         요청 URL (정규화된 값)
     * @param statusCode   HTTP 상태 코드
     * @param responseTime 응답 시간 (ms)
     */
    public void record(String metricName, String path, int statusCode, long responseTime) {
        String normalizedPath = HttpRequestMetricUtils.normalizeUri(path);
        String counterKey = metricName + "|" + normalizedPath + "|" + statusCode;
        String timerKey = metricName.replace("_count", "_duration") + "|" + normalizedPath + "|" + statusCode;

        Counter counter = counterCache.computeIfAbsent(counterKey, key ->
            Counter.builder(metricName)
                .description("Total number of HTTP responses")
                .tag("path", normalizedPath)
                .tag("status", String.valueOf(statusCode))
                .register(meterRegistry)
        );
        counter.increment();

        Timer timer = timerCache.computeIfAbsent(timerKey, key ->
            Timer.builder(metricName.replace("_count", "_duration"))
                .description("Time taken for HTTP responses")
                .tag("path", normalizedPath)
                .tag("status", String.valueOf(statusCode))
                .register(meterRegistry)
        );

        timer.record(responseTime, TimeUnit.MILLISECONDS);
    }

}