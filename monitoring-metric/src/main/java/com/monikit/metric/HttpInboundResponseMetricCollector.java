package com.monikit.metric;

import com.monikit.config.MoniKitMetricsProperties;
import com.monikit.core.HttpInboundResponseLog;
import com.monikit.core.LogType;
import com.monikit.core.MetricCollector;

/**
 * 서버에서 나가는 HTTP 응답 메트릭을 수집하는 기본 구현체.
 * <p>
 * - 응답 횟수(`http_inbound_response_total`) 및 응답 시간(`http_inbound_response_duration`)을 기록.
 * - 설정(`monikit.metrics.http.enabled`)이 `true`일 때만 자동 등록됨.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0.1
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

        String path = logEntry.getUri();
        int statusCode = logEntry.getStatusCode();
        long responseTime = logEntry.getExecutionTime();

        httpResponseMetricsRecorder.record(path, statusCode, responseTime);

    }
}