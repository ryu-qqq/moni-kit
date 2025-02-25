package com.monikit.starter.config;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * MoniKit 로깅 설정을 관리하는 클래스.
 * <p>
 * - 상세 로깅 여부 (detailedLogging)
 * - SQL 쿼리 성능 로깅 설정 (slowQueryThresholdMs, criticalQueryThresholdMs)
 * - 데이터베이스 로깅 활성화 여부 (datasourceLoggingEnabled)
 * - HTTP 필터 활성화 여부 (filtersEnabled)
 * - Trace ID 필터 활성화 여부 (traceEnabled)
 * - HTTP 인터셉터 활성화 여부 (interceptorsEnabled)
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "monikit.logging")
public class MoniKitLoggingProperties {

    private static final Logger logger = LoggerFactory.getLogger(MoniKitLoggingProperties.class);

    private boolean detailedLogging = false;
    private long slowQueryThresholdMs = 1000;
    private long criticalQueryThresholdMs = 5000;
    private boolean datasourceLoggingEnabled = true;
    private boolean traceEnabled = true;
    private boolean logEnabled = true;

    public boolean isDetailedLogging() { return detailedLogging; }
    public void setDetailedLogging(boolean detailedLogging) { this.detailedLogging = detailedLogging; }

    public long getSlowQueryThresholdMs() { return slowQueryThresholdMs; }
    public void setSlowQueryThresholdMs(long slowQueryThresholdMs) { this.slowQueryThresholdMs = slowQueryThresholdMs; }

    public long getCriticalQueryThresholdMs() { return criticalQueryThresholdMs; }
    public void setCriticalQueryThresholdMs(long criticalQueryThresholdMs) { this.criticalQueryThresholdMs = criticalQueryThresholdMs; }

    public boolean isDatasourceLoggingEnabled() { return datasourceLoggingEnabled; }
    public void setDatasourceLoggingEnabled(boolean datasourceLoggingEnabled) { this.datasourceLoggingEnabled = datasourceLoggingEnabled; }

    public boolean isTraceEnabled() { return traceEnabled; }
    public void setTraceEnabled(boolean traceEnabled) { this.traceEnabled = traceEnabled; }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    /**
     * logEnabled가 false인데 특정 로깅 관련 설정이 활성화되어 있으면 경고 로그 출력
     */
    @PostConstruct
    public void validateLoggingConfiguration() {
        if (!logEnabled && (datasourceLoggingEnabled || traceEnabled || detailedLogging)) {
            logger.warn("logEnabled is disabled (false), but some logging settings (datasourceLoggingEnabled, traceEnabled, detailedLogging) are enabled. Logging may not be recorded.");
        }
    }
}