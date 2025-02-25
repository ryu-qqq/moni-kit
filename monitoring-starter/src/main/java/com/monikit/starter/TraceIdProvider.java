package com.monikit.starter;

import org.slf4j.MDC;

/**
 * MDC 기반의 `TraceIdProvider` 유틸리티 클래스.
 * <p>
 * - Spring Boot 환경에서 MDC를 사용하여 Trace ID를 자동으로 관리.
 * - `MDC`는 내부적으로 `ThreadLocal`을 사용하여 스레드별로 Trace ID를 관리함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public final class TraceIdProvider {

    private static final String TRACE_ID_KEY = "traceId";

    private TraceIdProvider() {
    }

    /**
     * 현재 Trace ID를 반환한다.
     *
     * @return 현재 Trace ID (없으면 "N/A" 반환)
     */
    public static String getTraceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        return (traceId != null) ? traceId : "N/A";
    }

    /**
     * 현재 Trace ID를 설정한다.
     *
     * @param traceId 설정할 Trace ID
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    /**
     * 현재 Trace ID를 초기화한다.
     */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }
}