package com.monikit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MoniKit의 메트릭 수집 기능에 대한 설정을 정의하는 구성 클래스.
 * <p>
 *  아래 설정 항목들을 통해 메트릭 수집 범위를 조절할 수 있습니다:
 * </p>
 *
 * <ul>
 *     <li><b>metricsEnabled</b> : 전체 메트릭 수집 기능의 마스터 스위치</li>
 *     <li><b>queryMetricsEnabled</b> : SQL 쿼리 실행 시간, 횟수 등의 메트릭 수집 여부</li>
 *     <li><b>httpMetricsEnabled</b> : HTTP 요청/응답 관련 메트릭 수집 여부</li>
 * </ul>
 * <p>
 * 설정 접두사는 <code>monikit.metrics</code> 입니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */


@ConfigurationProperties(prefix = "monikit.metrics")
public class MoniKitMetricsProperties {

    private boolean metricsEnabled = true;
    private boolean queryMetricsEnabled = true;
    private boolean httpMetricsEnabled = true;

    public boolean isMetricsEnabled() { return metricsEnabled; }
    public void setMetricsEnabled(boolean metricsEnabled) { this.metricsEnabled = metricsEnabled; }

    public boolean isQueryMetricsEnabled() { return queryMetricsEnabled; }
    public void setQueryMetricsEnabled(boolean queryMetricsEnabled) { this.queryMetricsEnabled = queryMetricsEnabled; }

    public boolean isHttpMetricsEnabled() { return httpMetricsEnabled; }
    public void setHttpMetricsEnabled(boolean httpMetricsEnabled) { this.httpMetricsEnabled = httpMetricsEnabled; }

}
