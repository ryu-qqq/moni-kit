package com.monikit.metric;

import com.monikit.config.MoniKitMetricsProperties;
import com.monikit.core.HttpOutboundResponseLog;
import com.monikit.core.LogType;
import com.monikit.core.MetricCollector;

/**
 * 외부 API로 나가는 HTTP 응답 메트릭을 수집하는 기본 구현체.
 * <p>
 * - `http_outbound_response_total`, `http_outbound_response_duration`을 기록.
 * - `path` 및 `statusCode`별 응답 횟수를 `ResponseStatusCounter`를 통해 기록.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.1
 */

public class HttpOutboundResponseMetricCollector implements MetricCollector<HttpOutboundResponseLog> {

    private final MoniKitMetricsProperties metricsProperties;
    private final HttpResponseMetricsRecorder httpResponseMetricsRecorder;

    public HttpOutboundResponseMetricCollector(
        MoniKitMetricsProperties metricsProperties,
        HttpResponseMetricsRecorder httpResponseMetricsRecorder) {
        this.metricsProperties = metricsProperties;
        this.httpResponseMetricsRecorder = httpResponseMetricsRecorder;
    }

    @Override
    public boolean supports(LogType logType) {
        return logType == LogType.OUTBOUND_RESPONSE;
    }

    @Override
    public void record(HttpOutboundResponseLog logEntry) {
        if (!metricsProperties.isMetricsEnabled()) {
            return;
        }

        String path = logEntry.getUri();
        int statusCode = logEntry.getStatusCode();
        long responseTime = logEntry.getExecutionTime();

        httpResponseMetricsRecorder.record(path, statusCode, responseTime);

    }

}