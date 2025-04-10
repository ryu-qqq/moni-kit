package com.monikit.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MoniKit의 로깅 기능을 제어하는 구성 프로퍼티 클래스.
 * <p>
 * 아래 설정 항목들을 통해 로그 출력 범위 및 상세 수준을 조절할 수 있습니다:
 * </p>
 *
 * <ul>
 *     <li><b>logEnabled</b>: 전체 로깅 기능의 마스터 스위치</li>
 *     <li><b>traceEnabled</b>: Trace ID 기반 추적 로그 활성화 여부</li>
 *     <li><b>detailedLogging</b>: SQL 파라미터, 메서드 인자 등 상세 정보 포함 여부</li>
 *     <li><b>summaryLogging</b>: Execution 로그 요약 모드 활성화 여부 (기본값: true)</li>
 *     <li><b>thresholdMillis</b>: 상세 로그로 전환할 실행 시간 임계값 (ms)</li>
 *     <li><b>slowQueryThresholdMs</b>: 느린 쿼리로 간주할 기준 시간 (ms)</li>
 *     <li><b>criticalQueryThresholdMs</b>: 매우 느린 쿼리로 간주할 기준 시간 (ms)</li>
 *     <li><b>datasourceLoggingEnabled</b>: JDBC SQL 실행 로깅 활성화 여부</li>
 * </ul>
 *
 * <p>
 * 설정 접두사는 <code>monikit.logging</code> 입니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

@ConfigurationProperties(prefix = "monikit.logging")
public class MoniKitLoggingProperties {

    private boolean detailedLogging = false;
    private long slowQueryThresholdMs = 1000;
    private long criticalQueryThresholdMs = 5000;
    private boolean datasourceLoggingEnabled = true;
    private boolean traceEnabled = true;
    private boolean logEnabled = true;
    private boolean summaryLogging = true;
    private long thresholdMillis = 300;

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

    public boolean isSummaryLogging() {
        return summaryLogging;
    }

    public long getThresholdMillis() {
        return thresholdMillis;
    }

    public void setSummaryLogging(boolean summaryLogging) {
        this.summaryLogging = summaryLogging;
    }

    public void setThresholdMillis(long thresholdMillis) {
        this.thresholdMillis = thresholdMillis;
    }

}