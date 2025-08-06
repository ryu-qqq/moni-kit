package com.monikit.otel.trace;

import com.monikit.core.TraceIdProvider;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

/**
 * OpenTelemetry 기반 TraceId 제공자.
 * <p>
 * 기존 {@link com.monikit.core.DefaultTraceIdProvider}를 대체하여
 * OpenTelemetry의 표준 Trace Context를 활용합니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 2.0.0
 */
public class OtelTraceIdProvider implements TraceIdProvider {

    private final Tracer tracer;

    public OtelTraceIdProvider(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public String getTraceId() {
        SpanContext spanContext = Span.current().getSpanContext();
        if (spanContext.isValid()) {
            return spanContext.getTraceId();
        }
        
        // 현재 활성 Span이 없는 경우 새로운 Span 생성
        Span span = tracer.spanBuilder("monikit-trace")
            .startSpan();
        
        try {
            return span.getSpanContext().getTraceId();
        } finally {
            // 임시 Span은 즉시 종료
            span.end();
        }
    }

    @Override
    public void setTraceId(String traceId) {
        // OpenTelemetry에서는 TraceId를 직접 설정할 수 없음
        // 대신 현재 Context에서 Span을 가져와서 사용
        // 필요시 SpanBuilder로 새로운 Span 생성
    }

    @Override
    public void clear() {
        // OpenTelemetry Context는 자동으로 관리되므로 명시적 clear 불필요
        // 하지만 호환성을 위해 메서드 유지
    }
}
