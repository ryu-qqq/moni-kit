package com.monikit.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MoniKit 메트릭 수집 설정을 관리하는 클래스.
 * <p>
 * - 전체 메트릭 활성화 여부 (`metricsEnabled`).
 * - 개별 메트릭 활성화 설정 (`queryMetricsEnabled`, `httpMetricsEnabled`, `customMetricsEnabled`, `externalMallMetricsEnabled`).
 * - SQL 쿼리 관련 설정 (슬로우 쿼리 감지 임계값, 샘플링 비율).
 * - 외부몰 요청 메트릭 (응답 시간 & 응답 코드) 수집 활성화.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.3
 */
@Configuration
@ConfigurationProperties(prefix = "monikit.metrics")
public class MoniKitMetricsProperties {

    private boolean metricsEnabled = true;
    private boolean queryMetricsEnabled = true;
    private boolean httpMetricsEnabled = true;
    private boolean externalMallMetricsEnabled = true;

    private long slowQueryThresholdMs = 2000;
    private int querySamplingRate = 10;


    public boolean isMetricsEnabled() { return metricsEnabled; }
    public void setMetricsEnabled(boolean metricsEnabled) { this.metricsEnabled = metricsEnabled; }

    public boolean isQueryMetricsEnabled() { return queryMetricsEnabled; }
    public void setQueryMetricsEnabled(boolean queryMetricsEnabled) { this.queryMetricsEnabled = queryMetricsEnabled; }

    public boolean isHttpMetricsEnabled() { return httpMetricsEnabled; }
    public void setHttpMetricsEnabled(boolean httpMetricsEnabled) { this.httpMetricsEnabled = httpMetricsEnabled; }

    public boolean isExternalMallMetricsEnabled() { return externalMallMetricsEnabled; }
    public void setExternalMallMetricsEnabled(boolean externalMallMetricsEnabled) { this.externalMallMetricsEnabled = externalMallMetricsEnabled; }

    public long getSlowQueryThresholdMs() { return slowQueryThresholdMs; }
    public void setSlowQueryThresholdMs(long slowQueryThresholdMs) { this.slowQueryThresholdMs = slowQueryThresholdMs; }

    public int getQuerySamplingRate() { return querySamplingRate; }
    public void setQuerySamplingRate(int querySamplingRate) { this.querySamplingRate = querySamplingRate; }

}
