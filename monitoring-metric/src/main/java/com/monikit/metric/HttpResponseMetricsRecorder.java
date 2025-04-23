package com.monikit.metric;

/**
 * HTTP 응답 메트릭을 관리하는 클래스.
 * `MeterBinder`를 구현하여 Micrometer에 자동 등록되도록 설정.
 * `@Component`를 추가하여 Spring 빈으로 등록.
 *
 * @author ryu-qqq
 * @since 1.0.1
 */

public class HttpResponseMetricsRecorder {

    private final HttpResponseCountMetricsBinder countMetricsBinder;
    private final HttpResponseDurationMetricsBinder durationMetricsBinder;

    public HttpResponseMetricsRecorder(
        HttpResponseCountMetricsBinder countMetricsBinder,
        HttpResponseDurationMetricsBinder durationMetricsBinder) {
        this.countMetricsBinder = countMetricsBinder;
        this.durationMetricsBinder = durationMetricsBinder;
    }

    public void record(String path, int statusCode, long responseTime) {
        countMetricsBinder.increment(path, statusCode);
        durationMetricsBinder.record(path, statusCode, responseTime);
    }

}