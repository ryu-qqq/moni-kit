package com.monikit.config;


import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * MoniKit의 로깅 설정을 정의하는 구성 프로퍼티 클래스.
 *
 * <p>접두사: <code>monikit.logging</code></p>
 *
 * <ul>
 *     <li><b>logEnabled</b>: 전체 로깅 기능의 마스터 스위치</li>
 *     <li><b>datasourceLoggingEnabled</b>: JDBC SQL 실행 로깅 활성화 여부</li>
 *     <li><b>slowQueryThresholdMs</b>: 느린 쿼리로 간주할 기준 시간 (ms)</li>
 *     <li><b>criticalQueryThresholdMs</b>: 매우 느린 쿼리로 간주할 기준 시간 (ms)</li>
 *     <li><b>dynamicMatching</b>: 클래스/메서드 이름 및 조건 기반 동적 로깅 규칙</li>
 *     <li><b>allowedPackages: 로그 허용 패키지 (예: "com.ryuqq)</li>
 * </ul>
 *
 * <p>추적 ID(traceId)는 항상 자동으로 수집되며 별도 설정은 제공되지 않습니다.</p>
 *
 * @author ryu-qqq
 * @since 1.1.2
 */
@ConfigurationProperties(prefix = "monikit.logging")
public class MoniKitLoggingProperties {

    private boolean logEnabled = true;
    private boolean datasourceLoggingEnabled = true;
    private long slowQueryThresholdMs = 1000;
    private long criticalQueryThresholdMs = 5000;
    private List<String> allowedPackages = List.of();
    private List<DynamicLogRule> dynamicMatching = new ArrayList<>();

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    public boolean isDatasourceLoggingEnabled() {
        return datasourceLoggingEnabled;
    }

    public void setDatasourceLoggingEnabled(boolean datasourceLoggingEnabled) {
        this.datasourceLoggingEnabled = datasourceLoggingEnabled;
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

    public List<DynamicLogRule> getDynamicMatching() {
        return dynamicMatching;
    }

    public void setDynamicMatching(List<DynamicLogRule> dynamicMatching) {
        this.dynamicMatching = dynamicMatching;
    }

    public List<String> getAllowedPackages() {
        return allowedPackages;
    }

    public void setAllowedPackages(List<String> allowedPackages) {
        this.allowedPackages = allowedPackages;
    }
}