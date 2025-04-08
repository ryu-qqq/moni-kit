package com.monikit.starter.jdbc.logging;

import com.monikit.core.LogLevel;

/**
 * SQL 쿼리의 실행 시간을 기반으로 로그 레벨을 평가하는 유틸리티 클래스.
 *
 * <p>
 * - 실행 시간이 {@code criticalQueryThresholdMs}보다 크면 {@link LogLevel#ERROR}
 * - 실행 시간이 {@code slowQueryThresholdMs}보다 크면 {@link LogLevel#WARN}
 * - 그렇지 않으면 {@link LogLevel#INFO}
 * </p>
 *
 * <p>
 * 정적 메서드만 제공하며 인스턴스 생성은 불가능하다.
 * </p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * LogLevel level = QueryPerformanceEvaluator.evaluate(1500, 1000, 5000);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */
public class QueryPerformanceEvaluator {

    private QueryPerformanceEvaluator() {
        // 유틸리티 클래스 - 인스턴스화 방지
    }

    /**
     * 쿼리 실행 시간을 기준으로 로그 레벨을 평가한다.
     *
     * @param executionTime         쿼리 실행 시간 (ms)
     * @param slowQueryThresholdMs  슬로우 쿼리 임계값 (ms)
     * @param criticalQueryThresholdMs 크리티컬 쿼리 임계값 (ms)
     * @return 평가된 {@link LogLevel}
     */

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
