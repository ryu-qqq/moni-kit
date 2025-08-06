package com.monikit.core;

import java.util.UUID;

/**
 * 기본 TraceIdProvider 구현체.
 * <p>
 * - ThreadLocal 기반으로 Trace ID를 저장하고 제공함.
 * - Trace ID가 존재하지 않으면 자동으로 UUID를 생성함.
 * - 요청 종료 시 {@code clear()}로 ThreadLocal 제거 필요.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 * @deprecated 2.0.0부터 {@link com.monikit.otel.trace.OtelTraceIdProvider}를 사용하세요.
 *             OpenTelemetry 표준을 준수하며 AWS X-Ray와 완전히 호환됩니다.
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public class DefaultTraceIdProvider implements TraceIdProvider {

    private static final ThreadLocal<String> TRACE_ID_HOLDER = new ThreadLocal<>();

    @Override
    public String getTraceId() {
        String traceId = TRACE_ID_HOLDER.get();
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            TRACE_ID_HOLDER.set(traceId);
        }
        return traceId;
    }

    @Override
    public void setTraceId(String traceId) {
        TRACE_ID_HOLDER.set(traceId);
    }

    @Override
    public void clear() {
        TRACE_ID_HOLDER.remove();
    }
}
