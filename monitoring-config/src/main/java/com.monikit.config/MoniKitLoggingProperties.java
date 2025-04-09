package com.monikit.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PostConstruct;

/**
 * MoniKit의 로깅 기능을 제어하는 구성 프로퍼티 클래스.
 * <p>
 * 아래와 같은 기능들을 설정할 수 있습니다:
 * </p>
 * <ul>
 *     <li><b>detailedLogging</b>: SQL 파라미터 등 상세 정보 로깅 여부</li>
 *     <li><b>slowQueryThresholdMs</b>: 느린 쿼리로 간주할 임계값 (ms)</li>
 *     <li><b>criticalQueryThresholdMs</b>: 매우 느린 쿼리로 간주할 임계값 (ms)</li>
 *     <li><b>datasourceLoggingEnabled</b>: JDBC SQL 실행 로깅 활성화 여부</li>
 *     <li><b>traceEnabled</b>: Trace ID 기반 추적 로그 활성화 여부</li>
 *     <li><b>logEnabled</b>: 전체 로깅 기능의 마스터 스위치</li>
 * </ul>
 * <p>
 * 설정 키 접두사는 <code>monikit.logging</code> 입니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

@Primary
@Configuration
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

    @PostConstruct
    public void validateLoggingConfiguration() {
        if (!logEnabled && (datasourceLoggingEnabled || traceEnabled || detailedLogging)) {
            logger.warn("logEnabled is disabled (false), but some logging settings (datasourceLoggingEnabled, traceEnabled, detailedLogging) are enabled. Logging may not be recorded.");
        }
    }

}