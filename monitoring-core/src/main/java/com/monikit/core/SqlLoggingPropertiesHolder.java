package com.monikit.core;

/**
 * SQL 로깅 임계값을 저장하는 정적 클래스.
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class SqlLoggingPropertiesHolder {
    private static long slowQueryThresholdMs = 1000;
    private static long criticalQueryThresholdMs = 5000;

    public static long getSlowQueryThresholdMs() {
        return slowQueryThresholdMs;
    }

    public static void setSlowQueryThresholdMs(long value) {
        slowQueryThresholdMs = value;
    }

    public static long getCriticalQueryThresholdMs() {
        return criticalQueryThresholdMs;
    }

    public static void setCriticalQueryThresholdMs(long value) {
        criticalQueryThresholdMs = value;
    }
}