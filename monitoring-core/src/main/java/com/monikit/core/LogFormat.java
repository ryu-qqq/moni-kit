package com.monikit.core;

import java.time.Instant;

public interface LogFormat {
    /**
     * 로그의 타임스탬프.
     * @return 로그 생성 시각 (UTC 기준)
     */
    Instant getTimestamp();

    /**
     * 로그 레벨.
     * @return 로그의 심각도 (INFO, WARN, ERROR 등)
     */
    LogLevel getLevel();

    /**
     * 로깅이 발생한 클래스명.
     * @return 로그가 찍힌 클래스명
     */
    String getClassName();

    /**
     * Trace ID (분산 트레이싱 지원).
     * @return 요청 또는 트랜잭션의 고유 ID
     */
    String getTraceId();

}
