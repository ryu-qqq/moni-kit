package com.monikit.starter;

/**
 * 요청 단위로 Trace ID를 관리하는 클래스 (MDC 기반).
 * <p>
 * Spring Boot의 요청별 MDC와 연동하여 Trace ID를 자동으로 관리한다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class TraceIdHolder {

    private static final String TRACE_ID_KEY = "traceId";

    /**
     * 현재 요청의 Trace ID를 반환한다.
     * MDC에서 값을 조회하여 제공한다.
     *
     * @return 현재 Trace ID (없으면 null)
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * 현재 요청의 Trace ID를 설정한다.
     * MDC에 값을 저장하여 로깅과 함께 추적할 수 있도록 한다.
     *
     * @param traceId 설정할 Trace ID
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    /**
     * 요청이 끝나면 MDC에서 Trace ID를 제거한다.
     */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }

}