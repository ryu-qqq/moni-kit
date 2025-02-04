package com.monikit.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MoniKit SQL 쿼리 성능 로깅 설정.
 * <p>
 * - `slowQueryThresholdMs` → WARN 로그 기준 (기본 1000ms)
 * - `criticalQueryThresholdMs` → ERROR 로그 기준 (기본 5000ms)
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "monikit.logging.sql")
public class SqlLoggingProperties {
    private long slowQueryThresholdMs = 1000;
    private long criticalQueryThresholdMs = 5000;

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
}