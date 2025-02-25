package com.monikit.core;

/**
 * MoniKit에서 사용되는 로그 유형(Enum).
 * <p>
 * 로그 유형은 ELK 및 Kibana에서 logType 필터링을 통해
 * 특정 유형의 로그만 조회하는 데 사용됩니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public enum LogType {
    EXECUTION_TIME,
    EXECUTION_DETAIL,
    EXCEPTION,
    DATABASE_QUERY,
    INBOUND_REQUEST,
    INBOUND_RESPONSE,
    OUTBOUND_REQUEST,
    OUTBOUND_RESPONSE,
    BATCH_JOB

}
