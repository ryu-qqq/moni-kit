package com.monikit.core.model;

import java.time.Instant;

import com.monikit.core.LogLevel;
import com.monikit.core.LogType;

/**
 * MoniKit의 모든 로그 엔트리가 구현해야 하는 인터페이스입니다.
 * <p>
 * 이 인터페이스를 구현하면 ELK 및 Prometheus와 연동할 수 있는
 * 구조화된 로그 데이터를 생성할 수 있습니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public interface LogEntry {
    /**
     * 로그의 타임스탬프를 반환합니다.
     * <p>
     * 로그가 생성된 시점을 UTC 기준으로 저장하며,
     * ELK 및 Kibana에서 검색할 때 활용됩니다.
     * </p>
     *
     * @return 로그 생성 시각 (UTC 기준)
     */
    Instant getTimestamp();

    /**
     * Trace ID를 반환합니다.
     * <p>
     * 하나의 요청 또는 트랜잭션을 추적하기 위해 사용됩니다.
     * 같은 Trace ID를 가진 로그는 동일한 요청에서 발생한 것으로 간주됩니다.
     * </p>
     *
     * @return 요청 또는 트랜잭션의 고유 ID
     */
    String getTraceId();

    /**
     * 로그 유형을 반환합니다.
     * <p>
     * {@link LogType} Enum을 사용하여 정해진 로그 유형만 사용 가능하며,
     * Kibana에서 logType 필터링을 통해 특정 유형의 로그만 조회할 수 있습니다.
     * </p>
     *
     * @return 로그의 유형 (예: EXECUTION_TIME, BUSINESS_EVENT, EXCEPTION 등)
     */
    LogType getLogType();

    /**
     * 로그 레벨을 반환합니다.
     * <p>
     * 로그 레벨은 TRACE, DEBUG, INFO, WARN, ERROR, CRITICAL 등의 값으로 구분됩니다.
     * ELK에서 logLevel을 기준으로 필터링하여 특정 심각도의 로그만 분석할 수 있습니다.
     * </p>
     *
     * @return 로그의 심각도 (TRACE, DEBUG, INFO, WARN, ERROR)
     */
    LogLevel getLogLevel();

    String toString();

}
