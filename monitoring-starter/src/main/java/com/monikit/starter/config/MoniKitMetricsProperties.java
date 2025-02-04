package com.monikit.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MoniKit 메트릭 수집 설정을 관리하는 클래스.
 * <p>
 * - 메트릭 수집 활성화 여부 (`enabled`)
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "monikit.metrics")
public class MoniKitMetricsProperties {

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
