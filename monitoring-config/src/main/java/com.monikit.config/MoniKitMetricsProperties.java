package com.monikit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MoniKit의 메트릭 수집 기능에 대한 설정을 정의하는 구성 클래스.
 * <p>
 * 이 클래스는 다음과 같은 메트릭 설정을 외부 설정 파일(application.yml/properties)과 바인딩합니다:
 * </p>
 *
 * <ul>
 *     <li><b>metricsEnabled</b> : 전체 메트릭 수집 기능의 마스터 스위치</li>
 *     <li><b>queryMetricsEnabled</b> : SQL 쿼리 실행 시간, 횟수 등의 메트릭 수집 여부</li>
 *     <li><b>httpMetricsEnabled</b> : HTTP 요청/응답 관련 메트릭 수집 여부</li>
 *     <li><b>slowQueryThresholdMs</b> : 느린 쿼리로 간주할 기준 시간 (ms)</li>
 *     <li><b>querySamplingRate</b> : SQL 메트릭 수집 시 샘플링 비율 (%)</li>
 * </ul>
 *
 * <p><b>설정 접두사</b>: <code>monikit.metrics</code></p>
 *
 * 예시:
 * <pre>
 * monikit.metrics:
 *   metrics-enabled: true
 *   query-metrics-enabled: true
 *   http-metrics-enabled: false
 *   slow-query-threshold-ms: 1500
 *   query-sampling-rate: 10
 * </pre>
 *
 * <p>
 * 모든 설정은 Spring Boot의 <code>@ConfigurationProperties</code> 기능을 통해 자동으로 바인딩됩니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */


@Configuration
@ConfigurationProperties(prefix = "monikit.metrics")
public class MoniKitMetricsProperties {

    private boolean metricsEnabled = true;
    private boolean queryMetricsEnabled = true;
    private boolean httpMetricsEnabled = true;

    private long slowQueryThresholdMs = 2000;
    private int querySamplingRate = 10;


    public boolean isMetricsEnabled() { return metricsEnabled; }
    public void setMetricsEnabled(boolean metricsEnabled) { this.metricsEnabled = metricsEnabled; }

    public boolean isQueryMetricsEnabled() { return queryMetricsEnabled; }
    public void setQueryMetricsEnabled(boolean queryMetricsEnabled) { this.queryMetricsEnabled = queryMetricsEnabled; }

    public boolean isHttpMetricsEnabled() { return httpMetricsEnabled; }
    public void setHttpMetricsEnabled(boolean httpMetricsEnabled) { this.httpMetricsEnabled = httpMetricsEnabled; }

    public long getSlowQueryThresholdMs() { return slowQueryThresholdMs; }
    public void setSlowQueryThresholdMs(long slowQueryThresholdMs) { this.slowQueryThresholdMs = slowQueryThresholdMs; }

    public int getQuerySamplingRate() { return querySamplingRate; }
    public void setQuerySamplingRate(int querySamplingRate) { this.querySamplingRate = querySamplingRate; }

}
