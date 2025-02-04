package com.monikit.starter;

import jakarta.annotation.PostConstruct;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.monikit.core.TraceIdProvider;
/**
 * MDC 기반의 `TraceIdProvider` 구현체.
 * <p>
 * - Spring Boot 환경에서 MDC를 사용하여 Trace ID를 자동으로 관리.
 * - `monitoring-core`의 `TraceIdProvider`에 자동으로 등록됨.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Component
public class MdcTraceIdProvider implements TraceIdProvider {

    private static final String TRACE_ID_KEY = "traceId";

    @Override
    public String getTraceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        return (traceId != null) ? traceId : "N/A";
    }

    /**
     * Trace ID를 MDC에 설정하는 메서드 (Spring Boot 환경에서 활용 가능).
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    /**
     * MDC에서 Trace ID를 제거하는 메서드.
     */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }

    /**
     * Spring Boot 실행 시 `monitoring-core`의 TraceIdProvider에 자동 등록.
     */
    @PostConstruct
    public void init() {
        TraceIdProvider.setInstance(this);
    }


}