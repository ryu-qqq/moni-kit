package com.monikit.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * MoniKit 메트릭 수집 설정을 관리하는 클래스.
 * <p>
 * - HTTP 메트릭 필터 활성화 여부 (`metricsEnabled`)
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@ConfigurationProperties(prefix = "monikit.metrics")
public class MoniKitMetricsProperties {

    private boolean metricsEnabled = true;

    public boolean isMetricsEnabled() { return metricsEnabled; }
    public void setMetricsEnabled(boolean metricsEnabled) { this.metricsEnabled = metricsEnabled; }


}
