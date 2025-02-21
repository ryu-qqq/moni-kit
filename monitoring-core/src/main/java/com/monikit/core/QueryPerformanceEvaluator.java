package com.monikit.core;

/**
 * SQL 쿼리 성능을 평가하여 적절한 로그 레벨을 결정하는 유틸리티.
 * <p>
 * - 실행 시간이 `slowQueryThresholdMs`보다 크면 WARN
 * - 실행 시간이 `criticalQueryThresholdMs`보다 크면 ERROR
 * - 기본적으로 INFO
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class QueryPerformanceEvaluator {

    private QueryPerformanceEvaluator() {
    }

    public static LogLevel evaluate(long executionTime, long slowQueryThresholdMs, long criticalQueryThresholdMs) {
        if (executionTime > criticalQueryThresholdMs) {
            return LogLevel.ERROR;
        } else if (executionTime > slowQueryThresholdMs) {
            return LogLevel.WARN;
        } else {
            return LogLevel.INFO;
        }
    }

}
