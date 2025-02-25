package com.monikit.starter;

import io.micrometer.core.instrument.Timer;

import java.util.concurrent.TimeUnit;

import com.monikit.core.HttpInboundResponseLog;
import com.monikit.core.LogType;
import com.monikit.core.MetricCollector;
import com.monikit.starter.config.MoniKitMetricsProperties;

/**
 * 서버에서 나가는 HTTP 응답 메트릭을 수집하는 기본 구현체.
 * <p>
 * - 응답 횟수(`http_inbound_response_total`) 및 응답 시간(`http_inbound_response_duration`)을 기록.
 * - 설정(`monikit.metrics.http.enabled`)이 `true`일 때만 자동 등록됨.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.3
 */
public class HttpInboundResponseMetricCollector implements MetricCollector<HttpInboundResponseLog> {

    private final MoniKitMetricsProperties metricsProperties;
    private final HttpResponseMetricsRecorder httpResponseMetricsRecorder;

    public HttpInboundResponseMetricCollector(
        MoniKitMetricsProperties metricsProperties,
        HttpResponseMetricsRecorder httpResponseMetricsRecorder) {
        this.metricsProperties = metricsProperties;
        this.httpResponseMetricsRecorder = httpResponseMetricsRecorder;
    }

    @Override
    public boolean supports(LogType logType) {
        return logType == LogType.INBOUND_RESPONSE;
    }

    @Override
    public void record(HttpInboundResponseLog logEntry) {
        if (!metricsProperties.isMetricsEnabled() || !metricsProperties.isHttpMetricsEnabled()) {
            return;
        }

        String path = logEntry.getRequestUri();
        int statusCode = logEntry.getStatusCode();
        long responseTime = logEntry.getExecutionTime();

        httpResponseMetricsRecorder.record("http_inbound_response_count", path, statusCode, responseTime);

    }
}