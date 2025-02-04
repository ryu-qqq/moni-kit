package com.monikit.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MoniKit 로깅 설정을 관리하는 클래스.
 * <p>
 * - 상세 로깅 여부 (`detailedLogging`)
 * - SQL 쿼리 성능 로깅 설정 (`slowQueryThresholdMs`, `criticalQueryThresholdMs`)
 * - 데이터베이스 로깅 활성화 여부 (`datasourceLoggingEnabled`)
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Component
@ConfigurationProperties(prefix = "monikit.logging")
public class MoniKitLoggingProperties {

    private boolean detailedLogging = false;
    private long slowQueryThresholdMs = 1000;
    private long criticalQueryThresholdMs = 5000;
    private boolean datasourceLoggingEnabled = true;

    public boolean isDetailedLogging() {
        return detailedLogging;
    }

    public void setDetailedLogging(boolean detailedLogging) {
        this.detailedLogging = detailedLogging;
    }

    public long getSlowQueryThresholdMs() {
        return slowQueryThresholdMs;
    }

    public void setSlowQueryThresholdMs(long slowQueryThresholdMs) {
        this.slowQueryThresholdMs = slowQueryThresholdMs;
    }

    public long getCriticalQueryThresholdMs() {
        return criticalQueryThresholdMs;
    }

    public void setCriticalQueryThresholdMs(long criticalQueryThresholdMs) {
        this.criticalQueryThresholdMs = criticalQueryThresholdMs;
    }

    public boolean isDatasourceLoggingEnabled() {
        return datasourceLoggingEnabled;
    }

    public void setDatasourceLoggingEnabled(boolean datasourceLoggingEnabled) {
        this.datasourceLoggingEnabled = datasourceLoggingEnabled;
    }

}
